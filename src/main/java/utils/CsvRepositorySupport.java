package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class CsvRepositorySupport {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss.SSSSSS");

    private CsvRepositorySupport() {
    }

    public static Path getDefaultDataFilePath(String fileName) {
        return findProjectRoot(Path.of("").toAbsolutePath().normalize())
                .resolve("data")
                .resolve(fileName)
                .normalize();
    }

    public static List<String[]> readRows(Path filePath, String dataLabel) {
        ensureFileExists(filePath, dataLabel);

        try {
            List<String[]> rows = new ArrayList<>();
            for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                rows.add(parseCsvLine(line));
            }
            return rows;
        } catch (IOException exception) {
            throw new IllegalStateException(String.format("Unable to read %s file: %s", dataLabel, filePath),
                    exception);
        }
    }

    public static void writeRows(Path filePath, String dataLabel, List<String[]> rows) {
        ensureFileExists(filePath, dataLabel);

        List<String> content = new ArrayList<>();
        for (String[] row : rows) {
            content.add(toCsvLine(row));
        }

        try {
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(String.format("Unable to write %s file: %s", dataLabel, filePath),
                    exception);
        }
    }

    public static Date parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return null;
        }

        try {
            LocalDateTime localDateTime = LocalDateTime.parse(timestamp.trim(), TIMESTAMP_FORMATTER);
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(String.format("Invalid timestamp '%s'.", timestamp), exception);
        }
    }

    public static String formatTimestamp(Date timestamp) {
        if (timestamp == null) {
            return "";
        }

        LocalDateTime localDateTime = LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
        return TIMESTAMP_FORMATTER.format(localDateTime);
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

    private static void ensureFileExists(Path filePath, String dataLabel) {
        if (filePath == null || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IllegalStateException(String.format("%s file does not exist: %s", dataLabel, filePath));
        }
    }

    private static String[] parseCsvLine(String line) {
        ArrayList<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean insideQuotedValue = false;

        for (int index = 0; index < line.length(); index++) {
            char currentChar = line.charAt(index);
            if (currentChar == '"') {
                boolean isEscapedQuote = insideQuotedValue
                        && index + 1 < line.length()
                        && line.charAt(index + 1) == '"';
                if (isEscapedQuote) {
                    currentValue.append('"');
                    index++;
                    continue;
                }

                insideQuotedValue = !insideQuotedValue;
                continue;
            }

            if (currentChar == ',' && !insideQuotedValue) {
                values.add(currentValue.toString());
                currentValue = new StringBuilder();
                continue;
            }

            currentValue.append(currentChar);
        }

        values.add(currentValue.toString());
        return values.toArray(new String[0]);
    }

    private static String toCsvLine(String[] row) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < row.length; index++) {
            if (index > 0) {
                builder.append(',');
            }

            String value = row[index] == null ? "" : row[index];
            boolean requiresQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
            if (requiresQuotes) {
                builder.append('"');
                builder.append(value.replace("\"", "\"\""));
                builder.append('"');
                continue;
            }

            builder.append(value);
        }
        return builder.toString();
    }
}
