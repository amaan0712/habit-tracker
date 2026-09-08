package com.habittracker.dao;

import com.habittracker.model.Habit;
import com.habittracker.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all JDBC access for the users table.
 */
public class UserDao {

    /** Inserts a new user and returns it with the database-generated id set. */
    public User save(String username) throws SQLException {
        String sql = "INSERT INTO users (username) VALUES (?)";
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), username);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated id for user: " + username);
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username FROM users WHERE username = ?";
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("username"));
                }
            }
        }
        return null;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, username FROM users WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("username"));
                }
            }
        }
        return null;
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, username FROM users";
        Connection conn = DatabaseConnection.getConnection();
        List<User> users = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username")));
            }
        }
        return users;
    }

    /**
     * Loads every user with their habits (and each habit's log entries) already
     * attached, ready for streak/leaderboard calculations without further queries.
     */
    public List<User> findAllWithHabitsAndLogs(HabitDao habitDao, HabitLogDao logDao) throws SQLException {
        List<User> users = findAll();

        for (User user : users) {
            List<Habit> habits = habitDao.findByUserId(user.getId());
            for (Habit habit : habits) {
                logDao.findByHabitId(habit.getId()).forEach(habit::addLogEntry);
                user.addHabit(habit);
            }
        }
        return users;
    }
}
