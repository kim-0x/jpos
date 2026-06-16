package utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens JDBC connections to the SQLite database file.
 * Each call to {@link #getConnection()} returns a new, independent connection
 * that the caller is responsible for closing (use try-with-resources).
 * Foreign-key enforcement is switched on for every connection.
 */
public final class SqliteConnectionProvider {

    private static final String DEFAULT_DB_SUB_DIR = "db";
    private static final String DEFAULT_DB_FILE    = "jpos.db";

    private final String jdbcUrl;

    /** Uses the default {@code data/db/jpos.db} path resolved from the project root. */
    public SqliteConnectionProvider() {
        this(DataSourcePathHelper.getDefaultFilePath(DEFAULT_DB_SUB_DIR, DEFAULT_DB_FILE));
    }

    public SqliteConnectionProvider(Path dbFilePath) {
        this("jdbc:sqlite:" + dbFilePath.toAbsolutePath());
        ensureParentDirectoryExists(dbFilePath);
    }

    public SqliteConnectionProvider(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * Returns a new {@link Connection}.  The caller must close it (try-with-resources).
     * Foreign keys are enabled on the connection before it is returned.
     */
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (var stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private static void ensureParentDirectoryExists(Path dbFilePath) {
        try {
            Path parent = dbFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot create database directory: " + dbFilePath.getParent(), e);
        }
    }
}
