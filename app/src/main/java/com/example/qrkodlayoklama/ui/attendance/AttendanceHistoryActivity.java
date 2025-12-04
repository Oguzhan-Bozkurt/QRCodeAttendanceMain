package com.example.qrkodlayoklama.ui.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.SessionHistoryDto;
import com.example.qrkodlayoklama.ui.BaseActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceHistoryActivity extends BaseActivity {

    public static final String EXTRA_COURSE_ID = "courseId";
    public static final String EXTRA_COURSE_NAME = "courseName";

    private RecyclerView recycler;
    private ProgressBar progress;
    private AttendanceHistoryAdapter adapter;
    private TextView empty;
    private long courseId;
    private String courseName;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        courseName = getIntent().getStringExtra(EXTRA_COURSE_NAME);
        if (courseId == -1) {
            Toast.makeText(this, "Ders bilgisi yok", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar(courseName, true);

        progress = findViewById(R.id.progress);
        recycler = findViewById(R.id.recyclerHistory);
        empty = findViewById(R.id.empty);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceHistoryAdapter(courseId, courseName);
        recycler.setAdapter(adapter);

        loadAttendanceHistory();
    }

    @Override protected void onResume() {
        super.onResume();
        loadAttendanceHistory();
    }

    private void setLoading(boolean loading) {
        if (progress != null) progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showListOrEmpty(boolean hasData) {
        if (empty != null)   empty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        if (recycler != null) recycler.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    private void loadAttendanceHistory() {
        setLoading(true);
        if (empty != null) empty.setVisibility(View.GONE);

        ApiClient.attendance().history(courseId).enqueue(new Callback<List<SessionHistoryDto>>() {
            @Override public void onResponse(Call<List<SessionHistoryDto>> call, Response<List<SessionHistoryDto>> resp) {
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    List<SessionHistoryDto> data = resp.body();
                    adapter.setItems(data);
                    showListOrEmpty(!data.isEmpty());
                } else {
                    showListOrEmpty(false);
                    Toast.makeText(AttendanceHistoryActivity.this, "Hata: " + resp.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<SessionHistoryDto>> call, Throwable t) {
                setLoading(false);
                showListOrEmpty(false);
                Toast.makeText(AttendanceHistoryActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
