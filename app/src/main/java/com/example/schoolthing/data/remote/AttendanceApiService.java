package com.example.schoolthing.data.remote;

import com.example.schoolthing.data.local.ScanRecordEntity;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit interface for sync communication with self-hosted Java Spring Boot backend.
 */
public interface AttendanceApiService {

    class SyncResponse {
        private boolean success;
        private String message;
        private List<Long> syncedIds;

        public SyncResponse(boolean success, String message, List<Long> syncedIds) {
            this.success = success;
            this.message = message;
            this.syncedIds = syncedIds;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public List<Long> getSyncedIds() {
            return syncedIds;
        }
    }

    @POST("/api/v1/attendance/sync")
    Call<SyncResponse> syncScans(@Body List<ScanRecordEntity> scanRecords);
}
