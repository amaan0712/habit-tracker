package com.habittracker.io;

import com.habittracker.model.Habit;
import com.habittracker.service.LeaderboardService;
import com.habittracker.streak.StreakCalculator;
import com.habittracker.streak.StreakCalculatorFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Exports streak and leaderboard reports to CSV files using java.nio.file
 * (NIO.2) rather than the older java.io.File API, per Module 3's I/O content.
 */
public class ReportExporter {

    /** Writes a per-habit streak report (one row per habit) to the given path. */
    public void exportHabitReport(Path path, List<Habit> habits) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Habit,Type,Frequency,CurrentStreak,LongestStreak,CompletionRatePct\n");

        for (Habit habit : habits) {
            StreakCalculator calc = StreakCalculatorFactory.forHabit(habit);
            int current = calc.calculateCurrentStreak(habit);
            int longest = calc.calculateLongestStreak(habit);
            double rate = calc.calculateCompletionRate(habit) * 100;

            sb.append(escapeCsv(habit.getName())).append(',')
              .append(habit.getClass().getSimpleName()).append(',')
              .append(habit.getFrequency()).append(',')
              .append(current).append(',')
              .append(longest).append(',')
              .append(String.format("%.1f", rate))
              .append('\n');
        }

        Files.writeString(path, sb.toString(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /** Writes the leaderboard (one row per user, ranked) to the given path. */
    public void exportLeaderboard(Path path, List<LeaderboardService.LeaderboardEntry> entries) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Rank,User,AverageCompletionRatePct,TotalCurrentStreak\n");

        int rank = 1;
        for (LeaderboardService.LeaderboardEntry entry : entries) {
            sb.append(rank++).append(',')
              .append(escapeCsv(entry.user().getUsername())).append(',')
              .append(String.format("%.1f", entry.averageCompletionRate() * 100)).append(',')
              .append(entry.totalCurrentStreak())
              .append('\n');
        }

        Files.writeString(path, sb.toString(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
