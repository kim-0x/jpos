package utils;

import java.nio.file.Files;
import java.nio.file.Path;

public final class DataSourcePathHelper {
    private DataSourcePathHelper() {
    }

    /**
     * INTENT: Build a default path under the project data directory for a specific data source type.
     * PRECONDITION: dataSubDirectory identifies a folder under data (for example csv, bin, or report),
     * and relativeSegments contains optional child path segments to append in order.
     * RETURNS: a normalized path rooted at projectRoot/data/dataSubDirectory with all relative segments
     * resolved.
     * POSTCONDITION: no filesystem content is modified while computing the target path.
     */
    public static Path getDefaultFilePath(String dataSubDirectory, String... relativeSegments) {
        Path currentPath = findProjectRoot(Path.of("").toAbsolutePath().normalize())
                .resolve("data")
                .resolve(dataSubDirectory);

        for (String relativeSegment : relativeSegments) {
            currentPath = currentPath.resolve(relativeSegment);
        }

        return currentPath.normalize();
    }

    /**
     * INTENT: Locate the repository root directory that contains the project descriptor and data folder.
     * PRECONDITION: startPath identifies a directory within or near the project tree that can be walked
     * upward through its parent paths.
     * RETURNS: the nearest ancestor path containing both pom.xml and the data directory, or startPath
     * when no matching project root is found.
     * POSTCONDITION: no filesystem content is modified while determining the project root path.
     */
    public static Path findProjectRoot(Path startPath) {
        for (Path currentPath = startPath; currentPath != null; currentPath = currentPath.getParent()) {
            if (Files.isRegularFile(currentPath.resolve("pom.xml"))
                    && Files.isDirectory(currentPath.resolve("data"))) {
                return currentPath;
            }
        }

        return startPath;
    }
}
