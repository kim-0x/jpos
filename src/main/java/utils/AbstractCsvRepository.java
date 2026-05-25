package utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCsvRepository<T> {
    private final Path filePath;

    protected AbstractCsvRepository(Path filePath) {
        this.filePath = filePath;
    }

    protected final List<T> loadFromCsv() {
        List<String[]> rows = CsvRepositorySupport.readRows(filePath, getDataLabel());
        ArrayList<T> records = new ArrayList<>();

        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            records.add(toEntity(rows.get(rowIndex), rowIndex + 1));
        }

        return records;
    }

    protected final void persistToCsv(Iterable<T> records) {
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(getHeaderRow());

        for (T record : records) {
            rows.add(toRow(record));
        }

        CsvRepositorySupport.writeRows(filePath, getDataLabel(), rows);
    }

    protected abstract String getDataLabel();

    protected abstract String[] getHeaderRow();

    protected abstract T toEntity(String[] row, int lineNumber);

    protected abstract String[] toRow(T entity);
}
