package com.example.qrkodlayoklama.ui.course;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.CourseCreateRequest;
import com.example.qrkodlayoklama.data.remote.model.CourseDto;
import com.example.qrkodlayoklama.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseCreateActivity extends BaseActivity {

    private EditText etCourseName, etCourseCode;
    private Button btnSave, btnAddStudent;
    private ProgressBar progress;
    private RecyclerView recycler;
    private SelectedStudentAdapter adapter;

    private final ArrayList<Long> selectedStudentIds = new ArrayList<>();

    private ActivityResultLauncher<Intent> pickStudentsLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_create);
        setupToolbar("Ders Oluştur", true);

        etCourseName = findViewById(R.id.etCourseName);
        etCourseCode = findViewById(R.id.etCourseCode);
        btnSave = findViewById(R.id.btnSave);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        progress = findViewById(R.id.progress);
        recycler = findViewById(R.id.recycler);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectedStudentAdapter();
        recycler.setAdapter(adapter);

        adapter.setOnRemoveListener(removedId -> {
            for (int i = 0; i < selectedStudentIds.size(); i++) {
                if (selectedStudentIds.get(i) == removedId) {
                    selectedStudentIds.remove(i);
                    break;
                }
            }
            adapter.removeById(removedId);
        });

        pickStudentsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        long[] idsArr   = result.getData().getLongArrayExtra(StudentPickerActivity.RESULT_SELECTED_IDS);
                        String[] names  = result.getData().getStringArrayExtra(StudentPickerActivity.RESULT_SELECTED_NAMES);

                        selectedStudentIds.clear();

                        List<SelectedStudentAdapter.StudentUi> uiList = new ArrayList<>();

                        if (idsArr != null) {
                            for (int i = 0; i < idsArr.length; i++) {
                                long id = idsArr[i];
                                selectedStudentIds.add(id);
                                String nm = (names != null && i < names.length) ? names[i] : ("Öğrenci #" + id);
                                uiList.add(new SelectedStudentAdapter.StudentUi(id, nm));
                            }
                        }
                        adapter.setItems(uiList);
                    }
                }
        );

        btnAddStudent.setOnClickListener(v -> {
            String name = etCourseName.getText().toString().trim();
            String code = etCourseCode.getText().toString().trim();

            Intent i = new Intent(this, StudentPickerActivity.class);
            i.putExtra(StudentPickerActivity.EXTRA_DRAFT_NAME, name);
            i.putExtra(StudentPickerActivity.EXTRA_DRAFT_CODE, code);

            long[] preIds;
            preIds = adapter.getIdArray();
            i.putExtra(StudentPickerActivity.EXTRA_PRESELECTED_IDS, preIds);

            pickStudentsLauncher.launch(i);
        });


        btnSave.setOnClickListener(v -> doSave());
    }

    private void setLoading(boolean b) {
        if (progress != null)      progress.setVisibility(b ? View.VISIBLE : View.GONE);
        if (btnAddStudent != null) btnAddStudent.setEnabled(!b);
        if (etCourseName != null)  etCourseName.setEnabled(!b);
        if (etCourseCode != null)  etCourseCode.setEnabled(!b);
    }

    private long[] toLongArray(java.util.List<Long> list) {
        long[] arr = new long[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private void doSave() {
        String name = etCourseName.getText().toString().trim();
        String code = etCourseCode.getText().toString().trim();

        if (name.isEmpty() || code.isEmpty() || selectedStudentIds.isEmpty()) {
            Toast.makeText(this, "Ders adı/kodu ve en az 1 öğrenci gerekli", Toast.LENGTH_SHORT).show(); return;
        }

        setLoading(true);

        ApiClient.courses()
                .create(new CourseCreateRequest(name, code, new ArrayList<>(selectedStudentIds)))
                .enqueue(new Callback<CourseDto>() {
                    @Override public void onResponse(Call<CourseDto> call, Response<CourseDto> resp) {
                        setLoading(false);
                        if (resp.isSuccessful() && resp.body() != null) {
                            Toast.makeText(CourseCreateActivity.this, "Ders kaydedildi", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CourseCreateActivity.this, "Hata: " + resp.code(), Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override public void onFailure(Call<CourseDto> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(CourseCreateActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
