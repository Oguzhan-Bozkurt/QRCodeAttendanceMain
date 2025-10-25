package com.example.qrkodlayoklama.ui.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.model.AttendanceRecordDto;
import com.example.qrkodlayoklama.util.DateFormat;

import java.util.ArrayList;
import java.util.List;

public class SessionRecordsAdapter extends RecyclerView.Adapter<SessionRecordsAdapter.VH> {
    private List<AttendanceRecordDto> items = new ArrayList<>();

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_record, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        AttendanceRecordDto r = items.get(position);
        h.tvStudentInfo.setText(r.getName() + " " + r.getSurname() + " - " + r.getUserName());
        h.tvTime.setText(DateFormat.any(String.valueOf(r.getCheckedAt())));
    }

    @Override public int getItemCount() { return items.size(); }

    public void setItems(List<AttendanceRecordDto> list) {
        items = (list != null) ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvStudentInfo, tvTime;
        VH(View itemView) {
            super(itemView);
            tvStudentInfo = itemView.findViewById(R.id.tvStudentInfo);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}

