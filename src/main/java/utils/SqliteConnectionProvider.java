package utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens JDBC connections for the configured database URL.
 */
public final class SqliteConnectionProvider {

    private static final String DEFAULT_DB_SUB_DIR = "db";
    private static final String DEFAULT_DB_FILE = "jpos.db";

    private final String jdbcUrl;
    private final String connectionInitSql;

    /** Uses the default {@code data/db/jpos.db} path resolved from the project root. */
    public SqliteConnectionProvider() {
        this(DataSourcePathHelper.getDefaultFilePath(DEFAULT_DB_SUB_DIR, DEFAULT_DB_FILE));
    }

    public SqliteConnectionProvider(Path dbFilePath) {
        this("jdbc:sqlite:" + dbFilePath.toAbsolutePath());
        ensureParentDirectoryExists(dbFilePath);
    }

    public SqliteConnectionProvider(String jdbcUrl) {
        this(jdbcUrl, jdbcUrl != null && jdbcUrl.startsWith("jdbc:sqlite:") ? "PRAGMA foreign_keys = ON" : null);
    }

    public SqliteConnectionProvider(String jdbcUrl, String connectionInitSql) {
        this.jdbcUrl = jdbcUrl;
        this.connectionInitSql = connectionInitSql;
    }

    /**
     * Returns a new {@link Connection}. The caller must close it.
     */
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        if (connectionInitSql != null && !connectionInitSql.isBlank()) {
            try (var stmt = connection.createStatement()) {
                stmt.execute(connectionInitSql);
            }
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
