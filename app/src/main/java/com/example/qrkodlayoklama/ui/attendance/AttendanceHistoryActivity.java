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

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_attendance_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_export_pdf) {
            exportPdf();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportPdf() {
        setLoading(true);
        ApiClient.attendance().exportCoursePdf(courseId).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> resp) {
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    try {
                        String safeCourse = (courseName == null ? "Ders" : courseName).replaceAll("[^\\w\\-]+", "_");
                        String fileName = "Yoklama_" + safeCourse + "_" +
                                new java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault())
                                        .format(new java.util.Date()) + ".pdf";

                        boolean ok = saveToDownloads(resp.body().byteStream(), fileName);
                        if (ok) {
                            android.widget.Toast.makeText(AttendanceHistoryActivity.this,
                                    "PDF indirildi: " + fileName, android.widget.Toast.LENGTH_LONG).show();
                        } else {
                            android.widget.Toast.makeText(AttendanceHistoryActivity.this,
                                    "PDF kaydedilemedi", android.widget.Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        android.widget.Toast.makeText(AttendanceHistoryActivity.this,
                                "Hata: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                } else {
                    android.widget.Toast.makeText(AttendanceHistoryActivity.this,
                            "İndirme hatası: " + resp.code(), android.widget.Toast.LENGTH_LONG).show();
                }
            }

            @Override public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                setLoading(false);
                android.widget.Toast.makeText(AttendanceHistoryActivity.this,
                        "Ağ hatası: " + t.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean saveToDownloads(java.io.InputStream in, String fileName) {
        try {
            android.content.ContentResolver resolver = getContentResolver();
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(android.provider.MediaStore.Downloads.IS_PENDING, 1);

            android.net.Uri uri = resolver.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return false;

            try (java.io.OutputStream out = resolver.openOutputStream(uri)) {
                if (out == null) return false;
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
                out.flush();
            } finally {
                in.close();
            }

            values.clear();
            values.put(android.provider.MediaStore.Downloads.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
