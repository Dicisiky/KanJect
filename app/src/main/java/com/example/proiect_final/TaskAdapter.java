package com.example.proiect_final;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private List<Task> taskList;
    private List<Task> todoList;
    private List<Task> inProgressList;
    private List<Task> doneList;
    private String currentList;
    public TaskAdapter(List<Task> tasks, List<Task> todoList, List<Task> inProgressList, List<Task> doneList, String currentList) {
        this.taskList = tasks;
        this.todoList = todoList;
        this.inProgressList = inProgressList;
        this.doneList = doneList;
        this.currentList = currentList;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleView, deadlineView;
        public ViewHolder(View v) {
            super(v);
            titleView = v.findViewById(R.id.task_title);
            deadlineView = v.findViewById(R.id.task_deadline);
        }
    }
    @Override
    public TaskAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_card, parent, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.titleView.setText(task.title);
        holder.deadlineView.setText(task.deadline);
        holder.itemView.setOnClickListener(v -> showEditDialog(v, task, position));
    }
    @Override
    public int getItemCount() {
        return taskList.size();
    }
    public void saveTasks() {

    }
    private void notifyListChanged(List<Task> targetList, View view) {
        RecyclerView recyclerView = null;
        if (view.getContext() instanceof ProjectActivity) {
            ProjectActivity activity = (ProjectActivity) view.getContext();
            if (targetList == activity.getTodoList()) {
                recyclerView = activity.findViewById(R.id.recycler_todo);
            } else if (targetList == activity.getInProgressList()) {
                recyclerView = activity.findViewById(R.id.recycler_inprogress);
            } else if (targetList == activity.getDoneList()) {
                recyclerView = activity.findViewById(R.id.recycler_done);
            }
        }
        if (recyclerView != null && recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }
    private void showEditDialog(View view, Task task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_add_task, null);
        EditText inputTitle = dialogView.findViewById(R.id.input_title);
        EditText inputDescription = dialogView.findViewById(R.id.input_desc);
        EditText inputDeadline = dialogView.findViewById(R.id.input_deadline);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinner_status);
        spinnerStatus.setVisibility(View.VISIBLE); // Afișăm doar la editare
        inputTitle.setText(task.title);
        inputDescription.setText(task.description);
        inputDeadline.setText(task.deadline);
        inputDeadline.setOnClickListener(v1 -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(),
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedYear + "-" +
                                String.format("%02d", selectedMonth + 1) + "-" +
                                String.format("%02d", selectedDay);
                        inputDeadline.setText(date);
                    }, year, month, day);
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });

        ArrayAdapter<String> statusAdapter;
        switch (currentList) {
            case "To Do":
                statusAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, new String[]{"In Progress", "Done"});
                break;
            case "In Progress":
                statusAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, new String[]{"To Do", "Done"});
                break;
            case "Done":
                statusAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, new String[]{"To Do", "In Progress"});
                break;
            default:
                statusAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, new String[]{});
        }
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
        Button btnReminder = dialogView.findViewById(R.id.btn_reminder);
        btnReminder.setVisibility(View.VISIBLE);
        btnReminder.setOnClickListener(v -> {
            String deadline = inputDeadline.getText().toString().trim();
            String title = inputTitle.getText().toString().trim();
            if (deadline.isEmpty() || title.isEmpty()) {
                Toast.makeText(view.getContext(), "Completează titlul și deadline-ul!", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(deadline);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra(CalendarContract.Events.TITLE, title);
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.getTimeInMillis() + 60 * 60 * 1000);
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "Task reminder");
                intent.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                view.getContext().startActivity(intent);
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(view.getContext(), "Dată invalidă!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setView(dialogView);
        builder.setPositiveButton("Salvează", (dialog, which) -> {
            String newTitle = inputTitle.getText().toString().trim();
            String newDesc = inputDescription.getText().toString().trim();
            String newDeadline = inputDeadline.getText().toString().trim();
            String newStatus = spinnerStatus.getSelectedItem().toString();
            task.title = newTitle;
            task.description = newDesc;
            task.deadline = newDeadline;
            if (!newStatus.equals(task.status)) {

                taskList.remove(position);
                notifyItemRemoved(position);

                task.status = newStatus;

                if (newStatus.equals("To Do")) {
                    todoList.add(task);
                } else if (newStatus.equals("In Progress")) {
                    inProgressList.add(task);
                } else if (newStatus.equals("Done")) {
                    doneList.add(task);
                }

                if (newStatus.equals("To Do")) {
                    notifyListChanged(todoList, view);
                } else if (newStatus.equals("In Progress")) {
                    notifyListChanged(inProgressList, view);
                } else if (newStatus.equals("Done")) {
                    notifyListChanged(doneList, view);
                }

            } else {
                notifyItemChanged(position);
            }

            if (view.getContext() instanceof ProjectActivity) {
                ((ProjectActivity) view.getContext()).saveTasks();
            }
        });
        builder.setNegativeButton("Anulează", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}