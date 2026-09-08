package com.habittracker.exception;

/**
 * Thrown when a habit is created or updated with an invalid frequency
 * or an invalid target value (e.g. negative or zero target count/minutes).
 */
public class InvalidHabitFrequencyException extends Exception {

    public InvalidHabitFrequencyException(String message) {
        super(message);
    }

    public InvalidHabitFrequencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
