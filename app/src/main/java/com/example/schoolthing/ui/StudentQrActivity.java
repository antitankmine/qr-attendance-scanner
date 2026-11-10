package com.example.schoolthing.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;
import com.example.schoolthing.data.local.UserManager;
import com.example.schoolthing.databinding.ActivityStudentQrBinding;
import com.example.schoolthing.security.QrCryptoUtils;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * Student Pass Activity rendering dynamic time-sensitive QR code for the logged in student.
 */
public class StudentQrActivity extends AppCompatActivity {

    private ActivityStudentQrBinding binding;
    private CountDownTimer timer;
    private static final int WINDOW_SECONDS = 30;
    private String studentId;
    private String studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        studentName = UserManager.getSavedName(this);
        studentId = UserManager.getSavedId(this);

        binding.tvStudentName.setText("Student: " + studentName);
        binding.tvStudentId.setText("ID: " + studentId);

        startDynamicQrCycle();
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
