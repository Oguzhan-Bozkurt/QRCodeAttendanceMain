package com.example.qrkodlayoklama.ui.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.MyAttendanceDto;
import com.example.qrkodlayoklama.ui.BaseActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAttendanceActivity extends BaseActivity {

    private ProgressBar progress;
    private TextView empty;
    private RecyclerView recycler;
    private MyAttendanceAdapter adapter;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_attendance);
        setupToolbar("Devam Durumum", true);

        progress = findViewById(R.id.progress);
        empty    = findViewById(R.id.empty);
        recycler = findViewById(R.id.recycler);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAttendanceAdapter();
        recycler.setAdapter(adapter);

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
        ApiClient.attendance().myAttendance().enqueue(new Callback<List<MyAttendanceDto>>() {
            @Override public void onResponse(Call<List<MyAttendanceDto>> call, Response<List<MyAttendanceDto>> resp) {
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    List<MyAttendanceDto> data = resp.body();
                    adapter.setItems(data);
                    empty.setText("Henüz yoklama kaydı bulunmuyor.");
                    showListOrEmpty(!data.isEmpty());
                } else {
                    showListOrEmpty(false);
                    Toast.makeText(MyAttendanceActivity.this, "Hata: " + resp.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<MyAttendanceDto>> call, Throwable t) {
                setLoading(false);
                showListOrEmpty(false);
                Toast.makeText(MyAttendanceActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
