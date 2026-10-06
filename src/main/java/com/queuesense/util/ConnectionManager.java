package com.queuesense.util;

import com.queuesense.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Enterprise ConnectionManager managing HikariCP connection pooling for QueueSense.
 */
public class ConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    private static final HikariDataSource dataSource;

    static {
        try {
            Properties props = new Properties();
            try (InputStream input = ConnectionManager.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (input == null) {
                    throw new DatabaseException("db.properties file not found on classpath!");
                }
                props.load(input);
            }

            // Register JDBC driver explicitly
            String driverClass = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            Class.forName(driverClass);

            HikariConfig config = new HikariConfig();
            config.setDriverClassName(driverClass);
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password", ""));

            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "30000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "10000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "600000")));

            config.setPoolName("QueueSense-HikariPool");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            logger.info("HikariCP DataSource initialized successfully with URL: {}", props.getProperty("db.url"));
        } catch (Exception ex) {
            logger.error("Failed to initialize database connection pool", ex);
            throw new DatabaseException("Failed to initialize database connection pool: " + ex.getMessage(), ex);
        }
    }

    private ConnectionManager() {}

    /**
     * Obtains a pooled database connection. Caller MUST close it (e.g. try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Safely rolls back a transaction if connection is active.
     */
    public static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.rollback();
                    logger.debug("Transaction rolled back successfully");
                }
            } catch (SQLException e) {
                logger.error("Failed to rollback transaction", e);
            }
        }
    }

    /**
     * Safely resets autoCommit and closes connection.
     */
    public static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.setAutoCommit(true);
                }
                conn.close();
            } catch (SQLException e) {
                logger.error("Failed to close connection", e);
            }
        }
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP DataSource closed.");
        }
    }
}
