package com.example.qrkodlayoklama.ui.course;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.qrkodlayoklama.R;
import java.util.ArrayList;
import java.util.List;

public class SelectedStudentAdapter extends RecyclerView.Adapter<SelectedStudentAdapter.VH> {

    public static class StudentUi {
        public long id;
        public String name;
        public StudentUi(long id, String name) { this.id = id; this.name = name; }
    }

    private final List<StudentUi> items = new ArrayList<>();

    public interface OnRemoveListener { void onRemove(long id); }
    private OnRemoveListener onRemove;

    public void setOnRemoveListener(OnRemoveListener l) { this.onRemove = l; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_student, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        StudentUi s = items.get(position);
        ((TextView) h.itemView.findViewById(R.id.tvStudent))
                .setText(s.name + " (id: " + s.id + ")");
        h.itemView.setOnLongClickListener(v -> {
            items.remove(position);
            notifyDataSetChanged();
            return true;
        });
    }

    @Override public int getItemCount() { return items.size(); }

    public void setItems(List<StudentUi> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public long[] getIdArray() {
        long[] arr = new long[items.size()];
        for (int i = 0; i < items.size(); i++) {
            arr[i] = items.get(i).id;
        }
        return arr;
    }

    static class VH extends RecyclerView.ViewHolder {
        VH(@NonNull View itemView) { super(itemView); }
    }

    public void removeById(long id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == id) {
                items.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }
}
