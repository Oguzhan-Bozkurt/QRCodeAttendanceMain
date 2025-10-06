package com.example.qrkodlayoklama.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.content.Intent;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.local.SessionManager;
import com.example.qrkodlayoklama.ui.login.LoginActivity;


public class StudentHomeActivity extends AppCompatActivity {
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> doLogout());

        Button btnScanQr = findViewById(R.id.btnScanQr);
        btnScanQr.setOnClickListener(v -> {
            Intent i = new Intent(StudentHomeActivity.this,
                    com.example.qrkodlayoklama.ui.attendance.QrScanActivity.class);
            startActivity(i);
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Çıkış yap")
                .setMessage("Oturumu kapatmak istiyor musunuz?")
                .setPositiveButton("Evet", (d, w) -> {
                    SessionManager.clear();
                    Intent i = new Intent(this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                })
                .setNegativeButton("İptal", null)
                .show();
    }
    private void doLogout() {
        SessionManager.clear();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

}
