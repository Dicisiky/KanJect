package com.example.proiect_final;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {
    public interface OnProjectClickListener {
        void onProjectClick(Project project);
        void onDeleteClick(Project project);
    }

    private final List<Project> projectList;
    private final OnProjectClickListener listener;

    public ProjectAdapter(List<Project> projectList, OnProjectClickListener listener) {
        this.projectList = projectList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.projectName.setText(project.name);

        holder.itemView.setOnClickListener(v ->
                listener.onProjectClick(project));

        holder.deleteButton.setOnClickListener(v ->
                listener.onDeleteClick(project));
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView projectName;
        public ImageView deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            projectName = itemView.findViewById(R.id.project_name);
            deleteButton = itemView.findViewById(R.id.btn_delete_project);
        }
    }
}