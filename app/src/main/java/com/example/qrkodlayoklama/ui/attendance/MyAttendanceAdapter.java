package com.example.qrkodlayoklama.ui.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyAttendanceAdapter extends RecyclerView.Adapter<MyAttendanceAdapter.ViewHolder> {

    private List<SummaryItem> items = new ArrayList<>();

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SummaryItem that = (SummaryItem) o;
            return courseId == that.courseId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(courseId);
        }
    }

    public void setItems(List<SummaryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SummaryItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSummary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSummary = itemView.findViewById(R.id.tvSummary);
        }

        public void bind(SummaryItem item) {
            String titleText = item.courseName + " (" + item.courseCode + ")";
            tvTitle.setText(titleText);

            int total = item.totalSessions;
            int attended = item.attended;

            String summaryText = "Oturum sayısı: " + total + "   Katıldığı: " + attended;
            tvSummary.setText(summaryText);

        }
    }
}
