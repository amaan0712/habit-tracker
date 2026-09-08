package com.habittracker.service;

import com.habittracker.model.Habit;
import com.habittracker.model.User;
import com.habittracker.streak.StreakCalculatorFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds a leaderboard ranking users by their average habit completion rate
 * across all the habits they own. Demonstrates Stream-based aggregation
 * and sorting across a collection of users.
 */
public class LeaderboardService {

    /**
     * A single row of the leaderboard.
     */
    public record LeaderboardEntry(User user, double averageCompletionRate, int totalCurrentStreak) {
    }

    /**
     * Builds a leaderboard sorted by average completion rate (descending),
     * then by total current streak (descending) as a tiebreaker.
     */
    public List<LeaderboardEntry> buildLeaderboard(Collection<User> users) {
        return users.stream()
                .map(this::toEntry)
                .sorted(Comparator.comparingDouble(LeaderboardEntry::averageCompletionRate).reversed()
                        .thenComparing(Comparator.comparingInt(LeaderboardEntry::totalCurrentStreak).reversed()))
                .collect(Collectors.toList());
    }

    private LeaderboardEntry toEntry(User user) {
        List<Habit> habits = user.getHabits();

        double averageCompletion = habits.stream()
                .mapToDouble(h -> StreakCalculatorFactory.forHabit(h).calculateCompletionRate(h))
                .average()
                .orElse(0.0);

        int totalCurrentStreak = habits.stream()
                .mapToInt(h -> StreakCalculatorFactory.forHabit(h).calculateCurrentStreak(h))
                .sum();

        return new LeaderboardEntry(user, averageCompletion, totalCurrentStreak);
    }
}
