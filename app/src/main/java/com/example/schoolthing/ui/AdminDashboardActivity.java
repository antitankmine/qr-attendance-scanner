package com.example.schoolthing.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.schoolthing.data.local.ScanRecordEntity;
import com.example.schoolthing.data.repository.AttendanceRepository;
import com.example.schoolthing.databinding.ActivityAdminDashboardBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Admin Console Activity for viewing audit logs, monitoring offline queue, and triggering sync.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private AttendanceRepository repository;
    private final List<String> logList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new AttendanceRepository(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, logList);
        binding.listViewAuditLogs.setAdapter(adapter);

        refreshDashboard();

        binding.btnTriggerSync.setOnClickListener(v -> {
            Toast.makeText(this, "Syncing pending scans with Spring Boot backend...", Toast.LENGTH_SHORT).show();
            repository.triggerSync(new AttendanceRepository.SyncCallback() {
                @Override
                public void onSyncComplete(int syncedCount, String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_LONG).show();
                        refreshDashboard();
                    });
                }

                @Override
                public void onSyncFailed(String errorMessage) {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminDashboardActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        refreshDashboard();
                    });
                }
            });
        });
    }

    private void refreshDashboard() {
        int unsyncedCount = repository.getUnsyncedCount();
        binding.tvUnsyncedQueueCount.setText("Offline Queue: " + unsyncedCount + " Pending Syncs");

        logList.clear();
        List<ScanRecordEntity> records = repository.getAllScans();
        if (records.isEmpty()) {
            logList.add("No scan activity recorded yet.");
        } else {
            for (ScanRecordEntity record : records) {
                String timeStr = dateFormat.format(new Date(record.getTimestamp()));
                String syncBadge = record.isSynced() ? "[SYNCED]" : "[QUEUED OFFLINE]";
                logList.add(syncBadge + " " + timeStr + " — " + record.getStudentName()
                        + " (" + record.getStudentId() + ") : " + record.getAttendanceStatus());
            }
        }
        adapter.notifyDataSetChanged();
    }
}
