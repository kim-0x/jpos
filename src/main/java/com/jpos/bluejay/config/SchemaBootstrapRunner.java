package com.jpos.bluejay.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import utils.ConnectionProvider;

import java.sql.Connection;

@Component
public class SchemaBootstrapRunner implements ApplicationRunner {
    private final ConnectionProvider connectionProvider;
    private final ResourceLoader resourceLoader;

    public SchemaBootstrapRunner(ConnectionProvider connectionProvider, ResourceLoader resourceLoader) {
        this.connectionProvider = connectionProvider;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(ApplicationArguments args) {
        Resource schema = resourceLoader.getResource("classpath:db/schema.sql");
        try (Connection conn = connectionProvider.getConnection()) {
            ScriptUtils.executeSqlScript(conn, schema);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database schema", e);
        }
    }
}
