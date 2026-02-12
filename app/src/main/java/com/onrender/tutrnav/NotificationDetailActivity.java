package com.onrender.tutrnav;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvBody = findViewById(R.id.tvDetailBody);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        ImageView btnDismiss = findViewById(R.id.btnDismiss);

        // Get Data from Intent
        String title = getIntent().getStringExtra("title");
        String body = getIntent().getStringExtra("body");
        String date = getIntent().getStringExtra("date");

        tvTitle.setText(title);
        tvBody.setText(body);
        tvDate.setText(date);

        // Close the screen when back is clicked
        btnDismiss.setOnClickListener(v -> finish());
    }
}