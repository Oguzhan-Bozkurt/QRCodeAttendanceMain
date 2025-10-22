package com.example.qrkodlayoklama.ui.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.AttendanceSessionDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceSessionDetailActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE_ID = "courseId";
    public static final String EXTRA_SESSION_ID = "sessionId";

    private long courseId, sessionId;
    private ProgressBar progress;
    private TextView tvInfo, tvSecret, tvCreated, tvExpires, tvActive;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_session_detail);

        progress = findViewById(R.id.progress);
        tvInfo   = findViewById(R.id.tvInfo);
        tvSecret = findViewById(R.id.tvSecret);
        tvCreated= findViewById(R.id.tvCreated);
        tvExpires= findViewById(R.id.tvExpires);
        tvActive = findViewById(R.id.tvActive);

        courseId  = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        sessionId = getIntent().getLongExtra(EXTRA_SESSION_ID, -1);

        if (courseId == -1 || sessionId == -1) {
            Toast.makeText(this, "Eksik oturum bilgisi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDetail();
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
}
