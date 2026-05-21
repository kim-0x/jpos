package utils;

import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class CsvRepositorySupportTest {
    @Test
    public void shouldResolveDataPathFromProjectRoot() {
        String originalUserDir = System.getProperty("user.dir");
        Path projectRoot = Path.of(originalUserDir).toAbsolutePath().normalize();

        try {
            System.setProperty("user.dir", projectRoot.toString());

            Path dataFilePath = CsvRepositorySupport.getDefaultDataFilePath("user.csv");

            assertEquals(projectRoot.resolve("data").resolve("user.csv").normalize(), dataFilePath);
        } finally {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    public void shouldResolveDataPathFromTargetDirectory() {
        String originalUserDir = System.getProperty("user.dir");
        Path projectRoot = Path.of(originalUserDir).toAbsolutePath().normalize();
        Path targetDirectory = projectRoot.resolve("target");

        try {
            System.setProperty("user.dir", targetDirectory.toString());

            Path dataFilePath = CsvRepositorySupport.getDefaultDataFilePath("user.csv");

            assertEquals(projectRoot.resolve("data").resolve("user.csv").normalize(), dataFilePath);
        } finally {
            System.setProperty("user.dir", originalUserDir);
        }
    }
}
