package com.example.qrkodlayoklama.ui.auth;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.RegisterRequest;
import com.example.qrkodlayoklama.ui.BaseActivity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    private EditText etUsername, etPassword, etName, etSurname;
    private Spinner spRole, spTitle;
    private Button btnSubmit;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupToolbar("Kayıt Ol", true);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        spRole = findViewById(R.id.spRole);
        spTitle = findViewById(R.id.spTitle);
        btnSubmit = findViewById(R.id.btnSubmit);

        ArrayAdapter<CharSequence> adapterRole = ArrayAdapter.createFromResource(this, R.array.roles, android.R.layout.simple_spinner_item);
        adapterRole.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapterRole);

        ArrayAdapter<CharSequence> adapterTitle = ArrayAdapter.createFromResource(this, R.array.titles, android.R.layout.simple_spinner_item);
        adapterTitle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTitle.setAdapter(adapterTitle);

        btnSubmit.setOnClickListener(v -> register());
    }

    private void register() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        boolean role = spRole.getSelectedItem().toString().equals("Öğrenci") ? true : false;
        String title = spRole.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty()) {
            Toast.makeText(this, "Tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest req = new RegisterRequest(username, password, name, surname, role, title);
        ApiClient.auth().register(req).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                if (resp.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Kayıt başarılı! Giriş yapabilirsiniz.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Hata: " + resp.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
