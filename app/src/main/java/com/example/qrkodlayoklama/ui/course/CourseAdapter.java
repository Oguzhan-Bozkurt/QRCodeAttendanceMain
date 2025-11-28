package com.example.qrkodlayoklama.ui.course;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.model.CourseDto;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.VH> {

    private final List<CourseDto> items = new ArrayList<>();

    public interface OnItemClickListener {
        void onCourseClick(CourseDto course);
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(CourseDto course);
    }
    public interface OnEditClickListener {
        void onEditClick(CourseDto course);
    }

    private OnItemClickListener itemClickListener;
    private OnDeleteClickListener deleteClickListener;
    private OnEditClickListener editClickListener;

    public void setItems(List<CourseDto> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.itemClickListener = l;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener l) {
        this.deleteClickListener = l;
    }

    public void setEditClickListener(OnEditClickListener l) {
        this.editClickListener = l;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CourseDto c = items.get(position);
        h.tvName.setText(c.getCourseName());
        h.tvCode.setText(c.getCourseCode());

        h.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) itemClickListener.onCourseClick(c);
        });

        h.btnEdit.setOnClickListener(v -> {
            if (editClickListener != null) editClickListener.onEditClick(c);
        });

        h.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) deleteClickListener.onDeleteClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvCode;
        ImageButton btnDelete, btnEdit;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCode = itemView.findViewById(R.id.tvCode);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
