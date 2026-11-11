package com.example.schoolthing.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.schoolthing.R;
import com.example.schoolthing.data.local.ScanRecordEntity;
import com.example.schoolthing.data.model.UserRole;
import com.example.schoolthing.data.remote.FirebaseManager;
import com.example.schoolthing.data.repository.AttendanceRepository;
import com.example.schoolthing.databinding.ActivityTeacherDashboardBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Teacher Dashboard Activity for student account creation, QR lookup, and attendance search.
 */
public class TeacherDashboardActivity extends AppCompatActivity {

    private ActivityTeacherDashboardBinding binding;
    private AttendanceRepository repository;
    private final List<String> rosterList = new ArrayList<>();
    private final List<ScanRecordEntity> allScans = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeacherDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new AttendanceRepository(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rosterList);
        binding.listViewRoster.setAdapter(adapter);

        binding.btnCreateStudent.setOnClickListener(v -> showCreateStudentDialog());
        binding.btnScanQrLookup.setOnClickListener(v -> startActivity(new Intent(this, GateGuardScannerActivity.class)));

        binding.etStudentSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRoster(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadClassroomRoster();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClassroomRoster();
    }

    private void showCreateStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Student Account");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_student, null);
        EditText etName = dialogView.findViewById(R.id.etStudentName);
        EditText etId = dialogView.findViewById(R.id.etStudentId);
        EditText etEmail = dialogView.findViewById(R.id.etStudentEmail);
        EditText etPass = dialogView.findViewById(R.id.etStudentPassword);

        builder.setView(dialogView);
        builder.setPositiveButton("Create Account", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String id = etId.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            if (!name.isEmpty() && !id.isEmpty() && !email.isEmpty() && !pass.isEmpty()) {
                FirebaseManager.getInstance().createAccount(this, name, id, email, pass, UserRole.STUDENT, new FirebaseManager.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(TeacherDashboardActivity.this, "Student account created for " + name, Toast.LENGTH_SHORT).show();
                        loadClassroomRoster();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(TeacherDashboardActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadClassroomRoster() {
        allScans.clear();
        allScans.addAll(repository.getAllScans());
        filterRoster(binding.etStudentSearch.getText() != null ? binding.etStudentSearch.getText().toString() : "");
    }

    private void filterRoster(String query) {
        rosterList.clear();
        String filter = query.trim().toLowerCase(Locale.US);

        for (ScanRecordEntity scan : allScans) {
            boolean matchesName = scan.getStudentName() != null && scan.getStudentName().toLowerCase(Locale.US).contains(filter);
            boolean matchesId = scan.getStudentId() != null && scan.getStudentId().toLowerCase(Locale.US).contains(filter);

            if (filter.isEmpty() || matchesName || matchesId) {
                String timeStr = timeFormat.format(new Date(scan.getTimestamp()));
                rosterList.add(scan.getStudentName() + " (" + scan.getStudentId() + ") — " + scan.getAttendanceType() + " [" + scan.getAttendanceStatus() + "] (" + timeStr + ")");
            }
        }

        if (rosterList.isEmpty()) {
            rosterList.add("No attendance records found.");
        }
        adapter.notifyDataSetChanged();
    }
}
