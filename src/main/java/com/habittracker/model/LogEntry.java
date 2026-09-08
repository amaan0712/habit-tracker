package com.habittracker.model;

import java.time.LocalDate;

/**
 * Represents a single day's logged value for a habit.
 * The meaning of "value" depends on the habit type:
 *  - BooleanHabit: 1.0 = done, 0.0 = not done
 *  - CountableHabit: the count achieved that day
 *  - TimedHabit: minutes spent that day
 */
public class LogEntry {

    private final int id;
    private final int habitId;
    private final LocalDate logDate;
    private final double value;

    public LogEntry(int id, int habitId, LocalDate logDate, double value) {
        this.id = id;
        this.habitId = habitId;
        this.logDate = logDate;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public int getHabitId() {
        return habitId;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return logDate + " -> " + value;
    }
}
