package com.onrender.tutrnav;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    private RecyclerView rvSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvSchedule = findViewById(R.id.rvSchedule);
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));

        // Dummy Data
        List<ScheduleItem> list = new ArrayList<>();
        list.add(new ScheduleItem("Dr. Atul Mishra", "Bio Technology", "Class canceled due to emergency."));
        list.add(new ScheduleItem("Sushmita", "Maths", "Test on Monday."));
        rvSchedule.setAdapter(new ScheduleAdapter(list));

        // --- BOTTOM NAVIGATION ---

        // 1. Home
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentHomeActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // 2. Schedule (Here) - Do nothing

        // 3. Map (Make sure ID exists in activity_schedule.xml)
        // If ID isn't there, add android:id="@+id/navMap" to the 3rd icon
        findViewById(R.id.navMap).setOnClickListener(v -> {
            startActivity(new Intent(this, MapsActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // 4. Notifications (Make sure ID exists in activity_schedule.xml)
        findViewById(R.id.navNotif).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    // ... (Keep ScheduleItem and Adapter classes as they were) ...
    public static class ScheduleItem {
        String name, subject, message;
        public ScheduleItem(String name, String subject, String message) {
            this.name = name; this.subject = subject; this.message = message;
        }
    }

    public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
        List<ScheduleItem> items;
        public ScheduleAdapter(List<ScheduleItem> items) { this.items = items; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_schedule, p, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            ScheduleItem i = items.get(p); h.name.setText(i.name); h.subject.setText(i.subject); h.message.setText(i.message);
        }
        @Override public int getItemCount() { return items.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, subject, message;
            public ViewHolder(@NonNull View v) { super(v); name=v.findViewById(R.id.tvTeacherName); subject=v.findViewById(R.id.tvSubject); message=v.findViewById(R.id.tvMessage); }
        }
    }
}