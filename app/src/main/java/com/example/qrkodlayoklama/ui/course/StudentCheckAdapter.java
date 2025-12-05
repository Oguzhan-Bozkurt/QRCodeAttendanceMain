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

public class StudentCheckAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM   = 1;

    private final List<UserDto> items = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();
    private final Set<Long> preselectedIds = new HashSet<>();

    public void setPreselected(long[] ids) {
        preselectedIds.clear();
        if (ids != null) for (long id : ids) preselectedIds.add(id);
        notifyDataSetChanged();
    }

    public void setItems(List<UserDto> users) {
        items.clear();
        if (users != null) items.addAll(users);

        items.sort(Comparator.comparingLong(u ->
                u.getUserName() != null ? u.getUserName() : Long.MAX_VALUE));

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

    @Override public int getItemViewType(int position) {
        return (position == 0) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_select_all, parent, false);
            return new HeaderVH(v);
        } else {
            View v = inf.inflate(R.layout.item_student_check, parent, false);
            return new ItemVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            bindHeader((HeaderVH) holder);
        } else {
            bindItem((ItemVH) holder, position - 1);
        }
    }

    private void bindHeader(@NonNull HeaderVH h) {
        boolean allSelectableHaveIds = true;
        List<Long> allIds = new ArrayList<>();
        for (UserDto u : items) {
            if (u.getId() == null) { allSelectableHaveIds = false; }
            else allIds.add(u.getId());
        }

        boolean allSelected = !items.isEmpty()
                && allSelectableHaveIds
                && selectedIds.size() == allIds.size();

        h.cbAll.setOnCheckedChangeListener(null);
        h.cbAll.setChecked(allSelected);
        h.title.setText(allSelected ? "Tümünü Kaldır" : "Tümünü Seç");

        View.OnClickListener toggle = v -> h.cbAll.setChecked(!h.cbAll.isChecked());
        h.itemView.setOnClickListener(toggle);

        h.cbAll.setOnCheckedChangeListener((button, isChecked) -> {
            toggleAll(isChecked);
        });
    }

    private void bindItem(@NonNull ItemVH h, int idx) {
        UserDto u = items.get(idx);
        long id = (u.getId() != null) ? u.getId() : -1L;
        String fullName = (u.getName() != null ? u.getName() : "")
                + " " + (u.getSurname() != null ? u.getSurname() : "");
        long userName = (u.getUserName() != null) ? u.getUserName() : -1L;

        h.tvName.setText(fullName.trim() + " - " + userName);

        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(selectedIds.contains(id));

        View.OnClickListener toggle = v -> h.cb.setChecked(!h.cb.isChecked());
        h.itemView.setOnClickListener(toggle);

        h.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (id < 0) return;
            if (isChecked) selectedIds.add(id);
            else selectedIds.remove(id);
            notifyItemChanged(0);
        });
    }

    private void toggleAll(boolean select) {
        selectedIds.clear();
        if (select) {
            for (UserDto u : items) {
                if (u.getId() != null) selectedIds.add(u.getId());
            }
        }
        notifyDataSetChanged();
    }

    @Override public int getItemCount() {
        return 1 + items.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView  title;
        CheckBox  cbAll;
        HeaderVH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            cbAll  = itemView.findViewById(R.id.cbAll);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox cb;
        ItemVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            cb     = itemView.findViewById(R.id.cb);
        }
    }
}
