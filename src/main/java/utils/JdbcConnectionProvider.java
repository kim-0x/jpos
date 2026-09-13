package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class JdbcConnectionProvider implements ConnectionProvider {
    private final String jdbcUrl;
    private final String connectionInitSql;

    public JdbcConnectionProvider(String jdbcUrl) {
        this(jdbcUrl, jdbcUrl != null && jdbcUrl.startsWith("jdbc:sqlite:") ? "PRAGMA foreign_keys = ON" : null);
    }

    public JdbcConnectionProvider(String jdbcUrl, String connectionInitSql) {
        this.jdbcUrl = jdbcUrl;
        this.connectionInitSql = connectionInitSql;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        if (connectionInitSql != null && !connectionInitSql.isBlank()) {
            try (var stmt = connection.createStatement()) {
                stmt.execute(connectionInitSql);
            }
        }
        return connection;
    }
}
