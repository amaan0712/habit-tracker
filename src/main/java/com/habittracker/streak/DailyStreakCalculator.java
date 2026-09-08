package com.habittracker.streak;

/**
 * Streak strategy for daily habits: two log dates are "consecutive" if they are 1 day apart.
 */
public class DailyStreakCalculator extends AbstractStreakCalculator {

    @Override
    protected int stepDays() {
        return 1;
    }
}
