package com.example.qrkodlayoklama.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.local.SessionManager;
import com.example.qrkodlayoklama.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SessionManager.getToken().isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.tvHello);
        Button btnLogout = findViewById(R.id.btnLogout);

        tv.setText("Hoş geldin!");

        btnLogout.setOnClickListener(v -> {
            SessionManager.clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
