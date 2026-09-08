package com.habittracker.dao;

import com.habittracker.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all JDBC access for the habits table.
 * Maps each row to the correct Habit subclass (BooleanHabit, CountableHabit,
 * TimedHabit) based on the stored "type" discriminator column.
 */
public class HabitDao {

    /** Inserts a new habit and returns it with the database-generated id set. */
    public Habit save(Habit habit) throws SQLException {
        String sql = """
                INSERT INTO habits (user_id, name, type, frequency, target_value, created_date)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, habit.getUserId());
            ps.setString(2, habit.getName());
            ps.setString(3, typeOf(habit));
            ps.setString(4, habit.getFrequency().name());
            setTargetValue(ps, 5, habit);
            ps.setString(6, habit.getCreatedDate().toString());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return rebuildWithId(habit, keys.getInt(1));
                }
            }
        }
        throw new SQLException("Failed to retrieve generated id for habit: " + habit.getName());
    }

    public Habit findById(int id) throws SQLException {
        String sql = "SELECT * FROM habits WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Habit> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM habits WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        List<Habit> habits = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    habits.add(mapRow(rs));
                }
            }
        }
        return habits;
    }

    public List<Habit> findAll() throws SQLException {
        String sql = "SELECT * FROM habits";
        Connection conn = DatabaseConnection.getConnection();
        List<Habit> habits = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                habits.add(mapRow(rs));
            }
        }
        return habits;
    }

    // ---- helpers ----

    private String typeOf(Habit habit) {
        if (habit instanceof BooleanHabit) return "BOOLEAN";
        if (habit instanceof CountableHabit) return "COUNTABLE";
        if (habit instanceof TimedHabit) return "TIMED";
        throw new IllegalArgumentException("Unknown habit subclass: " + habit.getClass());
    }

    private void setTargetValue(PreparedStatement ps, int index, Habit habit) throws SQLException {
        if (habit instanceof CountableHabit countable) {
            ps.setInt(index, countable.getTargetCount());
        } else if (habit instanceof TimedHabit timed) {
            ps.setInt(index, timed.getTargetMinutes());
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    /** Builds a fresh Habit instance of the same subclass, but with the DB-generated id. */
    private Habit rebuildWithId(Habit original, int generatedId) {
        Habit.Frequency freq = original.getFrequency();
        LocalDate created = original.getCreatedDate();
        int userId = original.getUserId();
        String name = original.getName();

        if (original instanceof CountableHabit countable) {
            return new CountableHabit(generatedId, userId, name, freq, created, countable.getTargetCount());
        } else if (original instanceof TimedHabit timed) {
            return new TimedHabit(generatedId, userId, name, freq, created, timed.getTargetMinutes());
        } else {
            return new BooleanHabit(generatedId, userId, name, freq, created);
        }
    }

    private Habit mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        String name = rs.getString("name");
        String type = rs.getString("type");
        Habit.Frequency frequency = Habit.Frequency.valueOf(rs.getString("frequency"));
        LocalDate createdDate = LocalDate.parse(rs.getString("created_date"));
        int targetValue = rs.getInt("target_value");

        return switch (type) {
            case "COUNTABLE" -> new CountableHabit(id, userId, name, frequency, createdDate, targetValue);
            case "TIMED" -> new TimedHabit(id, userId, name, frequency, createdDate, targetValue);
            default -> new BooleanHabit(id, userId, name, frequency, createdDate);
        };
    }
}
