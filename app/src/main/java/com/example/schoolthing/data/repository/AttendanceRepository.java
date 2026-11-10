package com.example.schoolthing.data.repository;

import android.content.Context;
import com.example.schoolthing.data.local.AppDatabase;
import com.example.schoolthing.data.local.ScanRecordDao;
import com.example.schoolthing.data.local.ScanRecordEntity;
import com.example.schoolthing.data.model.AttendanceStatus;
import com.example.schoolthing.data.model.AttendanceType;
import com.example.schoolthing.data.remote.AttendanceApiService;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Repository enforcing offline-first scan queuing and background server sync.
 */
public class AttendanceRepository {

    private final ScanRecordDao scanRecordDao;
    private final AttendanceApiService apiService;

    public interface SyncCallback {
        void onSyncComplete(int syncedCount, String message);
        void onSyncFailed(String errorMessage);
    }

    public AttendanceRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.scanRecordDao = db.scanRecordDao();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080") // Local server endpoint for emulator / LAN
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.apiService = retrofit.create(AttendanceApiService.class);
    }

    public ScanRecordEntity recordScan(String studentId, String studentName, AttendanceType type, AttendanceStatus status) {
        ScanRecordEntity record = new ScanRecordEntity(
                studentId,
                studentName,
                System.currentTimeMillis(),
                type.name(),
                status.name(),
                false
        );
        long id = scanRecordDao.insert(record);
        record.setId(id);
        return record;
    }

    public List<ScanRecordEntity> getAllScans() {
        return scanRecordDao.getAllRecords();
    }

    public int getUnsyncedCount() {
        return scanRecordDao.getUnsyncedCount();
    }

    public void triggerSync(SyncCallback callback) {
        List<ScanRecordEntity> unsynced = scanRecordDao.getUnsyncedRecords();
        if (unsynced.isEmpty()) {
            if (callback != null) {
                callback.onSyncComplete(0, "No pending scans to sync.");
            }
            return;
        }

        apiService.syncScans(unsynced).enqueue(new Callback<AttendanceApiService.SyncResponse>() {
            @Override
            public void onResponse(Call<AttendanceApiService.SyncResponse> call, Response<AttendanceApiService.SyncResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Long> syncedIds = new ArrayList<>();
                    for (ScanRecordEntity entity : unsynced) {
                        syncedIds.add(entity.getId());
                    }
                    scanRecordDao.markRecordsAsSynced(syncedIds);
                    if (callback != null) {
                        callback.onSyncComplete(syncedIds.size(), "Successfully synced " + syncedIds.size() + " records.");
                    }
                } else {
                    // Even if network fails, scans remain safely queued in SQLite
                    if (callback != null) {
                        callback.onSyncFailed("Server error or offline. Records remain saved locally.");
                    }
                }
            }

            @Override
            public void onFailure(Call<AttendanceApiService.SyncResponse> call, Throwable t) {
                // Offline fallback: scans preserved safely in SQLite queue
                if (callback != null) {
                    callback.onSyncFailed("Network unavailable (" + t.getMessage() + "). Saved in offline queue.");
                }
            }
        });
    }
}
