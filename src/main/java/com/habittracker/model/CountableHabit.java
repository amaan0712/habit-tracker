package com.habittracker.model;

import java.time.LocalDate;

/**
 * A habit measured by a count that must reach a daily target
 * (e.g. "Drink 8 glasses of water", "Do 50 push-ups").
 */
public class CountableHabit extends Habit {

    private int targetCount;

    public CountableHabit(int id, int userId, String name, Frequency frequency,
                           LocalDate createdDate, int targetCount) {
        super(id, userId, name, frequency, createdDate);
        setTargetCount(targetCount);
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        if (targetCount <= 0) {
            throw new IllegalArgumentException("Target count must be positive");
        }
        this.targetCount = targetCount;
    }

    @Override
    public boolean isCompletedOn(LogEntry entry) {
        return entry.getValue() >= targetCount;
    }

    @Override
    public String getCompletionCriteria() {
        return "Reach a count of " + targetCount + " per day";
    }
}
