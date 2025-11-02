package com.example.qrkodlayoklama.ui.course;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.CourseDto;
import com.example.qrkodlayoklama.ui.BaseActivity;
import com.example.qrkodlayoklama.ui.attendance.QrShowActivity;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseListActivity extends BaseActivity {

    private ProgressBar progress;
    private TextView empty;
    private RecyclerView recycler;
    private CourseAdapter adapter;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);
        setupToolbar("Dersler", true);

        progress = findViewById(R.id.progress);
        empty    = findViewById(R.id.empty);
        recycler = findViewById(R.id.recycler);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseAdapter();
        recycler.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            Intent i = new Intent(CourseListActivity.this, QrShowActivity.class);
            i.putExtra(QrShowActivity.EXTRA_COURSE_ID, item.getId());
            i.putExtra(QrShowActivity.EXTRA_COURSE_NAME, item.getCourseName());
            startActivity(i);
        });

        adapter.setOnDeleteClickListener(course -> {
            new AlertDialog.Builder(this)
                    .setTitle("Dersi Sil")
                    .setMessage(course.getCourseName()
                            + " dersini ve yoklama kayıtlarını silmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet", (d, w) -> deleteCourse(course))
                    .setNegativeButton("İptal", null)
                    .show();
        });

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
        ApiClient.courses().all().enqueue(new Callback<List<CourseDto>>() {
            @Override public void onResponse(Call<List<CourseDto>> call, Response<List<CourseDto>> resp) {
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    List<CourseDto> data = resp.body();
                    adapter.setItems(data);
                    empty.setText("Henüz ders yok");
                    showListOrEmpty(!data.isEmpty());
                } else {
                    showListOrEmpty(false);
                    Toast.makeText(CourseListActivity.this, "Hata: " + resp.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<List<CourseDto>> call, Throwable t) {
                setLoading(false);
                showListOrEmpty(false);
                Toast.makeText(CourseListActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCourse(CourseDto course) {
        ApiClient.courses().delete(course.getId()).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                if (resp.isSuccessful()) {
                    Toast.makeText(CourseListActivity.this, "Ders silindi", Toast.LENGTH_SHORT).show();
                    load();
                } else {
                    Toast.makeText(CourseListActivity.this, "Hata: " + resp.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(CourseListActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
