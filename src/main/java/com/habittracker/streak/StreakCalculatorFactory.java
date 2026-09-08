package com.habittracker.streak;

import com.habittracker.model.Habit;

/**
 * Picks the correct StreakCalculator strategy for a habit based on its frequency.
 * Keeps the Strategy pattern's selection logic in one place.
 */
public final class StreakCalculatorFactory {

    private static final StreakCalculator DAILY = new DailyStreakCalculator();
    private static final StreakCalculator WEEKLY = new WeeklyStreakCalculator();

    private StreakCalculatorFactory() {
    }

    public static StreakCalculator forHabit(Habit habit) {
        return switch (habit.getFrequency()) {
            case DAILY -> DAILY;
            case WEEKLY -> WEEKLY;
        };
    }
}
