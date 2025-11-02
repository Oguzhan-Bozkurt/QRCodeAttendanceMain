package com.example.qrkodlayoklama.ui.attendance;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.model.SessionHistoryDto;
import com.example.qrkodlayoklama.util.DateFormat;

import java.util.ArrayList;
import java.util.List;

public class AttendanceHistoryAdapter extends RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder> {

    private final long courseId;
    private final String courseName;

    private List<SessionHistoryDto> items = new ArrayList<>();

    public AttendanceHistoryAdapter(long courseId, String courseName) {
        this.courseId = courseId;
        this.courseName = courseName;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        SessionHistoryDto s = items.get(position);

        String title = s.getDescription();
        if (s.getCount() != 0) title += " • " + s.getCount() + " kişi";
        h.tvTitle.setText(title);

        String created = DateFormat.any(s.getCreatedAt() != null ? s.getCreatedAt() : "-");
        String expires = DateFormat.any(s.getExpiresAt() != null ? s.getExpiresAt() : "-");
        String status  = Boolean.TRUE.equals(s.isActive()) ? "Aktif" : "Pasif";
        h.tvSubtitle.setText("Başlangıç: " + created + " \nBitiş: " + expires + " \nDurum: " + status);

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), AttendanceSessionDetailActivity.class);
            i.putExtra(AttendanceSessionDetailActivity.EXTRA_COURSE_ID, courseId);
            i.putExtra(AttendanceSessionDetailActivity.EXTRA_SESSION_ID, s.getId());
            i.putExtra(AttendanceSessionDetailActivity.EXTRA_COURSE_NAME, courseName);
            v.getContext().startActivity(i);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    public void setItems(List<SessionHistoryDto> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ViewHolder(View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
        }
    }

}
