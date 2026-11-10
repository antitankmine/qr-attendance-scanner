package com.example.schoolthing.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Singleton Room Database for offline scan queue storage.
 */
@Database(entities = {ScanRecordEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "qr_attendance_db";
    private static volatile AppDatabase INSTANCE;

    public abstract ScanRecordDao scanRecordDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DB_NAME
                    ).allowMainThreadQueries().build();
                }
            }
        }
        return INSTANCE;
    }
}
