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

public class AttendanceRecordsAdapter extends RecyclerView.Adapter<AttendanceRecordsAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(AttendanceRecordDto item);
    }

    private final List<AttendanceRecordDto> items = new ArrayList<>();
    private OnItemClickListener listener;

    public void setItems(List<AttendanceRecordDto> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_record, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AttendanceRecordDto r = items.get(position);

        String userNo   = safe(String.valueOf(r.getUserName()));
        String name     = safe(r.getName());
        String surname  = safe(r.getSurname());
        String checked  = safe(r.getCheckedAt());

        String studentInfo = name + " " + surname + " (" + userNo + ")";
        h.tvStudentInfo.setText(studentInfo);
        h.tvTime.setText(DateFormat.any(checked));

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(r);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvStudentInfo, tvTime;
        VH(@NonNull View itemView) {
            super(itemView);
            tvStudentInfo = itemView.findViewById(R.id.tvStudentInfo);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
