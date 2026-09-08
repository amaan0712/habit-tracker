package com.habittracker.model;

import java.time.LocalDate;

/**
 * A habit measured by time spent, needing to reach a target duration each day
 * (e.g. "Exercise for 30 minutes", "Read for 20 minutes").
 */
public class TimedHabit extends Habit {

    private int targetMinutes;

    public TimedHabit(int id, int userId, String name, Frequency frequency,
                       LocalDate createdDate, int targetMinutes) {
        super(id, userId, name, frequency, createdDate);
        setTargetMinutes(targetMinutes);
    }

    public int getTargetMinutes() {
        return targetMinutes;
    }

    public void setTargetMinutes(int targetMinutes) {
        if (targetMinutes <= 0) {
            throw new IllegalArgumentException("Target minutes must be positive");
        }
        this.targetMinutes = targetMinutes;
    }

    @Override
    public boolean isCompletedOn(LogEntry entry) {
        return entry.getValue() >= targetMinutes;
    }

    @Override
    public String getCompletionCriteria() {
        return "Reach " + targetMinutes + " minutes per day";
    }
}
