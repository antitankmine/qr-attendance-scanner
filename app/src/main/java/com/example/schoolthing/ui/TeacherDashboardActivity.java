package com.example.schoolthing.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.schoolthing.data.model.AttendanceStatus;
import com.example.schoolthing.data.model.AttendanceType;
import com.example.schoolthing.data.repository.AttendanceRepository;
import com.example.schoolthing.databinding.ActivityTeacherDashboardBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Teacher Dashboard Activity for classroom attendance roster & exception management.
 */
public class TeacherDashboardActivity extends AppCompatActivity {

    private ActivityTeacherDashboardBinding binding;
    private AttendanceRepository repository;
    private final List<String> rosterList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeacherDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new AttendanceRepository(this);

        setupRosterData();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rosterList);
        binding.listViewRoster.setAdapter(adapter);

        binding.listViewRoster.setOnItemClickListener((parent, view, position, id) -> {
            String selected = rosterList.get(position);
            // Toggle manual exception check-in
            if (selected.contains("Alex Rivera")) {
                repository.recordScan("STU-2026-9812", "Alex Rivera", AttendanceType.CLASS_CHECKIN, AttendanceStatus.PRESENT);
                Toast.makeText(this, "Updated Alex Rivera to PRESENT", Toast.LENGTH_SHORT).show();
            } else if (selected.contains("Jordan Lee")) {
                repository.recordScan("STU-2026-3401", "Jordan Lee", AttendanceType.CLASS_CHECKIN, AttendanceStatus.LATE);
                Toast.makeText(this, "Updated Jordan Lee to LATE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRosterData() {
        rosterList.clear();
        rosterList.add("✅ Alex Rivera (STU-2026-9812) — Present (08:02 AM)");
        rosterList.add("⚠️ Jordan Lee (STU-2026-3401) — Late (08:16 AM)");
        rosterList.add("✅ Sam Taylor (STU-2026-5519) — Present (07:55 AM)");
        rosterList.add("❌ Morgan Vance (STU-2026-7720) — Unexcused Absence");
    }
}
