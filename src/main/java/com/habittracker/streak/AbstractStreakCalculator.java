package com.habittracker.streak;

import com.habittracker.model.Habit;
import com.habittracker.model.LogEntry;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shared streak-calculation logic. Subclasses only specify the "step" in days
 * that separates two consecutive completions (1 for daily habits, 7 for weekly).
 * This is the Strategy pattern: HabitService picks the right subclass based on
 * the habit's frequency, but callers only depend on the StreakCalculator interface.
 */
public abstract class AbstractStreakCalculator implements StreakCalculator {

    /** Number of days between two consecutive completions for this strategy. */
    protected abstract int stepDays();

    /**
     * Returns the distinct dates on which the habit was completed, sorted ascending.
     * Uses a Stream pipeline: filter completed entries -> extract dates -> dedupe -> sort.
     */
    private List<LocalDate> completedDatesSorted(Habit habit) {
        return habit.getLogEntries().stream()
                .filter(habit::isCompletedOn)
                .map(LogEntry::getLogDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public int calculateCurrentStreak(Habit habit) {
        List<LocalDate> dates = completedDatesSorted(habit);
        if (dates.isEmpty()) {
            return 0;
        }

        int step = stepDays();
        LocalDate cursor = dates.get(dates.size() - 1);
        int streak = 1;

        for (int i = dates.size() - 2; i >= 0; i--) {
            LocalDate candidate = dates.get(i);
            if (ChronoUnit.DAYS.between(candidate, cursor) == step) {
                streak++;
                cursor = candidate;
            } else {
                break;
            }
        }
        return streak;
    }

    @Override
    public int calculateLongestStreak(Habit habit) {
        List<LocalDate> dates = completedDatesSorted(habit);
        if (dates.isEmpty()) {
            return 0;
        }

        int step = stepDays();
        int longest = 1;
        int current = 1;

        for (int i = 1; i < dates.size(); i++) {
            long diff = ChronoUnit.DAYS.between(dates.get(i - 1), dates.get(i));
            if (diff == step) {
                current++;
            } else {
                current = 1;
            }
            longest = Math.max(longest, current);
        }
        return longest;
    }

    @Override
    public double calculateCompletionRate(Habit habit) {
        List<LocalDate> dates = completedDatesSorted(habit);
        LocalDate start = habit.getCreatedDate();
        LocalDate end = LocalDate.now();

        long elapsedDays = ChronoUnit.DAYS.between(start, end) + 1;
        long totalPeriods = Math.max(1, elapsedDays / stepDays());

        double rate = (double) dates.size() / totalPeriods;
        return Math.min(1.0, rate);
    }
}
