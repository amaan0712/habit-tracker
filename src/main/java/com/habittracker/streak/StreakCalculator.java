package com.habittracker.streak;

import com.habittracker.model.Habit;

/**
 * Strategy interface for computing streaks on a habit.
 * Different implementations can define "consecutive" differently
 * depending on frequency (daily vs weekly) or habit type.
 */
public interface StreakCalculator {

    /**
     * The current active streak, counting backwards from today/most recent log.
     */
    int calculateCurrentStreak(Habit habit);

    /**
     * The longest streak ever achieved for this habit, from its full history.
     */
    int calculateLongestStreak(Habit habit);

    /**
     * Fraction of days (0.0 - 1.0) since the habit was created that were completed.
     */
    double calculateCompletionRate(Habit habit);
}
