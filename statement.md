# Problem Statement

Many people try to build habits — exercising, reading, drinking enough
water, meditating — but lose motivation without visibility into their
progress. Simple checklist apps show whether a habit was done "today," but
rarely surface the underlying pattern: how long is my current streak, is my
consistency actually improving, and how do I compare to others trying to
build similar habits. Without that feedback, people underestimate small
lapses until a habit has quietly died, and lose the light social motivation
that comes from shared accountability.

This project builds a command-line habit tracker that logs daily/weekly
progress on user-defined habits, computes streak and completion-rate
analytics automatically, and ranks users on a shared leaderboard — turning
raw daily logs into the kind of feedback that actually sustains a habit.

## Scope

The project covers:

- Creating and managing habits of three types: simple done/not-done habits,
  countable habits (e.g. a daily step count), and timed habits (e.g. minutes
  exercised)
- Logging one entry per habit per day/week
- Computing current streak, longest streak, and completion rate per habit
- Ranking all users on a leaderboard by average completion rate
- Persisting all data locally via an embedded SQLite database (no server
  setup required)
- Exporting streak and leaderboard reports as CSV files
- A background reminder that flags un-logged habits for the day

Out of scope for this version: a graphical interface (this is a CLI-only
tool per the assignment's executability requirements), multi-device sync,
and push notifications (the reminder is a console message, not an OS-level
notification).

## Target Users

- Individuals trying to build or maintain personal habits who want visible
  streak tracking rather than a plain yes/no checklist
- Small groups of friends or classmates who want light social accountability
  through a shared leaderboard
- As an academic project, it also serves as a demonstration of core Java
  concepts: OOP design, exception handling, collections/streams, JDBC, and
  concurrency, applied to a single cohesive application

## High-Level Features

- **Habit creation** — choose a habit type (boolean, countable, or timed),
  a name, a frequency (daily or weekly), and a target value where relevant
- **Daily logging** — record a value for a habit on a given date, with
  duplicate-entry protection
- **Streak analytics** — current streak, longest streak ever achieved, and
  overall completion rate, computed via Stream pipelines over log history
- **Leaderboard** — all users ranked by average completion rate across their
  habits, with total current streak as a tiebreaker
- **CSV export** — both a personal habit report and the full leaderboard can
  be exported to CSV files using NIO.2 file APIs
- **Reminders** — a background thread periodically checks for habits not
  yet logged today and prints a reminder to the console
