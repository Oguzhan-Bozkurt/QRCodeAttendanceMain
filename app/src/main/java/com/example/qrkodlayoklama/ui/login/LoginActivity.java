package com.example.qrkodlayoklama.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.local.SessionManager;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.LoginRequest;
import com.example.qrkodlayoklama.data.remote.model.LoginResponse;
import com.example.qrkodlayoklama.data.remote.model.UserDto;
import com.example.qrkodlayoklama.ui.StudentHomeActivity;
import com.example.qrkodlayoklama.ui.AcademicHomeActivity;
import com.example.qrkodlayoklama.ui.auth.RegisterActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUserName, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progress;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progress);
        btnLogin.setOnClickListener(v -> doLogin());

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        if (SessionManager.isLoggedIn()) {
            setLoading(true);
            fetchMeAndRoute();
        }
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        etUserName.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }

    private void doLogin() {
        String userNameStr = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (userNameStr.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Kullanıcı numarası ve şifre gerekli", Toast.LENGTH_SHORT).show();
            return;
        }

        Long userName;
        try {
            userName = Long.parseLong(userNameStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Kullanıcı numarası sadece rakam olmalı", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        ApiClient.auth().login(new LoginRequest(userName, password))
                .enqueue(new Callback<LoginResponse>() {
                    @Override public void onResponse(Call<LoginResponse> call, Response<LoginResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            SessionManager.setToken(resp.body().getAccessToken());

                            fetchMeAndRoute();
                        } else {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, "Giriş başarısız", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override public void onFailure(Call<LoginResponse> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchMeAndRoute() {
        ApiClient.me().me().enqueue(new Callback<UserDto>() {
            @Override public void onResponse(Call<UserDto> call, Response<UserDto> resp) {
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    boolean isStudent = Boolean.TRUE.equals(resp.body().getUserIsStudent());
                    Intent intent = new Intent(LoginActivity.this,
                            isStudent ? StudentHomeActivity.class : AcademicHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Kullanıcı bilgisi alınamadı", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<UserDto> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
