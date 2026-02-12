package com.onrender.tutrnav;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        // Navigation Setup
        findViewById(R.id.navHome).setOnClickListener(v -> startActivity(new Intent(this, StudentHomeActivity.class)));
        findViewById(R.id.navSchedule).setOnClickListener(v -> startActivity(new Intent(this, ScheduleActivity.class)));
        findViewById(R.id.navMap).setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));

        // Dummy Data
        List<NotificationItem> list = new ArrayList<>();
        list.add(new NotificationItem("The Original Company", "Good morning students", "12 Jan", android.R.drawable.stat_sys_warning));
        list.add(new NotificationItem("Sushmita | Maths 11th", "This message is to remind you about last date to pay fee amount ₹600 is 15 Jan.", "12 Jan", android.R.drawable.ic_menu_save));
        list.add(new NotificationItem("Sumit | Swimming", "The swimming classes are canceled for next week due to an emergency.", "12 Jan", android.R.drawable.ic_menu_slideshow));
        list.add(new NotificationItem("Sourav | English 11th", "Today's homework is to revise the notes on clauses and phrases.", "12 Jan", android.R.drawable.ic_menu_slideshow));

        rvNotifications.setAdapter(new NotificationAdapter(list));
    }

    public static class NotificationItem {
        String title, body, date;
        int icon;
        public NotificationItem(String title, String body, String date, int icon) {
            this.title = title; this.body = body; this.date = date; this.icon = icon;
        }
    }

    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        List<NotificationItem> items;
        public NotificationAdapter(List<NotificationItem> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationItem item = items.get(position);
            holder.title.setText(item.title);
            holder.body.setText(item.body);
            holder.date.setText(item.date);
            holder.icon.setImageResource(item.icon);
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, body, date;
            ImageView icon;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvNotifTitle);
                body = itemView.findViewById(R.id.tvNotifBody);
                date = itemView.findViewById(R.id.tvNotifDate);
                icon = itemView.findViewById(R.id.imgNotifIcon);
            }
        }
    }
}