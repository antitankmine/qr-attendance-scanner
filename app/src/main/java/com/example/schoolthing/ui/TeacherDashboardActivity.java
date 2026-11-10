package com.example.schoolthing.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.example.schoolthing.data.local.ScanRecordEntity;
import com.example.schoolthing.data.repository.AttendanceRepository;
import com.example.schoolthing.databinding.ActivityTeacherDashboardBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Teacher Dashboard Activity for barebones classroom attendance view.
 */
public class TeacherDashboardActivity extends AppCompatActivity {

    private ActivityTeacherDashboardBinding binding;
    private AttendanceRepository repository;
    private final List<String> rosterList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeacherDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new AttendanceRepository(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rosterList);
        binding.listViewRoster.setAdapter(adapter);

        loadClassroomRoster();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClassroomRoster();
    }

    private void loadClassroomRoster() {
        rosterList.clear();
        List<ScanRecordEntity> scans = repository.getAllScans();

        int present = 0;
        int late = 0;
        int unexcused = 0;

        if (scans.isEmpty()) {
            rosterList.add("No attendance records logged yet.");
        } else {
            for (ScanRecordEntity scan : scans) {
                String timeStr = timeFormat.format(new Date(scan.getTimestamp()));
                String statusStr = scan.getAttendanceStatus();
                String badge = "•";

                if ("PRESENT".equalsIgnoreCase(statusStr)) {
                    badge = "✓";
                    present++;
                } else if ("LATE".equalsIgnoreCase(statusStr)) {
                    badge = "⚠️";
                    late++;
                } else {
                    badge = "✕";
                    unexcused++;
                }

                rosterList.add(badge + " " + scan.getStudentName() + " (" + scan.getStudentId() + ") — " + statusStr + " (" + timeStr + ")");
            }
        }

        binding.tvPresentCount.setText(String.valueOf(present));
        binding.tvLateCount.setText(String.valueOf(late));
        binding.tvUnexcusedCount.setText(String.valueOf(unexcused));

        adapter.notifyDataSetChanged();
    }
}
