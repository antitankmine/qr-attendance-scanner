package com.example.schoolthing.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.schoolthing.data.model.AttendanceStatus;
import com.example.schoolthing.data.model.AttendanceType;
import com.example.schoolthing.data.repository.AttendanceRepository;
import com.example.schoolthing.databinding.ActivityGateGuardScannerBinding;
import com.example.schoolthing.security.DynamicQrPass;
import com.example.schoolthing.security.QrCryptoUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gate Guard Camera Scanning Activity with CameraX and ZXing.
 */
public class GateGuardScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 101;
    private ActivityGateGuardScannerBinding binding;
    private AttendanceRepository repository;
    private Camera camera;
    private boolean isFlashOn = false;
    private ExecutorService cameraExecutor;
    private long lastScanTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGateGuardScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new AttendanceRepository(this);
        cameraExecutor = Executors.newSingleThreadExecutor();

        binding.btnToggleFlash.setOnClickListener(v -> toggleFlash());

        binding.btnSimulateValid.setOnClickListener(v -> {
            String payload = QrCryptoUtils.generatePayload("STU-2026-9812", "Alex Rivera");
            processQrCode(payload);
        });

        binding.btnSimulateLate.setOnClickListener(v -> {
            // Process late check-in
            handleScanResult("STU-2026-3401", "Jordan Lee", AttendanceStatus.LATE);
        });

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission required for QR scanning", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new QrCodeAnalyzer());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private class QrCodeAnalyzer implements ImageAnalysis.Analyzer {
        private final MultiFormatReader reader = new MultiFormatReader();

        @Override
        public void analyze(@NonNull ImageProxy image) {
            if (System.currentTimeMillis() - lastScanTime < 2000) {
                image.close();
                return;
            }

            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            int width = image.getWidth();
            int height = image.getHeight();

            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                    bytes, width, height, 0, 0, width, height, false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
                Result result = reader.decode(bitmap);
                if (result != null && result.getText() != null) {
                    lastScanTime = System.currentTimeMillis();
                    runOnUiThread(() -> processQrCode(result.getText()));
                }
            } catch (Exception ignored) {
            } finally {
                image.close();
            }
        }
    }

    private void processQrCode(String qrContent) {
        DynamicQrPass pass = QrCryptoUtils.parseAndVerify(qrContent);
        if (pass != null) {
            handleScanResult(pass.getStudentId(), pass.getStudentName(), AttendanceStatus.PRESENT);
        } else {
            handleScanResult("UNKNOWN", "Invalid or Expired QR", AttendanceStatus.INVALID);
        }
    }

    private void handleScanResult(String studentId, String studentName, AttendanceStatus status) {
        // Enqueue scan into Room database
        repository.recordScan(studentId, studentName, AttendanceType.CAMPUS_ENTRY, status);
        int unsyncedCount = repository.getUnsyncedCount();

        binding.cardFeedback.setVisibility(View.VISIBLE);
        binding.cardFeedback.setCardBackgroundColor(Color.parseColor(status.getHexColor()));

        if (status == AttendanceStatus.PRESENT) {
            binding.tvStatusTitle.setText("ENTRY APPROVED • ON TIME");
        } else if (status == AttendanceStatus.LATE) {
            binding.tvStatusTitle.setText("ENTRY RECORDED • LATE ARRIVAL");
        } else {
            binding.tvStatusTitle.setText("ACCESS DENIED • INVALID QR");
        }

        binding.tvStatusDetails.setText("Student: " + studentName + " (" + studentId + ")\nType: Campus Entry");
        binding.tvQueueStatus.setText("✓ Saved to SQLite queue (" + unsyncedCount + " unsynced)");
    }

    private void toggleFlash() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);
            binding.btnToggleFlash.setText(isFlashOn ? "Flash Off" : "Flash On");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
