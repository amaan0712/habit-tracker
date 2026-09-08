package com.habittracker.dao;

import com.habittracker.model.LogEntry;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all JDBC access for the habit_logs table.
 */
public class HabitLogDao {

    /** Inserts a new log entry and returns it with the database-generated id set. */
    public LogEntry save(LogEntry entry) throws SQLException {
        String sql = "INSERT INTO habit_logs (habit_id, log_date, value) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entry.getHabitId());
            ps.setString(2, entry.getLogDate().toString());
            ps.setDouble(3, entry.getValue());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new LogEntry(keys.getInt(1), entry.getHabitId(), entry.getLogDate(), entry.getValue());
                }
            }
        }
        throw new SQLException("Failed to retrieve generated id for log entry on habit " + entry.getHabitId());
    }

    public List<LogEntry> findByHabitId(int habitId) throws SQLException {
        String sql = "SELECT id, habit_id, log_date, value FROM habit_logs WHERE habit_id = ? ORDER BY log_date";
        Connection conn = DatabaseConnection.getConnection();
        List<LogEntry> entries = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(new LogEntry(
                            rs.getInt("id"),
                            rs.getInt("habit_id"),
                            LocalDate.parse(rs.getString("log_date")),
                            rs.getDouble("value")));
                }
            }
        }
        return entries;
    }

    /** Returns true if this habit already has a log entry for the given date. */
    public boolean existsForDate(int habitId, LocalDate date) throws SQLException {
        String sql = "SELECT 1 FROM habit_logs WHERE habit_id = ? AND log_date = ?";
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void deleteById(int logId) throws SQLException {
        String sql = "DELETE FROM habit_logs WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, logId);
            ps.executeUpdate();
        }
    }
}
