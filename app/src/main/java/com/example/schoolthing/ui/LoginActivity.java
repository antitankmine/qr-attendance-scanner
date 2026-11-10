package com.example.schoolthing.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.schoolthing.MainActivity;
import com.example.schoolthing.data.local.UserManager;
import com.example.schoolthing.data.model.UserRole;
import com.example.schoolthing.databinding.ActivityLoginBinding;

/**
 * Login Activity for user authentication and role routing.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check if user account exists. If not, require Sign Up on first launch.
        if (!UserManager.isUserRegistered(this)) {
            Toast.makeText(this, "Please create a new account to get started.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SignUpActivity.class));
        }

        String[] roles = new String[]{
                UserRole.STUDENT.getDisplayName(),
                UserRole.GATE_GUARD.getDisplayName(),
                UserRole.TEACHER.getDisplayName(),
                UserRole.ADMINISTRATOR.getDisplayName()
        };
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        binding.spinnerRole.setAdapter(roleAdapter);

        // Pre-fill email if registered
        String savedEmail = UserManager.getSavedEmail(this);
        if (!savedEmail.isEmpty()) {
            binding.etEmail.setText(savedEmail);
        }

        binding.btnLogin.setOnClickListener(v -> handleLogin());

        binding.tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update pre-filled email if newly registered
        String savedEmail = UserManager.getSavedEmail(this);
        if (!savedEmail.isEmpty() && binding.etEmail.getText().toString().isEmpty()) {
            binding.etEmail.setText(savedEmail);
        }
    }

    private void handleLogin() {
        String emailOrId = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        int rolePosition = binding.spinnerRole.getSelectedItemPosition();

        if (TextUtils.isEmpty(emailOrId)) {
            binding.etEmail.setError("Email or ID required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password required");
            return;
        }

        if (!UserManager.isUserRegistered(this)) {
            Toast.makeText(this, "No registered account found. Please sign up first.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SignUpActivity.class));
            return;
        }

        if (!UserManager.validateLogin(this, emailOrId, password)) {
            Toast.makeText(this, "Invalid credentials. Please check email/ID and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = UserManager.getSavedName(this);
        Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();

        Intent intent;
        switch (rolePosition) {
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
                intent = new Intent(this, MainActivity.class);
                break;
        }
        startActivity(intent);
    }
}
