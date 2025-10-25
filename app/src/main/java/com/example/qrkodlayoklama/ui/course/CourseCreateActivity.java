package com.example.qrkodlayoklama.ui.course;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.CourseCreateRequest;
import com.example.qrkodlayoklama.data.remote.model.CourseDto;
import com.example.qrkodlayoklama.ui.BaseActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseCreateActivity extends BaseActivity {

    private EditText etCourseName, etCourseCode;
    private Button btnSave;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_create);
        setupToolbar("", true);


        etCourseName = findViewById(R.id.etCourseName);
        etCourseCode = findViewById(R.id.etCourseCode);
        btnSave      = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            String name = etCourseName.getText().toString().trim();
            String code = etCourseCode.getText().toString().trim();

            if (name.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "Ad ve Kod gerekli", Toast.LENGTH_SHORT).show();
                return;
            }

            setLoading(true);

            ApiClient.courses().create(new CourseCreateRequest(name, code))
                    .enqueue(new Callback<CourseDto>() {
                        @Override public void onResponse(Call<CourseDto> call, Response<CourseDto> resp) {
                            setLoading(false);
                            if (resp.isSuccessful() && resp.body() != null) {
                                Toast.makeText(CourseCreateActivity.this, "Kaydedildi", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                String body = null;
                                try { body = resp.errorBody() != null ? resp.errorBody().string() : null; } catch (Exception ignored) {}
                                Toast.makeText(CourseCreateActivity.this,
                                        "Hata: " + resp.code() + (body != null ? ("\n" + body) : ""),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override public void onFailure(Call<CourseDto> call, Throwable t) {
                            setLoading(false);
                            Toast.makeText(CourseCreateActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
    private void setLoading(boolean loading) {
        ProgressBar progress = findViewById(R.id.progress);
        View btnSave = findViewById(R.id.btnSave);
        View etName  = findViewById(R.id.etCourseName);
        View etCode  = findViewById(R.id.etCourseCode);

        if (progress != null) progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (btnSave != null)  btnSave.setEnabled(!loading);
        if (etName != null)   etName.setEnabled(!loading);
        if (etCode != null)   etCode.setEnabled(!loading);
    }


}
