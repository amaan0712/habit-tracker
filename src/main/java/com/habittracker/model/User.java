package com.habittracker.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user of the habit tracker. Each user owns a list of habits.
 */
public class User {

    private final int id;
    private String username;
    private final List<Habit> habits = new ArrayList<>();

    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Habit> getHabits() {
        return habits;
    }

    public void addHabit(Habit habit) {
        habits.add(habit);
    }

    @Override
    public String toString() {
        return username + " (id=" + id + ")";
    }
}
