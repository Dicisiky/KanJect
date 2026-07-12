package com.example.proiect_final;

import java.util.ArrayList;
import java.util.List;

public class Project {
    public String name;
    public List<Task> tasks;
    public Project(String name) {
        this.name = name;
        this.tasks = new ArrayList<>();

    }
}