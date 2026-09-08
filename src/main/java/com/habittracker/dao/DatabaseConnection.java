package com.habittracker.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the single JDBC connection to the local SQLite database file
 * and ensures the schema exists before any DAO uses it.
 */
public final class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:habits.db";
    private static Connection connection;

    private DatabaseConnection() {
    }

    /**
     * Returns the shared connection, opening it (and creating the schema)
     * on first use.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            initializeSchema(connection);
        }
        return connection;
    }

    private static void initializeSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS habits (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL CHECK (type IN ('BOOLEAN', 'COUNTABLE', 'TIMED')),
                    frequency TEXT NOT NULL CHECK (frequency IN ('DAILY', 'WEEKLY')),
                    target_value INTEGER,
                    created_date TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS habit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    habit_id INTEGER NOT NULL,
                    log_date TEXT NOT NULL,
                    value REAL NOT NULL,
                    UNIQUE (habit_id, log_date),
                    FOREIGN KEY (habit_id) REFERENCES habits(id)
                )
                """);
        }
    }

    /** Closes the shared connection. Call this once, on application shutdown. */
    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
