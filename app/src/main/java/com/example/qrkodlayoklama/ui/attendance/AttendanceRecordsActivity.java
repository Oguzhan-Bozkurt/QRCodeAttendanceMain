package com.example.qrkodlayoklama.ui.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.AttendanceRecordDto;

import java.util.List;


public class AttendanceRecordsActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE_ID = "courseId";
    private ProgressBar progress;
    private TextView empty;
    private RecyclerView recycler;
    private AttendanceRecordsAdapter adapter;
    private long courseId;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_records);

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
        adapter = new AttendanceRecordsAdapter();
        recycler.setAdapter(adapter);

        adapter.setOnItemClickListener(this::showRecordDialog);

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

        ApiClient.attendance().records(courseId).enqueue(new retrofit2.Callback<List<AttendanceRecordDto>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<List<AttendanceRecordDto>> call,
                                           @NonNull retrofit2.Response<List<AttendanceRecordDto>> resp) {
                        setLoading(false);
                        if (resp.isSuccessful() && resp.body() != null) {
                            List<AttendanceRecordDto> data = resp.body();
                            adapter.setItems(data);         // adapter.setItems(List<AttendanceRecordDto>) olmalı
                            showListOrEmpty(!data.isEmpty());
                        } else {
                            showListOrEmpty(false);
                            Toast.makeText(AttendanceRecordsActivity.this,
                                    "Hata: " + resp.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<List<AttendanceRecordDto>> call,
                                          @NonNull Throwable t) {
                        setLoading(false);
                        showListOrEmpty(false);
                        Toast.makeText(AttendanceRecordsActivity.this,
                                "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRecordDialog(AttendanceRecordDto r) {
        String userNo  = r.getUserName() != null ? String.valueOf(r.getUserName()) : "";
        String name    = r.getName() != null ? r.getName() : "";
        String surname = r.getSurname() != null ? r.getSurname() : "";
        String checked = r.getCheckedAt() != null ? r.getCheckedAt() : "";

        String when = checked;
        try {
            String s = checked.replace('T', ' ');
            if (s.endsWith("Z")) s = s.substring(0, s.length() - 1);
            when = s.length() >= 16 ? s.substring(0, 16) : s;
        } catch (Exception ignore) { /* no-op */ }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Yoklama Detayı")
                .setMessage(
                        "Öğrenci No: " + userNo +
                                "\nAd Soyad: " + name + " " + surname +
                                "\nZaman: " + when
                )
                .setPositiveButton("Tamam", null)
                .show();
    }

}
