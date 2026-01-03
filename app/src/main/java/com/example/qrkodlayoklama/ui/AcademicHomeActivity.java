package com.example.qrkodlayoklama.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.ui.course.CourseCreateActivity;
import com.example.qrkodlayoklama.ui.course.CourseListActivity;

public class AcademicHomeActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_home);
        setupToolbar("Akademisyen", false);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        Button btnCreateCourse = findViewById(R.id.btnCreateCourse);
        btnCreateCourse.setOnClickListener(v ->
                startActivity(new Intent(this, CourseCreateActivity.class))
        );

        Button btnMyCourses = findViewById(R.id.btnMyCourses);
        btnMyCourses.setOnClickListener(v -> {
            startActivity(new Intent(this, CourseListActivity.class));
        });
    }
}
