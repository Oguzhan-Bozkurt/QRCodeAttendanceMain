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
import com.example.qrkodlayoklama.data.remote.model.MyAttendanceDto;
import com.example.qrkodlayoklama.ui.BaseActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        load();
    }

    private void setLoading(boolean b) {
        if (progress != null) progress.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    private void showListOrEmpty(boolean hasData) {
        if (empty != null) empty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        if (recycler != null) recycler.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    private void load() {
        setLoading(true);
        ApiClient.attendance().myAttendance().enqueue(new Callback<List<MyAttendanceDto>>() {
            @Override
            public void onResponse(Call<List<MyAttendanceDto>> call, Response<List<MyAttendanceDto>> resp) {
                setLoading(false);
                if (!resp.isSuccessful() || resp.body() == null) {
                    showListOrEmpty(false);
                    Toast.makeText(MyAttendanceActivity.this, "Hata: " + resp.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<MyAttendanceDto> data = resp.body();
                empty.setText("Henüz yoklama kaydı bulunmuyor.");

                if (data.isEmpty()) {
                    adapter.setItems(new ArrayList<>());
                    showListOrEmpty(false);
                    return;
                }

                Map<Long, MyAttendanceAdapter.SummaryItem> map = new LinkedHashMap<>();

                for (MyAttendanceDto it : data) {
                    long courseId = it.getCourseId();

                    MyAttendanceAdapter.SummaryItem item = map.get(courseId);
                    if (item == null) {
                        String cn = it.getCourseName();
                        String cc = it.getCourseCode();
                        item = new MyAttendanceAdapter.SummaryItem(courseId, cn, cc);
                        item.totalSessions = (int) it.getTotalSessions();
                        map.put(courseId, item);
                    }

                    item.attended++;
                }

                List<MyAttendanceAdapter.SummaryItem> summaries = new ArrayList<>(map.values());
                adapter.setItems(summaries);
                showListOrEmpty(!summaries.isEmpty());
            }

            @Override
            public void onFailure(Call<List<MyAttendanceDto>> call, Throwable t) {
                setLoading(false);
                showListOrEmpty(false);
                Toast.makeText(MyAttendanceActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
