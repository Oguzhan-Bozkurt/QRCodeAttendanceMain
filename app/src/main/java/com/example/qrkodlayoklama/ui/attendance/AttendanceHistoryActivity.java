package com.example.qrkodlayoklama.ui.attendance;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.AttendanceRecordDto;
import com.example.qrkodlayoklama.data.remote.model.SessionHistoryDto;
import com.example.qrkodlayoklama.data.remote.model.UserDto;
import com.example.qrkodlayoklama.ui.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceHistoryActivity extends BaseActivity {

    public static final String EXTRA_COURSE_ID = "courseId";
    public static final String EXTRA_COURSE_NAME = "courseName";
    public static final String EXTRA_COURSE_CODE = "courseCode";

    private RecyclerView recycler;
    private ProgressBar progress;
    private AttendanceHistoryAdapter adapter;
    private TextView empty;
    private long courseId;
    private String courseName, courseCode;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        courseName = getIntent().getStringExtra(EXTRA_COURSE_NAME);
        courseCode = getIntent().getStringExtra(EXTRA_COURSE_CODE);
        if (courseId == -1) {
            Toast.makeText(this, "Ders bilgisi yok", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar(courseName, true);

        progress = findViewById(R.id.progress);
        recycler = findViewById(R.id.recyclerHistory);
        empty = findViewById(R.id.empty);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceHistoryAdapter(courseId, courseName);
        recycler.setAdapter(adapter);

        loadAttendanceHistory();
    }

    @Override protected void onResume() {
        super.onResume();
        loadAttendanceHistory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_attendance_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_export_pdf) {
            showExportDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExportDialog() {
        String[] options = new String[] {
                "Genel Yoklama Yazdır",
                "Haftalık Yoklama Yazdır"
        };
        new AlertDialog.Builder(this)
                .setTitle("Yazdır")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        exportGeneralPdf();
                    } else {
                        exportWeeklyPdf(); // ✅ artık haftalık da çağrılıyor
                    }
                })
                .show();
    }

    private void setLoading(boolean loading) {
        if (progress != null) progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showListOrEmpty(boolean hasData) {
        if (empty != null)   empty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        if (recycler != null) recycler.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    private void loadAttendanceHistory() {
        setLoading(true);
        if (empty != null) empty.setVisibility(View.GONE);

        ApiClient.attendance().history(courseId)
                .enqueue(new Callback<List<SessionHistoryDto>>() {
                    @Override public void onResponse(
                            Call<List<SessionHistoryDto>> call,
                            Response<List<SessionHistoryDto>> resp) {
                        setLoading(false);
                        if (resp.isSuccessful() && resp.body() != null) {
                            List<SessionHistoryDto> data = resp.body();
                            adapter.setItems(data);
                            showListOrEmpty(!data.isEmpty());
                        } else {
                            showListOrEmpty(false);
                            Toast.makeText(AttendanceHistoryActivity.this,
                                    "Hata: " + resp.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override public void onFailure(
                            Call<List<SessionHistoryDto>> call, Throwable t) {
                        setLoading(false);
                        showListOrEmpty(false);
                        Toast.makeText(AttendanceHistoryActivity.this,
                                "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void exportGeneralPdf() {
        setLoading(true);

        ApiClient.courses().students(courseId)
                .enqueue(new Callback<List<UserDto>>() {
                    @Override public void onResponse(
                            Call<List<UserDto>> call,
                            Response<List<UserDto>> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            List<UserDto> students = resp.body();
                            loadHistoryForGeneralPdf(students);
                        } else {
                            setLoading(false);
                            Toast.makeText(AttendanceHistoryActivity.this,
                                    "Öğrenci listesi alınamadı: " + resp.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override public void onFailure(
                            Call<List<UserDto>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(AttendanceHistoryActivity.this,
                                "Ağ hatası (öğrenciler): " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void exportWeeklyPdf() {
        setLoading(true);

        ApiClient.courses().students(courseId)
                .enqueue(new Callback<List<UserDto>>() {
                    @Override public void onResponse(Call<List<UserDto>> call, Response<List<UserDto>> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            List<UserDto> students = resp.body();
                            loadHistoryForWeeklyPdf(students);
                        } else {
                            setLoading(false);
                            Toast.makeText(AttendanceHistoryActivity.this,
                                    "Öğrenci listesi alınamadı: " + resp.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override public void onFailure(Call<List<UserDto>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(AttendanceHistoryActivity.this,
                                "Ağ hatası (öğrenciler): " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadHistoryForWeeklyPdf(List<UserDto> students) {
        ApiClient.attendance().history(courseId)
                .enqueue(new Callback<List<SessionHistoryDto>>() {
                    @Override public void onResponse(Call<List<SessionHistoryDto>> call,
                                                     Response<List<SessionHistoryDto>> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            List<SessionHistoryDto> sessions = resp.body();
                            if (sessions.isEmpty()) {
                                setLoading(false);
                                Toast.makeText(AttendanceHistoryActivity.this,
                                        "Bu ders için hiç yoklama oturumu yok.",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            List<SessionHistoryDto> sessionsChrono = new java.util.ArrayList<>(sessions);
                            java.util.Collections.reverse(sessionsChrono);

                            int maxWeek = 0;
                            for (SessionHistoryDto s : sessionsChrono) {
                                int w = parseWeekNumber(s.getDescription());
                                if (w > maxWeek) maxWeek = w;
                            }

                            int computedWeeks = Math.max(maxWeek, sessionsChrono.size());
                            if (computedWeeks <= 0) computedWeeks = 1;

                            final int totalWeeksFinal = computedWeeks; // ✅ lambda hatası burada çözülüyor

                            final Map<Long, Integer> sessionWeekIndex =
                                    buildSessionWeekIndex(sessionsChrono, totalWeeksFinal);

                            final Map<Long, boolean[]> matrix = new HashMap<>();
                            for (UserDto u : students) {
                                if (u.getId() != null) matrix.put(u.getId(), new boolean[totalWeeksFinal]);
                            }

                            fetchWeeklyRecordsSequential(
                                    sessionsChrono,
                                    0,
                                    sessionWeekIndex,
                                    matrix,
                                    () -> {
                                        setLoading(false);
                                        buildAndSaveWeeklyPdf(students, totalWeeksFinal, matrix);
                                    }
                            );

                        } else {
                            setLoading(false);
                            Toast.makeText(AttendanceHistoryActivity.this,
                                    "Oturum listesi alınamadı: " + resp.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override public void onFailure(Call<List<SessionHistoryDto>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(AttendanceHistoryActivity.this,
                                "Ağ hatası (oturumlar): " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fetchWeeklyRecordsSequential(
            List<SessionHistoryDto> sessionsChrono,
            int index,
            Map<Long, Integer> sessionWeekIndex,
            Map<Long, boolean[]> matrix,
            Runnable onComplete
    ) {
        if (index >= sessionsChrono.size()) {
            onComplete.run();
            return;
        }

        SessionHistoryDto s = sessionsChrono.get(index);
        Long sessionId = s.getId();
        if (sessionId == null) {
            fetchWeeklyRecordsSequential(sessionsChrono, index + 1, sessionWeekIndex, matrix, onComplete);
            return;
        }

        ApiClient.attendance().sessionRecords(courseId, sessionId)
                .enqueue(new Callback<List<AttendanceRecordDto>>() {
                    @Override public void onResponse(Call<List<AttendanceRecordDto>> call,
                                                     Response<List<AttendanceRecordDto>> resp) {
                        Integer weekIdx = sessionWeekIndex.get(sessionId);

                        if (resp.isSuccessful() && resp.body() != null && weekIdx != null) {
                            for (AttendanceRecordDto r : resp.body()) {
                                Long studentId = r.getStudentId();
                                if (studentId == null) continue;

                                boolean[] arr = matrix.get(studentId);
                                if (arr == null) continue;

                                if (weekIdx >= 0 && weekIdx < arr.length) {
                                    arr[weekIdx] = true;
                                }
                            }
                        } else if (!resp.isSuccessful()) {
                            Toast.makeText(AttendanceHistoryActivity.this,
                                    "Oturum kayıtları alınamadı (id=" + sessionId + "): " + resp.code(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        fetchWeeklyRecordsSequential(sessionsChrono, index + 1, sessionWeekIndex, matrix, onComplete);
                    }

                    @Override public void onFailure(Call<List<AttendanceRecordDto>> call, Throwable t) {
                        Toast.makeText(AttendanceHistoryActivity.this,
                                "Ağ hatası (oturum id=" + sessionId + "): " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        fetchWeeklyRecordsSequential(sessionsChrono, index + 1, sessionWeekIndex, matrix, onComplete);
                    }
                });
    }

    private void buildAndSaveWeeklyPdf(List<UserDto> students,
                                       int totalWeeks,
                                       Map<Long, boolean[]> matrix) {

        List<AttendancePdf.WeeklyRow> rows = new ArrayList<>();

        for (UserDto u : students) {
            if (u.getId() == null) continue;

            long studentNo = (u.getUserName() != null) ? u.getUserName() : 0L;
            String fullName =
                    (u.getName() != null ? u.getName() : "") + " " +
                            (u.getSurname() != null ? u.getSurname() : "");
            fullName = fullName.trim();

            boolean[] weeks = matrix.get(u.getId());
            if (weeks == null) weeks = new boolean[totalWeeks];

            rows.add(new AttendancePdf.WeeklyRow(studentNo, fullName, weeks));
        }

        rows.sort((a, b) -> Long.compare(a.studentNo, b.studentNo));

        boolean ok = AttendancePdf.generateWeekly(
                this,
                courseName,
                courseCode,
                totalWeeks,
                rows
        );

        if (ok) {
            Toast.makeText(this,
                    "PDF oluşturuldu (İndirilenler klasörüne kaydedildi).",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,
                    "PDF oluşturulamadı.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private int parseWeekNumber(String desc) {
        if (desc == null) return -1;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d+)\\s*\\.?\\s*Hafta", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(desc);
        int last = -1;
        while (m.find()) {
            try { last = Integer.parseInt(m.group(1)); } catch (NumberFormatException ignore) {}
        }
        return last;
    }

    private Map<Long, Integer> buildSessionWeekIndex(List<SessionHistoryDto> sessionsChrono, int totalWeeks) {
        Map<Long, Integer> map = new HashMap<>();
        boolean[] used = new boolean[totalWeeks];

        for (SessionHistoryDto s : sessionsChrono) {
            Long id = s.getId();
            if (id == null) continue;

            int w = parseWeekNumber(s.getDescription());
            if (w > 0) {
                int idx = w - 1;
                if (idx >= 0 && idx < totalWeeks) {
                    map.put(id, idx);
                    used[idx] = true;
                }
            }
        }

        int next = 0;
        for (SessionHistoryDto s : sessionsChrono) {
            Long id = s.getId();
            if (id == null) continue;
            if (map.containsKey(id)) continue;

            while (next < totalWeeks && used[next]) next++;
            if (next >= totalWeeks) break;

            map.put(id, next);
            used[next] = true;
            next++;
        }
        return map;
    }

    private void loadHistoryForGeneralPdf(List<UserDto> students) {
        ApiClient.attendance().history(courseId)
                .enqueue(new Callback<List<SessionHistoryDto>>() {
                    @Override public void onResponse(
                            Call<List<SessionHistoryDto>> call,
                            Response<List<SessionHistoryDto>> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            List<SessionHistoryDto> sessions = resp.body();
                            if (sessions.isEmpty()) {
                                setLoading(false);
                                Toast.makeText(AttendanceHistoryActivity.this,
                                        "Bu ders için hiç yoklama oturumu yok.",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            Map<Long, Integer> presentCounts = new HashMap<>();
                            fetchSessionRecordsSequential(
                                    students,
                                    sessions,
                                    0,
                                    presentCounts,
                                    () -> {
                                        setLoading(false);
                                        buildAndSaveGeneralPdf(students, sessions.size(), presentCounts);
                                    }
                            );
                        } else {
                            setLoading(false);
                            Toast.makeText(AttendanceHistoryActivity.this,
                                    "Oturum listesi alınamadı: " + resp.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override public void onFailure(
                            Call<List<SessionHistoryDto>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(AttendanceHistoryActivity.this,
                                "Ağ hatası (oturumlar): " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fetchSessionRecordsSequential(
            List<UserDto> students,
            List<SessionHistoryDto> sessions,
            int index,
            Map<Long, Integer> presentCounts,
            Runnable onComplete
    ) {
        if (index >= sessions.size()) {
            onComplete.run();
            return;
        }

        SessionHistoryDto s = sessions.get(index);
        Long sessionId = s.getId();
        if (sessionId == null) {
            fetchSessionRecordsSequential(students, sessions, index + 1, presentCounts, onComplete);
            return;
        }

        ApiClient.attendance().sessionRecords(courseId, sessionId)
                .enqueue(new Callback<List<AttendanceRecordDto>>() {
                    @Override public void onResponse(
                            Call<List<AttendanceRecordDto>> call,
                            Response<List<AttendanceRecordDto>> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            for (AttendanceRecordDto r : resp.body()) {
                                Long sid = r.getStudentId();
                                if (sid == null) continue;
                                Integer cur = presentCounts.get(sid);
                                presentCounts.put(sid, cur == null ? 1 : cur + 1);
                            }
                        } else {
                            Toast.makeText(AttendanceHistoryActivity.this,
                                    "Oturum kayıtları alınamadı (id=" + sessionId + "): " + resp.code(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        fetchSessionRecordsSequential(students, sessions,
                                index + 1, presentCounts, onComplete);
                    }

                    @Override public void onFailure(
                            Call<List<AttendanceRecordDto>> call, Throwable t) {
                        Toast.makeText(AttendanceHistoryActivity.this,
                                "Ağ hatası (oturum id=" + sessionId + "): " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        fetchSessionRecordsSequential(students, sessions,
                                index + 1, presentCounts, onComplete);
                    }
                });
    }

    private void buildAndSaveGeneralPdf(List<UserDto> students,
                                        int totalSessions,
                                        Map<Long, Integer> presentCounts) {
        List<AttendancePdf.GeneralRow> rows = new ArrayList<>();

        for (UserDto u : students) {
            if (u.getId() == null) continue;

            long studentId = u.getId();
            int present = presentCounts.getOrDefault(studentId, 0);
            int absent = Math.max(0, totalSessions - present);

            long studentNo = (u.getUserName() != null) ? u.getUserName() : 0L;
            String fullName =
                    (u.getName() != null ? u.getName() : "") + " " +
                            (u.getSurname() != null ? u.getSurname() : "");
            fullName = fullName.trim();

            rows.add(new AttendancePdf.GeneralRow(
                    studentNo,
                    fullName,
                    totalSessions,
                    present,
                    absent
            ));
        }

        rows.sort((a, b) -> Long.compare(a.studentNo, b.studentNo));

        boolean ok = AttendancePdf.generateGeneral(
                this,
                courseName,
                courseCode,
                totalSessions,
                rows
        );

        if (ok) {
            Toast.makeText(this,
                    "PDF oluşturuldu (İndirilenler klasörüne kaydedildi).",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,
                    "PDF oluşturulamadı.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
