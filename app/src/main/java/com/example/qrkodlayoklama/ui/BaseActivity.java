package com.example.qrkodlayoklama.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.local.SessionManager;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.ui.login.LoginActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupToolbar(String title, boolean showBack) {
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
                toolbar.setTitleMarginTop(dp(15));
                getSupportActionBar().setDisplayHomeAsUpEnabled(showBack);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Çıkış Yap")
                    .setMessage("Çıkış yapmak istediğinize emin misiniz?")
                    .setPositiveButton("Evet", (d, w) -> doLogout())
                    .setNegativeButton("İptal", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doLogout() {
        try {
            SessionManager.setToken(null);
            SessionManager.clear();
        } catch (Throwable ignore) {}

        try {
            getApplicationContext()
                    .getSharedPreferences("auth", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
        } catch (Throwable ignore) {}

        try {
            ApiClient.clearAuth();
        } catch (Throwable ignore) {}

        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finishAffinity();
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }
}
