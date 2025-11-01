package com.example.qrkodlayoklama.ui.course;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.UserDto;
import com.example.qrkodlayoklama.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class StudentPickerActivity extends BaseActivity {

    public static final String EXTRA_DRAFT_NAME = "draft_name";
    public static final String EXTRA_DRAFT_CODE = "draft_code";
    public static final String EXTRA_PRESELECTED_IDS = "preselected_ids";
    public static final String RESULT_SELECTED_IDS = "result_selected_ids";
    public static final String RESULT_SELECTED_NAMES = "result_selected_names";
    public static final String RESULT_SELECTED_USERNAMES = "result_selected_userNames";

    private final ArrayList<Long> selected = new ArrayList<>();

    private RecyclerView recycler;
    private ProgressBar progress;
    private Button btnOk, btnCancel;
    private StudentCheckAdapter adapter;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_picker);
        setupToolbar("Öğrenci Seç", true);

        recycler = findViewById(R.id.recycler);
        progress = findViewById(R.id.progress);
        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentCheckAdapter();
        recycler.setAdapter(adapter);

        long[] pre = getIntent().getLongArrayExtra(EXTRA_PRESELECTED_IDS);
        selected.clear();
        if (pre != null) {
            adapter.setPreselected(pre);
            for (long id : pre) selected.add(id);
        }

        btnCancel.setOnClickListener(v -> finish());

        btnOk.setOnClickListener(v -> {
            List<UserDto> sel = adapter.getSelectedUsers();
            long[] ids;
            String[] names;
            long[] userNames;

            if (sel != null && !sel.isEmpty()) {
                ids = new long[sel.size()];
                names = new String[sel.size()];
                userNames = new long[sel.size()];
                for (int i = 0; i < sel.size(); i++) {
                    UserDto u = sel.get(i);
                    ids[i]   = u.getId();
                    names[i] = (u.getName() != null ? u.getName() : "")
                            + " "
                            + (u.getSurname() != null ? u.getSurname() : "");
                    userNames[i] = u.getUserName();
                }
            } else {
                ids = new long[0];
                names = new String[0];
                userNames = new long[0];
            }

            Intent data = new Intent();
            data.putExtra(RESULT_SELECTED_IDS, ids);
            data.putExtra(RESULT_SELECTED_NAMES, names);
            data.putExtra(RESULT_SELECTED_USERNAMES, userNames);
            setResult(RESULT_OK, data);
            finish();
        });

        loadAllStudents();
    }

    private void loadAllStudents() {
        setLoading(true);
        ApiClient.users().allStudents().enqueue(new retrofit2.Callback<List<UserDto>>() {
            @Override public void onResponse(
                    retrofit2.Call<List<UserDto>> call,
                    retrofit2.Response<List<UserDto>> resp) {

                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    adapter.setItems(resp.body());
                } else {
                    Toast.makeText(StudentPickerActivity.this,
                            "Hata: " + resp.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(
                    retrofit2.Call<List<UserDto>> call,
                    Throwable t) {
                setLoading(false);
                Toast.makeText(StudentPickerActivity.this,
                        "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean b) {
        if (progress != null) progress.setVisibility(b ? View.VISIBLE : View.GONE);
        if (recycler != null) recycler.setAlpha(b ? 0.4f : 1f);
        if (btnOk != null) btnOk.setEnabled(!b);
        if (btnCancel != null) btnCancel.setEnabled(!b);
    }
}
