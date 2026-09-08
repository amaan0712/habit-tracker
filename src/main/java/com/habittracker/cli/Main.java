package com.habittracker.cli;

import com.habittracker.dao.DatabaseConnection;
import com.habittracker.dao.HabitDao;
import com.habittracker.dao.HabitLogDao;
import com.habittracker.dao.UserDao;
import com.habittracker.concurrency.ReminderScheduler;
import com.habittracker.exception.DuplicateLogEntryException;
import com.habittracker.exception.InvalidHabitFrequencyException;
import com.habittracker.io.ReportExporter;
import com.habittracker.model.Habit;
import com.habittracker.model.LogEntry;
import com.habittracker.model.User;
import com.habittracker.service.HabitService;
import com.habittracker.service.LeaderboardService;
import com.habittracker.streak.StreakCalculator;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line entry point for the Habit Tracker. Wires together all DAOs,
 * services, and the background reminder thread, then runs a menu loop.
 */
public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);

    private static UserDao userDao;
    private static HabitDao habitDao;
    private static HabitLogDao habitLogDao;
    private static HabitService habitService;
    private static LeaderboardService leaderboardService;
    private static ReportExporter reportExporter;
    private static ReminderScheduler reminderScheduler;

    private static User currentUser;

    public static void main(String[] args) {
        try {
            DatabaseConnection.getConnection(); // also creates schema on first run
        } catch (SQLException e) {
            System.err.println("Could not connect to the database: " + e.getMessage());
            return;
        }

        userDao = new UserDao();
        habitDao = new HabitDao();
        habitLogDao = new HabitLogDao();
        habitService = new HabitService(habitDao, habitLogDao);
        leaderboardService = new LeaderboardService();
        reportExporter = new ReportExporter();

        // Interval is in seconds. 30 was too aggressive for interactive use (it kept
        // interrupting menu prompts). 300 (5 min) is calmer for a live demo; a real
        // deployment would use 86400 (24 hours) since this represents a daily check.
        long reminderIntervalSeconds = 300;
        reminderScheduler = new ReminderScheduler(habitDao, habitLogDao);
        reminderScheduler.start(reminderIntervalSeconds);

        System.out.println("=== Habit Tracker ===");
        loginOrCreateUser();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = SCANNER.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> createHabit();
                    case "2" -> logHabitEntry();
                    case "3" -> viewMyHabits();
                    case "4" -> viewLeaderboard();
                    case "5" -> exportReport();
                    case "6" -> running = false;
                    default -> System.out.println("Not a valid option, try again.");
                }
            } catch (InvalidHabitFrequencyException | DuplicateLogEntryException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }

        reminderScheduler.stop();
        DatabaseConnection.close();
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("Logged in as: " + currentUser.getUsername());
        System.out.println("1. Create a habit");
        System.out.println("2. Log a habit entry");
        System.out.println("3. View my habits & streaks");
        System.out.println("4. View leaderboard");
        System.out.println("5. Export report (CSV)");
        System.out.println("6. Exit");
        System.out.print("Choose an option: ");
    }

    private static void loginOrCreateUser() {
        System.out.print("Enter your username: ");
        String username = SCANNER.nextLine().trim();

        try {
            User existing = userDao.findByUsername(username);
            if (existing != null) {
                currentUser = existing;
                System.out.println("Welcome back, " + username + "!");
            } else {
                currentUser = userDao.save(username);
                System.out.println("New user created: " + username);
            }
        } catch (SQLException e) {
            System.err.println("Could not log in: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void createHabit() throws InvalidHabitFrequencyException, SQLException {
        System.out.print("Habit name: ");
        String name = SCANNER.nextLine().trim();

        System.out.print("Type (1=Boolean, 2=Countable, 3=Timed): ");
        int typeChoice = readInt();
        HabitService.HabitType type = switch (typeChoice) {
            case 2 -> HabitService.HabitType.COUNTABLE;
            case 3 -> HabitService.HabitType.TIMED;
            default -> HabitService.HabitType.BOOLEAN;
        };

        System.out.print("Frequency (1=Daily, 2=Weekly): ");
        int freqChoice = readInt();
        Habit.Frequency frequency = (freqChoice == 2) ? Habit.Frequency.WEEKLY : Habit.Frequency.DAILY;

        int targetValue = 0;
        if (type != HabitService.HabitType.BOOLEAN) {
            System.out.print("Target value (count or minutes): ");
            targetValue = readInt();
        }

        Habit habit = habitService.createHabit(currentUser.getId(), name, frequency, type, targetValue);
        System.out.println("Created: " + habit);
    }

    private static void logHabitEntry() throws DuplicateLogEntryException, SQLException {
        System.out.print("Habit id: ");
        int habitId = readInt();

        System.out.print("Date (YYYY-MM-DD, blank for today): ");
        String dateInput = SCANNER.nextLine().trim();
        LocalDate date;
        try {
            date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format, please use YYYY-MM-DD.");
            return;
        }

        System.out.print("Value (1 for done if boolean, or a count/minutes): ");
        double value = readDouble();

        LogEntry entry = habitService.logEntry(habitId, date, value);
        System.out.println("Logged: " + entry);
    }

    private static void viewMyHabits() throws SQLException {
        List<Habit> habits = habitDao.findByUserId(currentUser.getId());
        if (habits.isEmpty()) {
            System.out.println("You have no habits yet.");
            return;
        }

        for (Habit habit : habits) {
            habitLogDao.findByHabitId(habit.getId()).forEach(habit::addLogEntry);
            StreakCalculator calc = habitService.streakCalculatorFor(habit);
            System.out.printf("%s | current streak: %d | longest: %d | completion: %.1f%%%n",
                    habit, calc.calculateCurrentStreak(habit),
                    calc.calculateLongestStreak(habit),
                    calc.calculateCompletionRate(habit) * 100);
        }
    }

    private static void viewLeaderboard() throws SQLException {
        List<User> users = userDao.findAllWithHabitsAndLogs(habitDao, habitLogDao);
        List<LeaderboardService.LeaderboardEntry> board = leaderboardService.buildLeaderboard(users);

        int rank = 1;
        for (LeaderboardService.LeaderboardEntry entry : board) {
            System.out.printf("%d. %s - avg completion: %.1f%%, total current streak: %d%n",
                    rank++, entry.user().getUsername(),
                    entry.averageCompletionRate() * 100, entry.totalCurrentStreak());
        }
    }

    private static void exportReport() throws SQLException {
        List<Habit> myHabits = habitDao.findByUserId(currentUser.getId());
        myHabits.forEach(h -> {
            try {
                habitLogDao.findByHabitId(h.getId()).forEach(h::addLogEntry);
            } catch (SQLException e) {
                System.out.println("Warning: could not load logs for " + h.getName());
            }
        });

        try {
            reportExporter.exportHabitReport(Path.of("my_habit_report.csv"), myHabits);
            System.out.println("Exported to my_habit_report.csv");

            List<User> users = userDao.findAllWithHabitsAndLogs(habitDao, habitLogDao);
            List<LeaderboardService.LeaderboardEntry> board = leaderboardService.buildLeaderboard(users);
            reportExporter.exportLeaderboard(Path.of("leaderboard_report.csv"), board);
            System.out.println("Exported to leaderboard_report.csv");
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(SCANNER.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a whole number: ");
            }
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(SCANNER.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a number: ");
            }
        }
    }
}
