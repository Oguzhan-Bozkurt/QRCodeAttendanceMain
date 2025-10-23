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
import com.example.qrkodlayoklama.data.remote.model.AttendanceRecordDto;
import com.example.qrkodlayoklama.data.remote.model.AttendanceSessionDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceSessionDetailActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE_ID = "courseId";
    public static final String EXTRA_SESSION_ID = "sessionId";
    private long courseId, sessionId;
    private ProgressBar progress;
    private TextView tvInfo, tvSecret, tvCreated, tvExpires, tvActive, empty;
    private RecyclerView recycler;
    private SessionRecordsAdapter adapter;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_session_detail);

        progress = findViewById(R.id.progress);
        tvInfo   = findViewById(R.id.tvInfo);
        tvSecret = findViewById(R.id.tvSecret);
        tvCreated= findViewById(R.id.tvCreated);
        tvExpires= findViewById(R.id.tvExpires);
        tvActive = findViewById(R.id.tvActive);
        recycler = findViewById(R.id.recyclerRecords);
        empty    = findViewById(R.id.empty);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SessionRecordsAdapter();
        recycler.setAdapter(adapter);

        courseId  = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        sessionId = getIntent().getLongExtra(EXTRA_SESSION_ID, -1);

        if (courseId == -1 || sessionId == -1) {
            Toast.makeText(this, "Eksik oturum bilgisi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDetail();
        loadRecords();
    }

    private void setLoading(boolean b) {
        if (progress != null) progress.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    private void loadDetail() {
        setLoading(true);
        ApiClient.attendance().sessionDetail(courseId, sessionId).enqueue(new Callback<AttendanceSessionDto>() {
            @Override public void onResponse(Call<AttendanceSessionDto> call, Response<AttendanceSessionDto> resp) {
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    var s = resp.body();
                    if (tvInfo   != null) tvInfo.setText("Oturum #" + s.getId());
                    if (tvSecret != null) tvSecret.setText("Kod: " + s.getSecret());
                    if (tvCreated!= null) tvCreated.setText("Başlangıç: " + (s.getCreatedAt() != null ? s.getCreatedAt() : "-"));
                    if (tvExpires!= null) tvExpires.setText("Bitiş: " + (s.getExpiresAt() != null ? s.getExpiresAt() : "-"));
                    if (tvActive != null) tvActive.setText(Boolean.TRUE.equals(s.isActive()) ? "Aktif" : "Pasif");
                } else {
                    Toast.makeText(AttendanceSessionDetailActivity.this,
                            "Bulunamadı: " + resp.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<AttendanceSessionDto> call, Throwable t) {
                setLoading(false);
                Toast.makeText(AttendanceSessionDetailActivity.this,
                        "Ağ hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showListOrEmpty(boolean hasData) {
        if (empty != null)   empty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        if (recycler != null) recycler.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    private void loadRecords() {
        setLoading(true);
        ApiClient.attendance().sessionRecords(courseId, sessionId)
                .enqueue(new retrofit2.Callback<List<AttendanceRecordDto>>() {
                    @Override public void onResponse(
                            retrofit2.Call<List<AttendanceRecordDto>> call,
                            retrofit2.Response<List<AttendanceRecordDto>> resp) {
                        setLoading(false);
                        if (resp.isSuccessful() && resp.body() != null) {
                            List<AttendanceRecordDto> data = resp.body();
                            adapter.setItems(data);
                            showListOrEmpty(!data.isEmpty());
                        } else {
                            showListOrEmpty(false);
                            Toast.makeText(AttendanceSessionDetailActivity.this,
                                    "Hata: " + resp.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override public void onFailure(
                            retrofit2.Call<List<AttendanceRecordDto>> call, Throwable t) {
                        setLoading(false);
                        showListOrEmpty(false);
                        Toast.makeText(AttendanceSessionDetailActivity.this,
                                "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
