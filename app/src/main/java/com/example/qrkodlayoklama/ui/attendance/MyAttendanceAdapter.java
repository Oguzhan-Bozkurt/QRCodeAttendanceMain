package com.example.qrkodlayoklama.ui.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.model.MyAttendanceDto;
import com.example.qrkodlayoklama.util.DateFormat;

import java.util.ArrayList;
import java.util.List;

public class MyAttendanceAdapter extends RecyclerView.Adapter<MyAttendanceAdapter.VH> {

    private final List<MyAttendanceDto> items = new ArrayList<>();

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_attendance, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        MyAttendanceDto it = items.get(position);
        h.tvCourse.setText(it.getCourseName() + " (" + it.getCourseCode() + ")" + " - " + it.getDescription());
        h.tvCheckedAt.setText("Yoklamaya katılma: " + DateFormat.any(it.getCheckedAt() != null ? it.getCheckedAt() : "-"));
    }

    @Override public int getItemCount() { return items.size(); }

    public void setItems(List<MyAttendanceDto> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCourse, tvCheckedAt;
        VH(@NonNull View itemView) {
            super(itemView);
            tvCourse    = itemView.findViewById(R.id.tvCourse);
            tvCheckedAt = itemView.findViewById(R.id.tvCheckedAt);
        }
    }
}
