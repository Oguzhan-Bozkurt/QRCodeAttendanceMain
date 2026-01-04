package com.example.qrkodlayoklama.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.qrkodlayoklama.R;

public class StudentHomeActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);
        setupToolbar("Öğrenci", false);

        View btnScanQr = findViewById(R.id.btnScanQr);
        btnScanQr.setOnClickListener(v -> {
            Intent i = new Intent(StudentHomeActivity.this,
                    com.example.qrkodlayoklama.ui.attendance.QrScanActivity.class);
            startActivity(i);
        });

        View btnMyAttendance = findViewById(R.id.btnMyAttendance);
        btnMyAttendance.setOnClickListener(v -> {
            Intent i = new Intent(StudentHomeActivity.this,
                    com.example.qrkodlayoklama.ui.attendance.MyAttendanceActivity.class);
            startActivity(i);
        });
    }
}
