package com.example.qrkodlayoklama.ui.course;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.model.UserDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudentCheckAdapter extends RecyclerView.Adapter<StudentCheckAdapter.VH> {

    private final List<UserDto> items = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();
    private final Set<Long> preselectedIds = new HashSet<>();

    public void setPreselected(long[] ids) {
        preselectedIds.clear();
        selectedIds.clear();
        if (ids != null) {
            for (long id : ids) preselectedIds.add(id);
        }
        notifyDataSetChanged();
    }

    public void setItems(List<UserDto> users) {
        items.clear();
        if (users != null) items.addAll(users);

        selectedIds.clear();
        selectedIds.addAll(preselectedIds);

        notifyDataSetChanged();
    }

    public List<UserDto> getSelectedUsers() {
        List<UserDto> out = new ArrayList<>();
        for (UserDto u : items) {
            if (u.getId() != null && selectedIds.contains(u.getId())) {
                out.add(u);
            }
        }
        return out;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_check, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        items.sort(Comparator.comparingLong(UserDto::getUserName));
        UserDto u = items.get(position);
        long id = u.getId() != null ? u.getId() : -1L;
        String fullName = (u.getName() != null ? u.getName() : "")
                + " " + (u.getSurname() != null ? u.getSurname() : "");
        long userName = u.getUserName() != null ? u.getUserName() : -1L;

        h.tvName.setText(fullName.trim() + " - " + userName);
        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(selectedIds.contains(id));

        View.OnClickListener toggle = v -> {
            boolean nowChecked = !h.cb.isChecked();
            h.cb.setChecked(nowChecked);
        };

        h.itemView.setOnClickListener(toggle);
        h.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (id < 0) return;
            if (isChecked) selectedIds.add(id);
            else selectedIds.remove(id);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox cb;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            cb = itemView.findViewById(R.id.cb);
        }
    }
}
