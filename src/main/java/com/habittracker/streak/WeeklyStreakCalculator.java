package com.habittracker.streak;

/**
 * Streak strategy for weekly habits: two log dates are "consecutive" if they are 7 days apart.
 */
public class WeeklyStreakCalculator extends AbstractStreakCalculator {

    @Override
    protected int stepDays() {
        return 7;
    }
}
