package com.habittracker.service;

import com.habittracker.dao.HabitDao;
import com.habittracker.dao.HabitLogDao;
import com.habittracker.exception.DuplicateLogEntryException;
import com.habittracker.exception.InvalidHabitFrequencyException;
import com.habittracker.model.*;
import com.habittracker.streak.StreakCalculator;
import com.habittracker.streak.StreakCalculatorFactory;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Core service for creating habits and logging daily entries.
 * Backed by JDBC persistence via HabitDao and HabitLogDao, so data
 * survives between CLI runs instead of living only in memory.
 */
public class HabitService {

    public enum HabitType { BOOLEAN, COUNTABLE, TIMED }

    private final HabitDao habitDao;
    private final HabitLogDao habitLogDao;

    public HabitService(HabitDao habitDao, HabitLogDao habitLogDao) {
        this.habitDao = habitDao;
        this.habitLogDao = habitLogDao;
    }

    /**
     * Creates a new habit for a user, validating the target value where relevant,
     * and persists it immediately.
     *
     * @throws InvalidHabitFrequencyException if a countable/timed habit has a
     *         non-positive target, since that would make the habit impossible
     *         to fail or impossible to complete.
     */
    public Habit createHabit(int userId, String name, Habit.Frequency frequency,
                              HabitType type, int targetValue) throws InvalidHabitFrequencyException, SQLException {

        if ((type == HabitType.COUNTABLE || type == HabitType.TIMED) && targetValue <= 0) {
            throw new InvalidHabitFrequencyException(
                    "Target value for a " + type + " habit must be positive, got: " + targetValue);
        }

        LocalDate today = LocalDate.now();
        // id is a placeholder here; HabitDao.save() ignores it and returns
        // a fresh instance carrying the real database-generated id.
        Habit toSave = switch (type) {
            case BOOLEAN -> new BooleanHabit(0, userId, name, frequency, today);
            case COUNTABLE -> new CountableHabit(0, userId, name, frequency, today, targetValue);
            case TIMED -> new TimedHabit(0, userId, name, frequency, today, targetValue);
        };

        return habitDao.save(toSave);
    }

    /**
     * Logs a value for a habit on a given date and persists it.
     *
     * @throws DuplicateLogEntryException if that habit already has an entry for that date.
     */
    public LogEntry logEntry(int habitId, LocalDate date, double value)
            throws DuplicateLogEntryException, SQLException {

        if (habitLogDao.existsForDate(habitId, date)) {
            throw new DuplicateLogEntryException(
                    "Habit id " + habitId + " already has a log entry for " + date);
        }

        LogEntry toSave = new LogEntry(0, habitId, date, value);
        return habitLogDao.save(toSave);
    }

    public Habit getHabit(int habitId) throws SQLException {
        Habit habit = habitDao.findById(habitId);
        if (habit == null) {
            throw new IllegalArgumentException("No habit found with id " + habitId);
        }
        habitLogDao.findByHabitId(habitId).forEach(habit::addLogEntry);
        return habit;
    }

    public StreakCalculator streakCalculatorFor(Habit habit) {
        return StreakCalculatorFactory.forHabit(habit);
    }
}
