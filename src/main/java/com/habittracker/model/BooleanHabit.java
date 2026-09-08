package com.habittracker.model;

import java.time.LocalDate;

/**
 * A habit that is either done or not done on a given day (e.g. "Meditate", "No smoking").
 * A logged value of 1.0 means done; 0.0 (or anything less than 1.0) means not done.
 */
public class BooleanHabit extends Habit {

    public BooleanHabit(int id, int userId, String name, Frequency frequency, LocalDate createdDate) {
        super(id, userId, name, frequency, createdDate);
    }

    @Override
    public boolean isCompletedOn(LogEntry entry) {
        return entry.getValue() >= 1.0;
    }

    @Override
    public String getCompletionCriteria() {
        return "Done (1) or not done (0)";
    }
}
