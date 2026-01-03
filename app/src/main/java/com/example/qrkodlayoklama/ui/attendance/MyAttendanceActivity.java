package com.example.qrkodlayoklama.ui.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.MyAttendanceSummaryDto;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_attendance);
        setupToolbar("Devam Durumum", true);

        progress = findViewById(R.id.progress);
        empty = findViewById(R.id.empty);
        recycler = findViewById(R.id.recycler);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAttendanceAdapter();
        recycler.setAdapter(adapter);

        loadAttendanceSummary();
    }

    private void setLoading(boolean isLoading) {
        if (progress != null) progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showListOrEmpty(boolean hasData) {
        if (empty != null) empty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        if (recycler != null) recycler.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    private void loadAttendanceSummary() {
        setLoading(true);
        ApiClient.attendance().myAttendance().enqueue(new Callback<List<MyAttendanceSummaryDto>>() {
            @Override
            public void onResponse(Call<List<MyAttendanceSummaryDto>> call, Response<List<MyAttendanceSummaryDto>> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    showListOrEmpty(false);
                    Toast.makeText(MyAttendanceActivity.this, "Hata: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<MyAttendanceSummaryDto> summaryList = response.body();

                if (summaryList.isEmpty()) {
                    empty.setText("Henüz kayıtlı olduğunuz bir ders bulunmuyor.");
                    showListOrEmpty(false);
                } else {
                    adapter.setItems(summaryList);
                    showListOrEmpty(true);
                }
            }

            @Override
            public void onFailure(Call<List<MyAttendanceSummaryDto>> call, Throwable t) {
                setLoading(false);
                showListOrEmpty(false);
                Toast.makeText(MyAttendanceActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
