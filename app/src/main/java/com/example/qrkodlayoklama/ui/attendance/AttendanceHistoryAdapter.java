package com.example.qrkodlayoklama.ui.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.model.SessionHistoryDto;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AttendanceHistoryAdapter extends RecyclerView.Adapter<AttendanceHistoryAdapter.VH> {

    private final List<SessionHistoryDto> items = new ArrayList<>();

    public void setItems(List<SessionHistoryDto> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SessionHistoryDto it = items.get(position);
        h.tvCreated.setText("Oluşturulma: " + fmtIso(it.getCreatedAt()));
        h.tvExpires.setText("Bitiş: " + fmtIso(it.getExpiresAt()));
        h.tvStatus.setText("Durum: " + (it.isActive() ? "Aktif" : "Kapandı"));
        h.tvCount.setText("   •   Katılan: " + it.getCount());
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCreated, tvExpires, tvStatus, tvCount;
        VH(@NonNull View v) {
            super(v);
            tvCreated = v.findViewById(R.id.tvCreated);
            tvExpires = v.findViewById(R.id.tvExpires);
            tvStatus  = v.findViewById(R.id.tvStatus);
            tvCount   = v.findViewById(R.id.tvCount);
        }
    }

    private static String fmtIso(String iso) {
        if (iso == null) return "-";
        String s = iso;
        int z = s.indexOf('Z');
        if (z > 0) {
            int dot = s.indexOf('.', s.indexOf('T'));
            if (dot > 0) {
                s = s.substring(0, Math.min(dot + 4, z)) + "Z"; // .SSS’e indir
            }
        }
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        parser.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date d = parser.parse(s, new ParsePosition(0));
        if (d == null) {
            parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            d = parser.parse(iso, new ParsePosition(0));
        }
        if (d == null) return iso;
        SimpleDateFormat out = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return out.format(d);
    }
}
