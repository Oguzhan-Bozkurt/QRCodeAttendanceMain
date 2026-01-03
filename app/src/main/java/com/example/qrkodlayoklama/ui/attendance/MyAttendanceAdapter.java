package com.example.qrkodlayoklama.ui.attendance;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;

import java.util.ArrayList;
import java.util.List;

public class MyAttendanceAdapter extends RecyclerView.Adapter<MyAttendanceAdapter.VH> {

    public static class SummaryItem {
        public final long courseId;
        public final String courseName;
        public final String courseCode;
        public int totalSessions = 0;
        public int attended = 0;

        public SummaryItem(long courseId, String courseName, String courseCode) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.courseCode = courseCode;
        }
    }

    private final List<SummaryItem> items = new ArrayList<>();

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_attendance, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SummaryItem it = items.get(position);

        String name = it.courseName != null ? it.courseName : "";
        String code = it.courseCode != null ? it.courseCode : "";
        h.tvTitle.setText(name + " (" + code + ")");

        h.tvSummary.setText("Oturum sayısı: " + it.totalSessions + "   Katıldığı: " + it.attended);

        if (it.totalSessions > 0) {
            double absenceRate = (double) (it.totalSessions - it.attended) / it.totalSessions;
            if (absenceRate > 0.7) {
                h.tvSummary.setTextColor(Color.RED);
            }
            else {
                h.tvSummary.setTextColor(h.defaultSummaryColor);
            }
        }
        else {
            h.tvSummary.setTextColor(h.defaultSummaryColor);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<SummaryItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSummary;
        final int defaultSummaryColor;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSummary = itemView.findViewById(R.id.tvSummary);
            defaultSummaryColor = tvSummary.getCurrentTextColor();
        }
    }
}
