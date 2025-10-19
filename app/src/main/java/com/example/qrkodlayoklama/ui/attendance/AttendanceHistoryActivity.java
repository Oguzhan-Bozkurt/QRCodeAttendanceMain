package com.example.qrkodlayoklama.ui.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.SessionHistoryDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceHistoryActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE_ID = "courseId";

    private ProgressBar progress;
    private TextView empty;
    private RecyclerView recycler;
    private AttendanceHistoryAdapter adapter;
    private long courseId;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        if (courseId == -1) {
            Toast.makeText(this, "Ders bilgisi yok", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progress = findViewById(R.id.progress);
        empty    = findViewById(R.id.empty);
        recycler = findViewById(R.id.recycler);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceHistoryAdapter();
        recycler.setAdapter(adapter);

        load();
    }

    private void setLoading(boolean b) {
        progress.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    private void showListOrEmpty(boolean hasData) {
        empty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        recycler.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    private void load() {
        setLoading(true);
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
