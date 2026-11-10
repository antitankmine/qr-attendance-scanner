package com.example.schoolthing;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.schoolthing.databinding.ActivityMainBinding;
import com.example.schoolthing.ui.AdminDashboardActivity;
import com.example.schoolthing.ui.GateGuardScannerActivity;
import com.example.schoolthing.ui.StudentQrActivity;
import com.example.schoolthing.ui.TeacherDashboardActivity;

/**
 * Main Portal Activity allowing selection of user roles.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cardStudent.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, StudentQrActivity.class)));

        binding.cardGateGuard.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, GateGuardScannerActivity.class)));

        binding.cardTeacher.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TeacherDashboardActivity.class)));

        binding.cardAdmin.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class)));
    }
}
