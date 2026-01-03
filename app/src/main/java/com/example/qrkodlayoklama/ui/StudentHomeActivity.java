package com.example.qrkodlayoklama.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.qrkodlayoklama.R;

public class StudentHomeActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);
        setupToolbar("Öğrenci", false);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        Button btnScanQr = findViewById(R.id.btnScanQr);
        btnScanQr.setOnClickListener(v -> {
            Intent i = new Intent(StudentHomeActivity.this,
                    com.example.qrkodlayoklama.ui.attendance.QrScanActivity.class);
            startActivity(i);
        });

        Button btnMyAttendance = findViewById(R.id.btnMyAttendance);
        btnMyAttendance.setOnClickListener(v -> {
            Intent i = new Intent(StudentHomeActivity.this,
                    com.example.qrkodlayoklama.ui.attendance.MyAttendanceActivity.class);
            startActivity(i);
        });
    }
}
