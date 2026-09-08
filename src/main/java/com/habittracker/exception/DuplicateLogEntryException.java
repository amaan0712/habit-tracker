package com.habittracker.exception;

/**
 * Thrown when a user attempts to log an entry for a habit on a date
 * that already has a log entry recorded.
 */
public class DuplicateLogEntryException extends Exception {

    public DuplicateLogEntryException(String message) {
        super(message);
    }

    public DuplicateLogEntryException(String message, Throwable cause) {
        super(message, cause);
    }
}
