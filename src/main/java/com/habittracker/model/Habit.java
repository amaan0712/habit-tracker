package com.habittracker.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class representing a habit a user is tracking.
 * Subclasses define what "completing" the habit on a given day means.
 */
public abstract class Habit {

    public enum Frequency {
        DAILY, WEEKLY
    }

    private final int id;
    private final int userId;
    private String name;
    private Frequency frequency;
    private final LocalDate createdDate;
    private final List<LogEntry> logEntries = new ArrayList<>();

    protected Habit(int id, int userId, String name, Frequency frequency, LocalDate createdDate) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.frequency = frequency;
        this.createdDate = createdDate;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void addLogEntry(LogEntry entry) {
        logEntries.add(entry);
    }

    /**
     * Each habit type decides for itself, based on a log entry's raw value,
     * whether that entry counts as "completed" for the day.
     */
    public abstract boolean isCompletedOn(LogEntry entry);

    /**
     * Short human-readable description of what counts as success,
     * used in CLI prompts and reports.
     */
    public abstract String getCompletionCriteria();

    @Override
    public String toString() {
        return String.format("[%d] %s (%s, %s)", id, name, getClass().getSimpleName(), frequency);
    }
}
