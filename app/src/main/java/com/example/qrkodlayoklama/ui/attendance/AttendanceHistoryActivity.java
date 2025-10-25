package com.example.qrkodlayoklama.ui.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

    private RecyclerView recycler;
    private ProgressBar progress;
    private AttendanceHistoryAdapter adapter;
    private long courseId;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);
        setupToolbar("", true);

        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        if (courseId == -1) {
            Toast.makeText(this, "Ders bilgisi yok", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progress = findViewById(R.id.progress);
        recycler = findViewById(R.id.recyclerHistory);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AttendanceHistoryAdapter(courseId);
        recycler.setAdapter(adapter);

        loadAttendanceHistory();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void loadAttendanceHistory() {
        setLoading(true);
        ApiClient.attendance().history(courseId).enqueue(new Callback<List<SessionHistoryDto>>() {
            @Override public void onResponse(Call<List<SessionHistoryDto>> call, Response<List<SessionHistoryDto>> resp) {
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    adapter.setItems(resp.body());
                } else {
                    Toast.makeText(AttendanceHistoryActivity.this, "Hata: " + resp.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<SessionHistoryDto>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(AttendanceHistoryActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
