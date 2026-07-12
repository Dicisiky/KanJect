package com.example.proiect_final;

public class Task {
    public String title;
    public String description;
    public String deadline;
    public String status;
    public Task(String title, String description, String deadline, String status) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.status = status;
    }
    @Override
    public String toString() {
        return title + "|" + description + "|" + deadline + "|" + status;
    }

    public static Task fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 4) {
            return new Task(parts[0], parts[1], parts[2], parts[3]);
        } else {
            return new Task("Invalid", "Invalid", "Invalid", "To Do");
        }
    }
}