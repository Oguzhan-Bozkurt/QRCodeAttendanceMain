package com.example.qrkodlayoklama.ui.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.model.MyAttendanceSummaryDto;

import java.util.ArrayList;
import java.util.List;

public class MyAttendanceAdapter extends RecyclerView.Adapter<MyAttendanceAdapter.VH> {
    private final List<MyAttendanceSummaryDto> items = new ArrayList<>();

    public void setItems(List<MyAttendanceSummaryDto> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_attendance, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MyAttendanceSummaryDto r = items.get(position);

        h.tvTitle.setText(r.getCourseName());
        h.tvCode.setText(r.getCourseCode());

        String summary = "Katılım: " + r.getAttendedSessions() + "/" + r.getTotalSessions();
        h.tvSummary.setText(summary);

        if (r.getTotalSessions() > 0) {
            int progress = (int) ((r.getAttendedSessions() * 100.0f) / r.getTotalSessions());
            h.progressAttendance.setProgress(progress);
        } else {
            h.progressAttendance.setProgress(0);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvCode, tvSummary;
        final ProgressBar progressAttendance;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvSummary = itemView.findViewById(R.id.tvSummary);
            progressAttendance = itemView.findViewById(R.id.progressAttendance);
        }
    }
}
