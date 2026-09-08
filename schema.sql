-- Habit Tracker database schema (SQLite)
-- This file documents the schema. DatabaseConnection.java runs the equivalent
-- CREATE TABLE IF NOT EXISTS statements automatically on startup, so you do not
-- need to run this file manually -- it's kept here for documentation/reference.

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS habits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('BOOLEAN', 'COUNTABLE', 'TIMED')),
    frequency TEXT NOT NULL CHECK (frequency IN ('DAILY', 'WEEKLY')),
    target_value INTEGER,
    created_date TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS habit_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    habit_id INTEGER NOT NULL,
    log_date TEXT NOT NULL,
    value REAL NOT NULL,
    UNIQUE (habit_id, log_date),
    FOREIGN KEY (habit_id) REFERENCES habits(id)
);
