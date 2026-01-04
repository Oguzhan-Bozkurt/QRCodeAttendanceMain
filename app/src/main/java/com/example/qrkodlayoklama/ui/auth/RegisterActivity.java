package com.example.qrkodlayoklama.ui.auth;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.RegisterRequest;
import com.example.qrkodlayoklama.ui.BaseActivity;
import com.google.android.material.textfield.TextInputLayout;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    private EditText etUsername, etPassword, etName, etSurname;
    private TextInputLayout tilUsername;
    private Spinner spUserIsStudent, spTitle;
    private Button btnRegister;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupToolbar("Kayıt Ol", true);

        tilUsername = findViewById(R.id.tilUsername);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        spUserIsStudent = findViewById(R.id.spUserIsStudent);
        spTitle = findViewById(R.id.spTitle);
        btnRegister = findViewById(R.id.btnRegister);

        ArrayAdapter<CharSequence> adapterRole = ArrayAdapter.createFromResource(this, R.array.roles, android.R.layout.simple_spinner_item);
        adapterRole.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUserIsStudent.setAdapter(adapterRole);

        ArrayAdapter<CharSequence> adapterTitle = ArrayAdapter.createFromResource(this, R.array.titles, android.R.layout.simple_spinner_item);
        adapterTitle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTitle.setAdapter(adapterTitle);

        btnRegister.setOnClickListener(v -> register());

        spUserIsStudent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if ("Akademisyen".equals(selected)) {
                    spTitle.setVisibility(View.VISIBLE);
                    spTitle.setEnabled(true);
                } else {
                    spTitle.setVisibility(View.GONE);
                    spTitle.setEnabled(false);
                    spTitle.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    private void register() {
        // Clear previous error
        tilUsername.setError(null);

        String userNameStr = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String surName = etSurname.getText().toString().trim();
        boolean userIsStudent = spUserIsStudent.getSelectedItem().toString().equals("Öğrenci");
        String title = userIsStudent ? "Öğrenci" : spTitle.getSelectedItem().toString();

        if (userNameStr.isEmpty() || password.isEmpty() || name.isEmpty() || surName.isEmpty()) {
            Toast.makeText(this, "Tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        Long userName;
        try {
            userName = Long.parseLong(userNameStr);
        } catch (NumberFormatException e) {
            tilUsername.setError("Kullanıcı numarası sadece rakam olmalı");
            return;
        }

        RegisterRequest req = new RegisterRequest(userName, password, name, surName, userIsStudent, title);
        ApiClient.auth().register(req).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                if (resp.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Kayıt başarılı! Giriş yapabilirsiniz.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    if (resp.code() == 409) {
                        tilUsername.setError("Bu kullanıcı adı zaten kullanımda");
                    } else {
                        Toast.makeText(RegisterActivity.this, "Bir hata oluştu: " + resp.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
