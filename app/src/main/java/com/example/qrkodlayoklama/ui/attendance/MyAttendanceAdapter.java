package com.example.qrkodlayoklama.ui.attendance;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.model.MyAttendanceSummaryDto;

import java.util.ArrayList;
import java.util.List;

public class MyAttendanceAdapter extends RecyclerView.Adapter<MyAttendanceAdapter.ViewHolder> {

    private List<MyAttendanceSummaryDto> items = new ArrayList<>();

    public void setItems(List<MyAttendanceSummaryDto> items) {
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
        MyAttendanceSummaryDto item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSummary;
        int defaultSummaryColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSummary = itemView.findViewById(R.id.tvSummary);
            defaultSummaryColor = tvSummary.getCurrentTextColor();
        }

        public void bind(MyAttendanceSummaryDto item) {
            String titleText = item.getCourseName() + " (" + item.getCourseCode() + ")";
            tvTitle.setText(titleText);

            int total = item.getTotalSessions();
            int attended = item.getAttendedSessions();

            String summaryText = "Oturum sayısı: " + total + "   Katıldığı: " + attended;
            tvSummary.setText(summaryText);

            double percentage = 0;
            if (total > 0) {
                percentage = ((double) attended / total) * 100;
            }

            if (total > 0 && percentage < 70) {
                tvSummary.setTextColor(Color.RED);
            } else {
                tvSummary.setTextColor(defaultSummaryColor);
            }
        }
    }
}
