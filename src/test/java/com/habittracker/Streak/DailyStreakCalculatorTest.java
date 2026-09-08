package com.habittracker.streak;

import com.habittracker.model.BooleanHabit;
import com.habittracker.model.Habit;
import com.habittracker.model.LogEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DailyStreakCalculatorTest {

    private final StreakCalculator calculator = new DailyStreakCalculator();

    @Test
    void noLogEntries_streaksAreZero() {
        Habit habit = new BooleanHabit(1, 1, "Meditate", Habit.Frequency.DAILY, LocalDate.now());

        assertEquals(0, calculator.calculateCurrentStreak(habit));
        assertEquals(0, calculator.calculateLongestStreak(habit));
    }

    @Test
    void threeConsecutiveDays_currentStreakIsThree() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        Habit habit = new BooleanHabit(1, 1, "Meditate", Habit.Frequency.DAILY, start);

        habit.addLogEntry(new LogEntry(1, 1, start, 1.0));
        habit.addLogEntry(new LogEntry(2, 1, start.plusDays(1), 1.0));
        habit.addLogEntry(new LogEntry(3, 1, start.plusDays(2), 1.0));

        assertEquals(3, calculator.calculateCurrentStreak(habit));
        assertEquals(3, calculator.calculateLongestStreak(habit));
    }

    @Test
    void gapInLogs_breaksCurrentStreakButKeepsLongest() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        Habit habit = new BooleanHabit(1, 1, "Meditate", Habit.Frequency.DAILY, start);

        // A 3-day streak, then a gap, then a single day logged today.
        habit.addLogEntry(new LogEntry(1, 1, start, 1.0));
        habit.addLogEntry(new LogEntry(2, 1, start.plusDays(1), 1.0));
        habit.addLogEntry(new LogEntry(3, 1, start.plusDays(2), 1.0));
        habit.addLogEntry(new LogEntry(4, 1, start.plusDays(10), 1.0));

        assertEquals(1, calculator.calculateCurrentStreak(habit));
        assertEquals(3, calculator.calculateLongestStreak(habit));
    }

    @Test
    void incompleteEntry_doesNotCountTowardStreak() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        Habit habit = new BooleanHabit(1, 1, "Meditate", Habit.Frequency.DAILY, start);

        habit.addLogEntry(new LogEntry(1, 1, start, 0.0)); // not completed (value < 1.0)

        assertEquals(0, calculator.calculateCurrentStreak(habit));
    }
}
