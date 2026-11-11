package com.example.schoolthing.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.schoolthing.R;
import com.example.schoolthing.data.model.SystemConfig;
import com.example.schoolthing.data.model.UserRole;
import com.example.schoolthing.data.remote.FirebaseManager;
import com.example.schoolthing.databinding.ActivityAdminDashboardBinding;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Admin Console Activity supporting interactive TimePicker threshold setters,
 * account creation/revocation, and active on-campus scan reports.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private final List<String> activeCampusList = new ArrayList<>();
    private ArrayAdapter<String> activeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, activeCampusList);
        binding.listViewActiveCampus.setAdapter(activeAdapter);

        binding.btnCreateAccount.setOnClickListener(v -> showCreateAccountDialog());
        binding.btnDeleteAccount.setOnClickListener(v -> showRevokeAccountDialog());
        binding.btnSaveThresholds.setOnClickListener(v -> saveThresholdSettings());

        binding.etLateThreshold.setOnClickListener(v -> showTimePickerDialog(binding.etLateThreshold));
        binding.etAbsentThreshold.setOnClickListener(v -> showTimePickerDialog(binding.etAbsentThreshold));

        loadActiveOnCampusReport();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveOnCampusReport();
    }

    private void showTimePickerDialog(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            String amPm = selectedHour >= 12 ? "PM" : "AM";
            int hour12 = selectedHour % 12;
            if (hour12 == 0) hour12 = 12;

            String timeStr = String.format(Locale.US, "%02d:%02d %s", hour12, selectedMinute, amPm);
            targetEditText.setText(timeStr);
        }, hour, minute, false);

        timePickerDialog.show();
    }

    private void showCreateAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Account");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_admin_account, null);
        EditText etName = dialogView.findViewById(R.id.etAdminName);
        EditText etId = dialogView.findViewById(R.id.etAdminId);
        EditText etEmail = dialogView.findViewById(R.id.etAdminEmail);
        EditText etPass = dialogView.findViewById(R.id.etAdminPassword);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerAdminRole);

        String[] roles = new String[]{
                UserRole.STUDENT.getDisplayName(),
                UserRole.GATE_GUARD.getDisplayName(),
                UserRole.TEACHER.getDisplayName(),
                UserRole.ADMINISTRATOR.getDisplayName()
        };
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        spinnerRole.setAdapter(roleAdapter);

        builder.setView(dialogView);
        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String id = etId.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            int rolePos = spinnerRole.getSelectedItemPosition();

            if (!name.isEmpty() && !id.isEmpty() && !email.isEmpty() && !pass.isEmpty()) {
                UserRole role = UserRole.values()[rolePos];
                FirebaseManager.getInstance().createAccount(this, name, id, email, pass, role, new FirebaseManager.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(AdminDashboardActivity.this, "Created account for " + name, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(AdminDashboardActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showRevokeAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Revoke Account");

        final EditText input = new EditText(this);
        input.setHint("Enter Student or Staff ID Number");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Revoke / Delete", (dialog, which) -> {
            String targetId = input.getText().toString().trim();
            if (!targetId.isEmpty()) {
                FirebaseManager.getInstance().revokeOrDeleteAccount(this, targetId, new FirebaseManager.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(AdminDashboardActivity.this, "Account " + targetId + " revoked successfully.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(AdminDashboardActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveThresholdSettings() {
        String lateTimeStr = binding.etLateThreshold.getText() != null ? binding.etLateThreshold.getText().toString().trim() : "08:15 AM";
        String absentTimeStr = binding.etAbsentThreshold.getText() != null ? binding.etAbsentThreshold.getText().toString().trim() : "08:30 AM";

        SystemConfig config = new SystemConfig(lateTimeStr, absentTimeStr);
        FirebaseManager.getInstance().updateThresholdConfig(this, config, new FirebaseManager.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(AdminDashboardActivity.this, "Cutoff time settings saved (" + lateTimeStr + " / " + absentTimeStr + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(AdminDashboardActivity.this, "Error saving settings: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadActiveOnCampusReport() {
        activeCampusList.clear();
        FirebaseManager.getInstance().fetchActiveOnCampus(this, new FirebaseManager.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                if (result.isEmpty()) {
                    activeCampusList.add("No active accounts currently on-campus.");
                } else {
                    activeCampusList.addAll(result);
                }
                activeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                activeCampusList.add("Error loading report.");
                activeAdapter.notifyDataSetChanged();
            }
        });
    }
}
