package com.example.proiect_final;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.provider.CalendarContract;
import android.widget.Button;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProjectActivity extends AppCompatActivity {
    private RecyclerView recyclerTodo, recyclerInProgress, recyclerDone;
    private TaskAdapter todoAdapter, inProgressAdapter, doneAdapter;
    private List<Task> todoList, inProgressList, doneList;
    private String projectName;
    public List<Task> getTodoList() {
        return todoList;
    }
    public List<Task> getInProgressList() {
        return inProgressList;
    }
    public List<Task> getDoneList() {
        return doneList;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);


        projectName = getIntent().getStringExtra("projectName");;
        TextView projectTitle = findViewById(R.id.project_title);
        projectTitle.setText(projectName);
        setTitle("Kanban - " + projectName);
        recyclerTodo = findViewById(R.id.recycler_todo);
        recyclerInProgress = findViewById(R.id.recycler_inprogress);
        recyclerDone = findViewById(R.id.recycler_done);
        todoList = new ArrayList<>();
        inProgressList = new ArrayList<>();
        doneList = new ArrayList<>();
        loadTasks();
        todoAdapter = new TaskAdapter(todoList, todoList, inProgressList, doneList, "To Do");
        inProgressAdapter = new TaskAdapter(inProgressList, todoList, inProgressList, doneList, "In Progress");
        doneAdapter = new TaskAdapter(doneList, todoList, inProgressList, doneList, "Done");
        recyclerTodo.setLayoutManager(new LinearLayoutManager(this));
        recyclerTodo.setAdapter(todoAdapter);
        recyclerInProgress.setLayoutManager(new LinearLayoutManager(this));
        recyclerInProgress.setAdapter(inProgressAdapter);
        recyclerDone.setLayoutManager(new LinearLayoutManager(this));
        recyclerDone.setAdapter(doneAdapter);
        FloatingActionButton fab = findViewById(R.id.fab_add_task);
        fab.setOnClickListener(view -> showAddTaskDialog());
    }
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        EditText inputTitle = viewInflated.findViewById(R.id.input_title);
        EditText inputDescription = viewInflated.findViewById(R.id.input_desc);
        EditText inputDeadline = viewInflated.findViewById(R.id.input_deadline);
        inputDeadline.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_CALENDAR},
                        1001);
                return;
            }
            DatePickerDialog datePickerDialog = new DatePickerDialog(ProjectActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedYear + "-" +
                                String.format("%02d", selectedMonth + 1) + "-" +
                                String.format("%02d", selectedDay);
                        inputDeadline.setText(date);
                    }, year, month, day);
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });
        Button btnReminder = viewInflated.findViewById(R.id.btn_reminder);
        btnReminder.setOnClickListener(v -> {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR},1);
            String deadline = inputDeadline.getText().toString().trim();
            String title = inputTitle.getText().toString().trim();
            if (deadline.isEmpty() || title.isEmpty()) {
                Toast.makeText(this, "Completează titlul și deadline-ul!", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(deadline);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setData(CalendarContract.Events.CONTENT_URI);
                intent.putExtra(CalendarContract.Events.TITLE, title);
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.getTimeInMillis() + 60 * 60 * 1000);
                intent.putExtra(CalendarContract.Events.DESCRIPTION, "Reminder pentru task: " + title);
                intent.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Eroare la setarea reminderului!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setView(viewInflated);
        builder.setPositiveButton("Adaugă", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            String description = inputDescription.getText().toString().trim();
            String deadline = inputDeadline.getText().toString().trim();
            String status = "To Do";
            boolean duplicate = false;
            for (Task t : todoList) {
                if (t.title.equalsIgnoreCase(title)) {
                    duplicate = true;
                    break;
                }
            }
            if (title.isEmpty()) {
                Toast.makeText(this, "Titlul este obligatoriu!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (duplicate) {
                Toast.makeText(this, "Există deja un task cu acest titlu în To Do", Toast.LENGTH_LONG).show();
                return;
            }
            Task newTask = new Task(title, description, deadline, status);
            todoList.add(newTask);
            todoAdapter.notifyItemInserted(todoList.size() - 1);
            saveTasks();
        });
        builder.setNegativeButton("Anulează", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisiune acordată. Apasă din nou pentru a seta reminderul.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permisiune refuzată. Nu se poate seta reminderul în calendar.", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void loadTasks() {
        try {
            File file = new File(getFilesDir(), "tasks_" + projectName + ".txt");
            if (!file.exists()) return;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                Task task = Task.fromString(line);
                if (task.status.equals("To Do")) {
                    todoList.add(task);
                } else if (task.status.equals("In Progress")) {
                    inProgressList.add(task);
                } else if (task.status.equals("Done")) {
                    doneList.add(task);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void saveTasks() {
        try {
            File file = new File(getFilesDir(), "tasks_" + projectName + ".txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (Task t : todoList) {
                writer.write(t.toString());
                writer.newLine();
            }
            for (Task t : inProgressList) {
                writer.write(t.toString());
                writer.newLine();
            }
            for (Task t : doneList) {
                writer.write(t.toString());
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        saveTasks();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveTasks();
    }

}