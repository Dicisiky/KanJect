package com.example.proiect_final;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String PROJECTS_FILE = "projects.txt";
    List<Project> projectList = new ArrayList<>();
    ProjectAdapter projectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        loadProjects();

        Button newProjectButton = findViewById(R.id.btn_new_project);
        RecyclerView recyclerProjects = findViewById(R.id.recycler_projects);

        projectAdapter = new ProjectAdapter(projectList, new ProjectAdapter.OnProjectClickListener() {
            @Override
            public void onProjectClick(Project project) {
                openProject(project);
            }

            @Override
            public void onDeleteClick(Project project) {
                confirmDeleteProject(project);
            }
        });

        recyclerProjects.setLayoutManager(new LinearLayoutManager(this));
        recyclerProjects.setAdapter(projectAdapter);

        newProjectButton.setOnClickListener(v -> showAddProjectDialog());
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveProjects();
    }

    private void openProject(Project project) {
        Intent intent = new Intent(MainActivity.this, ProjectActivity.class);
        intent.putExtra("projectName", project.name);
        startActivity(intent);
    }

    private void confirmDeleteProject(Project project) {
        new AlertDialog.Builder(this)
                .setTitle("Șterge proiect")
                .setMessage("Sigur dorești să ștergi proiectul \"" + project.name + "\"? Toate sarcinile vor fi pierdute.")
                .setPositiveButton("Șterge", (dialog, which) -> {
                    deleteProjectFile(project.name);
                    int position = projectList.indexOf(project);
                    if (position != -1) {
                        projectList.remove(position);
                        projectAdapter.notifyItemRemoved(position);
                        saveProjects();
                        Toast.makeText(this, "Proiect șters", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Anulează", null)
                .show();
    }

    private void deleteProjectFile(String projectName) {
        File file = new File(getFilesDir(), "tasks_" + projectName + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    private void showAddProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adaugă Proiect");
        final EditText input = new EditText(this);
        input.setHint("Numele proiectului");
        builder.setView(input);
        builder.setPositiveButton("Adaugă", (dialog, which) -> {
            String projectName = input.getText().toString().trim();
            if (!projectName.isEmpty()) {
                projectList.add(new Project(projectName));
                projectAdapter.notifyItemInserted(projectList.size() - 1);
                saveProjects();
                Toast.makeText(this, "Proiect adăugat: " + projectName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Numele proiectului nu poate fi gol!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Anulează", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveProjects() {
        try {
            File file = new File(getFilesDir(), PROJECTS_FILE);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            for (Project project : projectList) {
                writer.write(project.name);
                writer.newLine();
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProjects() {
        try {
            File file = new File(getFilesDir(), PROJECTS_FILE);
            if (!file.exists()) return;

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String projectName;

            while ((projectName = reader.readLine()) != null) {
                if (!projectName.isEmpty()) {
                    projectList.add(new Project(projectName));
                }
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}