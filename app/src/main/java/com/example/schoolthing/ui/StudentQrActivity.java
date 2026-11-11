package com.example.schoolthing.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.schoolthing.data.local.ScanRecordEntity;
import com.example.schoolthing.data.local.UserManager;
import com.example.schoolthing.data.remote.FirebaseManager;
import com.example.schoolthing.data.repository.AttendanceRepository;
import com.example.schoolthing.databinding.ActivityStudentQrBinding;
import com.example.schoolthing.security.QrCryptoUtils;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Student Pass Activity rendering dynamic time-sensitive QR code,
 * password change modal, and personal attendance log history.
 */
public class StudentQrActivity extends AppCompatActivity {

    private ActivityStudentQrBinding binding;
    private CountDownTimer timer;
    private static final int WINDOW_SECONDS = 30;
    private String studentId;
    private String studentName;
    private AttendanceRepository repository;
    private final List<String> studentLogs = new ArrayList<>();
    private ArrayAdapter<String> logAdapter;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new AttendanceRepository(this);

        studentName = UserManager.getSavedName(this);
        studentId = UserManager.getSavedId(this);

        binding.tvStudentName.setText(studentName);
        binding.tvStudentId.setText("ID: " + studentId);

        logAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentLogs);
        binding.listViewStudentLogs.setAdapter(logAdapter);

        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        loadPersonalAttendanceHistory();
        startDynamicQrCycle();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        final EditText input = new EditText(this);
        input.setHint("Enter new password");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newPassword = input.getText().toString().trim();
            if (!newPassword.isEmpty()) {
                FirebaseManager.getInstance().changePassword(this, newPassword, new FirebaseManager.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(StudentQrActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(StudentQrActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadPersonalAttendanceHistory() {
        studentLogs.clear();
        List<ScanRecordEntity> allRecords = repository.getAllScans();

        for (ScanRecordEntity record : allRecords) {
            if (studentId.equalsIgnoreCase(record.getStudentId())) {
                String timeStr = dateFormat.format(new Date(record.getTimestamp()));
                studentLogs.add(timeStr + " — " + record.getAttendanceType() + " (" + record.getAttendanceStatus() + ")");
            }
        }

        if (studentLogs.isEmpty()) {
            studentLogs.add("No personal attendance records found.");
        }
        logAdapter.notifyDataSetChanged();
    }

    private void startDynamicQrCycle() {
        generateAndShowQr();

        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(WINDOW_SECONDS * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                binding.tvTimer.setText("Refreshes in: " + secondsLeft + "s");
                binding.progressBar.setProgress(secondsLeft);
            }

            @Override
            public void onFinish() {
                startDynamicQrCycle(); // Cycle to next dynamic payload
            }
        }.start();
    }

    private void generateAndShowQr() {
        String payload = QrCryptoUtils.generatePayload(studentId, studentName);
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(payload, BarcodeFormat.QR_CODE, 600, 600);
            binding.imgQrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
