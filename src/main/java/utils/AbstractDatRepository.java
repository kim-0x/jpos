package utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDatRepository<T extends Serializable> {
    private final Path filePath;

    protected AbstractDatRepository(Path filePath) {
        this.filePath = filePath;
    }

    protected final List<T> loadFromDat() {
        ensureDatFileExists(filePath);

        try {
            if (Files.size(filePath) == 0L) {
                return new ArrayList<>();
            }
        } catch (IOException exception) {
            throw new IllegalStateException(String.format("Unable to inspect DAT file: %s", filePath), exception);
        }

        try (ObjectInputStream objectInputStream = new ObjectInputStream(Files.newInputStream(filePath))) {
            Object content = objectInputStream.readObject();
            return castContent(content);
        } catch (IOException | ClassNotFoundException exception) {
            throw new IllegalStateException(String.format("Unable to read DAT file: %s", filePath), exception);
        }
    }

    protected final void persistToDat(Iterable<T> records) {
        ensureParentDirectoryExists(filePath);

        ArrayList<T> serializableRecords = new ArrayList<>();
        for (T record : records) {
            serializableRecords.add(record);
        }

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            objectOutputStream.writeObject(serializableRecords);
            objectOutputStream.flush();
        } catch (IOException exception) {
            throw new IllegalStateException(String.format("Unable to write DAT file: %s", filePath), exception);
        }
    }

    protected static Path getDefaultDatFilePath(String... relativeSegments) {
        Path currentPath = findProjectRoot(Path.of("").toAbsolutePath().normalize())
                .resolve("data")
                .resolve("dat");

        for (String relativeSegment : relativeSegments) {
            currentPath = currentPath.resolve(relativeSegment);
        }

        return currentPath.normalize();
    }

    private static Path findProjectRoot(Path startPath) {
        for (Path currentPath = startPath; currentPath != null; currentPath = currentPath.getParent()) {
            if (Files.isRegularFile(currentPath.resolve("pom.xml"))
                    && Files.isDirectory(currentPath.resolve("data"))) {
                return currentPath;
            }
        }

        return startPath;
    }

    private List<T> castContent(Object content) {
        if (content == null) {
            return new ArrayList<>();
        }
        if (!(content instanceof List<?> rawList)) {
            throw new IllegalStateException("Invalid DAT content. Expected a list.");
        }

        ArrayList<T> records = new ArrayList<>(rawList.size());
        for (Object item : rawList) {
            if (!getEntityType().isInstance(item)) {
                throw new IllegalStateException(String.format(
                        "Invalid DAT entry type. Expected '%s' but found '%s'.",
                        getEntityType().getName(),
                        item == null ? "null" : item.getClass().getName()));
            }
            records.add(getEntityType().cast(item));
        }

        return records;
    }

    private void ensureDatFileExists(Path sourceFilePath) {
        if (sourceFilePath == null || !Files.exists(sourceFilePath) || !Files.isRegularFile(sourceFilePath)) {
            throw new IllegalStateException(String.format("DAT file does not exist: %s", sourceFilePath));
        }
    }

    private void ensureParentDirectoryExists(Path targetFilePath) {
        if (targetFilePath == null) {
            throw new IllegalStateException("DAT file path must not be null.");
        }

        Path parentPath = targetFilePath.getParent();
        if (parentPath == null) {
            return;
        }

        try {
            Files.createDirectories(parentPath);
        } catch (IOException exception) {
            throw new IllegalStateException(String.format("Unable to create DAT directory: %s", parentPath),
                    exception);
        }
    }

    protected abstract Class<T> getEntityType();
}
