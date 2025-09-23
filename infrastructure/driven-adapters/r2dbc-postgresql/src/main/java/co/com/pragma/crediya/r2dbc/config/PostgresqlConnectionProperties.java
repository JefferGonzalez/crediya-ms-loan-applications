package co.com.pragma.crediya.r2dbc.config;

import io.r2dbc.postgresql.client.SSLMode;

public record PostgresqlConnectionProperties(
        String host,
        Integer port,
        String database,
        String schema,
        String username,
        String password,
        SSLMode sslMode) {
}
