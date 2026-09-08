package com.habittracker.concurrency;

import com.habittracker.dao.HabitDao;
import com.habittracker.dao.HabitLogDao;
import com.habittracker.model.Habit;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Runs a background task that periodically scans all habits and prints a
 * reminder for any that have not been logged yet today. In a real app this
 * would run once a day; here the interval is configurable so it can be
 * demonstrated quickly (e.g. every 30 seconds) without waiting 24 hours.
 *
 * Demonstrates Module 5's concurrency content: a dedicated background thread
 * managed through a ScheduledExecutorService rather than raw Thread.start(),
 * which is the standard, safer way to run recurring tasks in Java.
 */
public class ReminderScheduler {

    private final HabitDao habitDao;
    private final HabitLogDao habitLogDao;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "reminder-thread");
        thread.setDaemon(true); // daemon so it never blocks the CLI from exiting
        return thread;
    });

    public ReminderScheduler(HabitDao habitDao, HabitLogDao habitLogDao) {
        this.habitDao = habitDao;
        this.habitLogDao = habitLogDao;
    }

    /**
     * Starts the recurring reminder check.
     *
     * @param intervalSeconds how often to run the check (use a small number
     *                        like 30 for demoing; a real deployment would use
     *                        a much longer interval, e.g. once every 24 hours)
     */
    public void start(long intervalSeconds) {
        executor.scheduleAtFixedRate(this::checkAndRemind, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    private void checkAndRemind() {
        LocalDate today = LocalDate.now();
        try {
            List<Habit> allHabits = habitDao.findAll();
            for (Habit habit : allHabits) {
                boolean loggedToday = habitLogDao.existsForDate(habit.getId(), today);
                if (!loggedToday) {
                    // Leading newline keeps this from jamming into whatever prompt
                    // the CLI happens to be showing when this background check fires.
                    System.out.println("\n[Reminder] \"" + habit.getName()
                            + "\" has not been logged for " + today + " yet.");
                }
            }
        } catch (SQLException e) {
            // A background thread should never crash the app; log and move on.
            System.err.println("[Reminder] Skipped this check due to a database error: " + e.getMessage());
        }
    }

    /** Shuts the background thread down cleanly. Call this before the app exits. */
    public void stop() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
