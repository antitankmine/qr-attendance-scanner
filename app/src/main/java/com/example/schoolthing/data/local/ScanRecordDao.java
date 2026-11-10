package com.example.schoolthing.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

/**
 * Data Access Object for local scan queue.
 */
@Dao
public interface ScanRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ScanRecordEntity record);

    @Query("SELECT * FROM scan_records ORDER BY timestamp DESC")
    List<ScanRecordEntity> getAllRecords();

    @Query("SELECT * FROM scan_records WHERE isSynced = 0 ORDER BY timestamp ASC")
    List<ScanRecordEntity> getUnsyncedRecords();

    @Query("UPDATE scan_records SET isSynced = 1 WHERE id IN (:ids)")
    void markRecordsAsSynced(List<Long> ids);

    @Query("SELECT COUNT(*) FROM scan_records WHERE isSynced = 0")
    int getUnsyncedCount();

    @Query("DELETE FROM scan_records")
    void deleteAll();
}
