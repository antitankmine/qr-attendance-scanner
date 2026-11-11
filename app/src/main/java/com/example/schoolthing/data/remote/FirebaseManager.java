package com.example.schoolthing.data.remote;

import android.content.Context;
import com.example.schoolthing.data.local.AppDatabase;
import com.example.schoolthing.data.local.ScanRecordEntity;
import com.example.schoolthing.data.local.UserManager;
import com.example.schoolthing.data.model.SystemConfig;
import com.example.schoolthing.data.model.UserRole;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service orchestrating Firebase Auth, Firestore DB, and local fallback.
 */
public class FirebaseManager {

    private static FirebaseManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isFirebaseConfigured = false;

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    private FirebaseManager() {
        try {
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            isFirebaseConfigured = true;
        } catch (Exception e) {
            isFirebaseConfigured = false;
        }
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public void createAccount(Context context, String fullName, String idNumber, String email, String password, UserRole role, Callback<Boolean> callback) {
        if (isFirebaseConfigured && auth != null) {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser() != null ? authResult.getUser().getUid() : idNumber;
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", uid);
                        userMap.put("fullName", fullName);
                        userMap.put("idNumber", idNumber);
                        userMap.put("email", email);
                        userMap.put("role", role.getDisplayName());
                        userMap.put("status", "ACTIVE");
                        userMap.put("createdAt", System.currentTimeMillis());

                        db.collection("users").document(idNumber).set(userMap)
                                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    })
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            UserManager.registerUser(context, fullName, idNumber, email, password, role.getDisplayName());
            callback.onSuccess(true);
        }
    }

    public void revokeOrDeleteAccount(Context context, String idNumber, Callback<Boolean> callback) {
        if (isFirebaseConfigured && db != null) {
            db.collection("users").document(idNumber).delete()
                    .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            UserManager.clearUser(context);
            callback.onSuccess(true);
        }
    }

    public void changePassword(Context context, String newPassword, Callback<Boolean> callback) {
        if (isFirebaseConfigured && auth != null && auth.getCurrentUser() != null) {
            auth.getCurrentUser().updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> {
                        UserManager.registerUser(context,
                                UserManager.getSavedName(context),
                                UserManager.getSavedId(context),
                                UserManager.getSavedEmail(context),
                                newPassword,
                                UserManager.getSavedRole(context));
                        callback.onSuccess(true);
                    })
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            UserManager.registerUser(context,
                    UserManager.getSavedName(context),
                    UserManager.getSavedId(context),
                    UserManager.getSavedEmail(context),
                    newPassword,
                    UserManager.getSavedRole(context));
            callback.onSuccess(true);
        }
    }

    public void logScan(Context context, String studentId, String studentName, String type, String status, Callback<Boolean> callback) {
        long now = System.currentTimeMillis();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(now));

        ScanRecordEntity entity = new ScanRecordEntity(studentId, studentName, now, type, status, false);
        AppDatabase.getInstance(context).scanRecordDao().insert(entity);

        if (isFirebaseConfigured && db != null) {
            Map<String, Object> scanMap = new HashMap<>();
            scanMap.put("studentId", studentId);
            scanMap.put("studentName", studentName);
            scanMap.put("timestamp", now);
            scanMap.put("dateStr", dateStr);
            scanMap.put("attendanceType", type);
            scanMap.put("attendanceStatus", status);

            db.collection("attendance").add(scanMap)
                    .addOnSuccessListener(docRef -> callback.onSuccess(true))
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            callback.onSuccess(true);
        }
    }

    public void fetchActiveOnCampus(Context context, Callback<List<String>> callback) {
        List<ScanRecordEntity> allScans = AppDatabase.getInstance(context).scanRecordDao().getAllRecords();
        Map<String, String> campusState = new HashMap<>();

        for (ScanRecordEntity scan : allScans) {
            String studentKey = scan.getStudentName() + " (" + scan.getStudentId() + ")";
            if ("CAMPUS_ENTRY".equalsIgnoreCase(scan.getAttendanceType())) {
                campusState.put(studentKey, "IN_CAMPUS");
            } else if ("CAMPUS_EXIT".equalsIgnoreCase(scan.getAttendanceType())) {
                campusState.remove(studentKey);
            }
        }

        List<String> activeOnCampus = new ArrayList<>(campusState.keySet());
        callback.onSuccess(activeOnCampus);
    }

    public void updateThresholdConfig(Context context, SystemConfig config, Callback<Boolean> callback) {
        if (isFirebaseConfigured && db != null) {
            Map<String, Object> configMap = new HashMap<>();
            configMap.put("lateThresholdTime", config.getLateThresholdTime());
            configMap.put("absentThresholdTime", config.getAbsentThresholdTime());

            db.collection("config").document("attendance_thresholds").set(configMap)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            callback.onSuccess(true);
        }
    }
}
