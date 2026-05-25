package utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class AbstractCsvRepository<T> {
    private final Path filePath;
    private final String dataLabel;
    private final String[] header;

    protected AbstractCsvRepository(Path filePath, String dataLabel, String[] header) {
        this.filePath = Objects.requireNonNull(filePath, "File path is required.");
        this.dataLabel = Objects.requireNonNull(dataLabel, "Data label is required.");
        this.header = Objects.requireNonNull(header, "Header is required.").clone();
    }

    protected List<T> loadAll() {
        List<String[]> rows = CsvRepositorySupport.readRows(filePath, dataLabel);
        ArrayList<T> entities = new ArrayList<>();

        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            String[] row = rows.get(rowIndex);
            int lineNumber = rowIndex + 1;
            if (row.length != expectedColumnCount()) {
                throw new IllegalStateException(String.format("Invalid row at line %d.", lineNumber));
            }

            try {
                entities.add(mapRowToEntity(row, lineNumber));
            } catch (RuntimeException exception) {
                throw new IllegalStateException(String.format("Invalid row at line %d.", lineNumber), exception);
            }
        }

        return entities;
    }

    protected void persistAll(Collection<T> entities) {
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(header.clone());

        for (T entity : entities) {
            rows.add(mapEntityToRow(entity));
        }

        CsvRepositorySupport.writeRows(filePath, dataLabel, rows);
    }

    protected abstract T mapRowToEntity(String[] row, int lineNumber);

    protected abstract String[] mapEntityToRow(T entity);

    protected abstract int expectedColumnCount();
}
