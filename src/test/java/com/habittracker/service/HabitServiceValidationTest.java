package com.habittracker.service;

import com.habittracker.exception.InvalidHabitFrequencyException;
import com.habittracker.model.Habit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Note: these tests only exercise the validation logic that runs before
 * any database call, so they don't need a live database connection.
 * Passing a null HabitDao/HabitLogDao is safe here because validation
 * failures throw before either DAO is ever touched.
 */
class HabitServiceValidationTest {

    private final HabitService habitService = new HabitService(null, null);

    @Test
    void countableHabitWithZeroTarget_throwsInvalidHabitFrequencyException() {
        assertThrows(InvalidHabitFrequencyException.class, () ->
                habitService.createHabit(1, "Push-ups", Habit.Frequency.DAILY,
                        HabitService.HabitType.COUNTABLE, 0));
    }

    @Test
    void timedHabitWithNegativeTarget_throwsInvalidHabitFrequencyException() {
        assertThrows(InvalidHabitFrequencyException.class, () ->
                habitService.createHabit(1, "Exercise", Habit.Frequency.DAILY,
                        HabitService.HabitType.TIMED, -10));
    }
}
