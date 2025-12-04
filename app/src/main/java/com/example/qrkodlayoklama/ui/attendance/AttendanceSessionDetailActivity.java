package com.example.qrkodlayoklama.ui.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.AttendanceRecordDto;
import com.example.qrkodlayoklama.data.remote.model.AttendanceSessionDto;
import com.example.qrkodlayoklama.ui.BaseActivity;
import com.example.qrkodlayoklama.ui.course.StudentPickerActivity;
import com.example.qrkodlayoklama.util.DateFormat;

import java.util.HashSet;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceSessionDetailActivity extends BaseActivity {

    public static final String EXTRA_COURSE_ID = "courseId";
    public static final String EXTRA_SESSION_ID = "sessionId";
    public static final String EXTRA_COURSE_NAME = "courseName";
    public static final String RESULT_CHANGED = "result_changed";
    public static final String RESULT_DELETED = "result_deleted";

    private ActivityResultLauncher<Intent> studentPickerLauncher;
    private final HashSet<Long> presentIds = new java.util.HashSet<>();

    private long courseId, sessionId;
    String courseName;
    private ProgressBar progress;
    private TextView tvInfo, tvSecret, tvCreated, tvExpires, tvActive, empty;
    private RecyclerView recycler;
    private SessionRecordsAdapter adapter;
    private Button btnAddManualStudent;

    private boolean somethingChanged = false;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_session_detail);

        progress = findViewById(R.id.progress);
        tvInfo = findViewById(R.id.tvInfo);
        tvSecret = findViewById(R.id.tvSecret);
        tvCreated = findViewById(R.id.tvCreated);
        tvExpires = findViewById(R.id.tvExpires);
        tvActive = findViewById(R.id.tvActive);
        recycler = findViewById(R.id.recyclerRecords);
        empty = findViewById(R.id.empty);
        btnAddManualStudent = findViewById(R.id.btnAddManualStudent);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SessionRecordsAdapter();
        recycler.setAdapter(adapter);

        courseId  = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        sessionId = getIntent().getLongExtra(EXTRA_SESSION_ID, -1);
        courseName = getIntent().getStringExtra(EXTRA_COURSE_NAME);

        if (courseId == -1 || sessionId == -1) {
            Toast.makeText(this, "Eksik oturum bilgisi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar(courseName, true);

        studentPickerLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        long[] selectedArr = result.getData()
                                .getLongArrayExtra(StudentPickerActivity.RESULT_SELECTED_IDS);
                        if (selectedArr == null) selectedArr = new long[0];

                        java.util.HashSet<Long> selected = new java.util.HashSet<>();
                        for (long x : selectedArr) selected.add(x);

                        java.util.ArrayList<Long> toAdd = new java.util.ArrayList<>();
                        java.util.ArrayList<Long> toRemove = new java.util.ArrayList<>();

                        for (Long s : selected) if (!presentIds.contains(s)) toAdd.add(s);
                        for (Long p : presentIds) if (!selected.contains(p)) toRemove.add(p);

                        applyManualChanges(toAdd, toRemove);
                    }
                });


        if (btnAddManualStudent != null) {
            btnAddManualStudent.setOnClickListener(v -> {
                Intent i = new Intent(AttendanceSessionDetailActivity.this, StudentPickerActivity.class);

                long[] pre = new long[presentIds.size()];
                int k = 0; for (Long id : presentIds) pre[k++] = id;
                i.putExtra(StudentPickerActivity.EXTRA_PRESELECTED_IDS, pre);

                studentPickerLauncher.launch(i);
            });
        }

        loadDetail();
        loadRecords();
    }

    private void setLoading(boolean b) {
        if (progress != null) progress.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    private void loadDetail() {
        setLoading(true);
        ApiClient.attendance().sessionDetail(courseId, sessionId).enqueue(new Callback<AttendanceSessionDto>() {
            @Override public void onResponse(Call<AttendanceSessionDto> call, Response<AttendanceSessionDto> resp) {
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    var s = resp.body();
                    if (tvInfo   != null) tvInfo.setText(s.getDescription());
                    if (tvSecret != null) tvSecret.setText("Kod: " + s.getSecret());
                    if (tvCreated != null) tvCreated.setText("Başlangıç: " + DateFormat.any(s.getCreatedAt()));
                    if (tvExpires != null) tvExpires.setText("Bitiş: " + DateFormat.any(s.getExpiresAt()));
                    if (tvActive  != null) tvActive.setText("Durum: " + (Boolean.TRUE.equals(s.isActive()) ? "Aktif" : "Pasif"));
                } else {
                    Toast.makeText(AttendanceSessionDetailActivity.this,
                            "Bulunamadı: " + resp.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<AttendanceSessionDto> call, Throwable t) {
                setLoading(false);
                Toast.makeText(AttendanceSessionDetailActivity.this,
                        "Ağ hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showListOrEmpty(boolean hasData) {
        if (empty != null)   empty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        if (recycler != null) recycler.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    private void loadRecords() {
        setLoading(true);
        ApiClient.attendance().sessionRecords(courseId, sessionId)
                .enqueue(new retrofit2.Callback<List<AttendanceRecordDto>>() {
                    @Override public void onResponse(retrofit2.Call<List<AttendanceRecordDto>> call,
                                                     retrofit2.Response<List<AttendanceRecordDto>> resp) {
                        setLoading(false);
                        if (resp.isSuccessful() && resp.body() != null) {
                            List<AttendanceRecordDto> data = resp.body();
                            adapter.setItems(data);
                            showListOrEmpty(!data.isEmpty());

                            presentIds.clear();
                            for (AttendanceRecordDto r : data) {
                                presentIds.add(r.getStudentId());
                            }
                        } else {
                            showListOrEmpty(false);
                            Toast.makeText(AttendanceSessionDetailActivity.this,
                                    "Hata: " + resp.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(retrofit2.Call<List<AttendanceRecordDto>> call, Throwable t) {
                        setLoading(false);
                        showListOrEmpty(false);
                        Toast.makeText(AttendanceSessionDetailActivity.this,
                                "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applyManualChanges(java.util.List<Long> toAdd, java.util.List<Long> toRemove) {
        if ((toAdd == null || toAdd.isEmpty()) && (toRemove == null || toRemove.isEmpty())) {
            Toast.makeText(this, "Değişiklik yok", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        final java.util.concurrent.atomic.AtomicInteger pending =
                new java.util.concurrent.atomic.AtomicInteger(
                        (toAdd != null ? toAdd.size() : 0) + (toRemove != null ? toRemove.size() : 0));

        if (toAdd != null) for (Long id : toAdd) {
            var body = new com.example.qrkodlayoklama.data.remote.model.ManualAddRequest(id);
            ApiClient.attendance().manualAdd(courseId, sessionId, body)
                    .enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                        @Override public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call,
                                                         retrofit2.Response<okhttp3.ResponseBody> resp) {
                            if (!resp.isSuccessful() && resp.code() != 409) {
                                Toast.makeText(AttendanceSessionDetailActivity.this,
                                        "Ekleme hatası: " + resp.code(), Toast.LENGTH_SHORT).show();
                            }
                            if (pending.decrementAndGet() == 0) { setLoading(false); loadRecords(); }
                        }
                        @Override public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                            Toast.makeText(AttendanceSessionDetailActivity.this,
                                    "Ağ hatası (ekleme): " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            if (pending.decrementAndGet() == 0) { setLoading(false); loadRecords(); }
                        }
                    });
        }

        if (toRemove != null) for (Long id : toRemove) {
            ApiClient.attendance().manualRemove(courseId, sessionId, id)
                    .enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                        @Override public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call,
                                                         retrofit2.Response<okhttp3.ResponseBody> resp) {
                            if (!resp.isSuccessful() && resp.code() != 404) {
                                Toast.makeText(AttendanceSessionDetailActivity.this,
                                        "Silme hatası: " + resp.code(), Toast.LENGTH_SHORT).show();
                            }
                            if (pending.decrementAndGet() == 0) { setLoading(false); loadRecords(); }
                        }
                        @Override public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                            Toast.makeText(AttendanceSessionDetailActivity.this,
                                    "Ağ hatası (silme): " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            if (pending.decrementAndGet() == 0) { setLoading(false); loadRecords(); }
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_session_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_session) {
            showEditSessionDialog();
            return true;
        } else if (id == R.id.action_delete_session) {
            confirmDeleteSession();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditSessionDialog() {
        if (tvInfo == null) return;

        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(tvInfo.getText());
        new AlertDialog.Builder(this)
                .setTitle("Oturum Başlığını Düzenle")
                .setView(input)
                .setPositiveButton("Kaydet", (d, w) -> {
                    String newDesc = input.getText().toString().trim();
                    if (newDesc.isEmpty()) {
                        Toast.makeText(this, "Başlık boş olamaz", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newDesc.length() > 50) {
                        Toast.makeText(this, "Başlık en fazla 50 karakter olabilir", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    doUpdateSession(newDesc);
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void doUpdateSession(String newDesc) {
        setLoading(true);
        ApiClient.attendance()
                .updateSession(courseId, sessionId,
                        new com.example.qrkodlayoklama.data.remote.model.AttendanceUpdateRequest(newDesc))
                .enqueue(new retrofit2.Callback<AttendanceSessionDto>() {
                    @Override
                    public void onResponse(retrofit2.Call<AttendanceSessionDto> call,
                                           retrofit2.Response<AttendanceSessionDto> resp) {
                        setLoading(false);
                        if (resp.isSuccessful() && resp.body() != null) {
                            AttendanceSessionDto s = resp.body();
                            if (tvInfo != null) tvInfo.setText(s.getDescription());
                            Toast.makeText(AttendanceSessionDetailActivity.this,
                                    "Oturum güncellendi", Toast.LENGTH_SHORT).show();
                            somethingChanged = true;   // <—
                        } else {
                            Toast.makeText(AttendanceSessionDetailActivity.this,
                                    "Güncelleme hatası: " + resp.code(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<AttendanceSessionDto> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(AttendanceSessionDetailActivity.this,
                                "Ağ hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void confirmDeleteSession() {
        new AlertDialog.Builder(this)
                .setTitle("Oturumu Sil")
                .setMessage("Bu oturumu ve yoklama kayıtlarını silmek istediğinize emin misiniz?")
                .setPositiveButton("Evet", (d, w) -> doDeleteSession())
                .setNegativeButton("İptal", null)
                .show();
    }

    private void doDeleteSession() {
        setLoading(true);
        ApiClient.attendance()
                .deleteSession(courseId, sessionId)
                .enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<ResponseBody> call,
                            retrofit2.Response<ResponseBody> resp) {
                        setLoading(false);
                        if (resp.isSuccessful()) {
                            Toast.makeText(AttendanceSessionDetailActivity.this,
                                    "Oturum silindi", Toast.LENGTH_SHORT).show();

                            Intent data = new Intent();
                            data.putExtra(RESULT_CHANGED, true);
                            data.putExtra(RESULT_DELETED, true);
                            setResult(RESULT_OK, data);
                            finish();
                        } else {
                            Toast.makeText(AttendanceSessionDetailActivity.this,
                                    "Silme hatası: " + resp.code(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<ResponseBody> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(AttendanceSessionDetailActivity.this,
                                "Ağ hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void finish() {
        if (somethingChanged) {
            Intent data = new Intent();
            data.putExtra(RESULT_CHANGED, true);
            setResult(RESULT_OK, data);
        }
        super.finish();
    }
}
