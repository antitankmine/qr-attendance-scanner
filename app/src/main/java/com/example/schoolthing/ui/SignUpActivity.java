package com.example.schoolthing.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.schoolthing.data.local.UserManager;
import com.example.schoolthing.data.model.UserRole;
import com.example.schoolthing.databinding.ActivitySignUpBinding;

/**
 * Sign Up Activity for registering new accounts with saved credentials.
 */
public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] roles = new String[]{
                UserRole.STUDENT.getDisplayName(),
                UserRole.GATE_GUARD.getDisplayName(),
                UserRole.TEACHER.getDisplayName(),
                UserRole.ADMINISTRATOR.getDisplayName()
        };
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        binding.spinnerRole.setAdapter(roleAdapter);

        binding.btnSignUp.setOnClickListener(v -> handleSignUp());

        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void handleSignUp() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String idNumber = binding.etIdNumber.getText() != null ? binding.etIdNumber.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        int selectedRoleIndex = binding.spinnerRole.getSelectedItemPosition();

        if (TextUtils.isEmpty(fullName)) {
            binding.etFullName.setError("Full name required");
            return;
        }

        if (TextUtils.isEmpty(idNumber)) {
            binding.etIdNumber.setError("ID number required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password required");
            return;
        }

        String roleStr = getRoleDisplayName(selectedRoleIndex);

        // Save credentials locally in SharedPreferences
        UserManager.registerUser(this, fullName, idNumber, email, password, roleStr);

        Toast.makeText(this, "Account registered! Welcome " + fullName, Toast.LENGTH_LONG).show();

        // Navigate to appropriate role dashboard
        Intent intent;
        switch (selectedRoleIndex) {
            case 0:
                intent = new Intent(this, StudentQrActivity.class);
                break;
            case 1:
                intent = new Intent(this, GateGuardScannerActivity.class);
                break;
            case 2:
                intent = new Intent(this, TeacherDashboardActivity.class);
                break;
            case 3:
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            default:
                intent = new Intent(this, LoginActivity.class);
                break;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private String getRoleDisplayName(int position) {
        switch (position) {
            case 0: return UserRole.STUDENT.getDisplayName();
            case 1: return UserRole.GATE_GUARD.getDisplayName();
            case 2: return UserRole.TEACHER.getDisplayName();
            case 3: return UserRole.ADMINISTRATOR.getDisplayName();
            default: return UserRole.STUDENT.getDisplayName();
        }
    }
}
