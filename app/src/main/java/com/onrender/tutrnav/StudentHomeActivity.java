package com.onrender.tutrnav;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentHomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView tvName;
    private ImageView navHome, navSchedule, navMap, navNotif;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Edge-to-Edge
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // 2. Init Firebase
        mAuth = FirebaseAuth.getInstance();

        // 3. Init Views
        tvName = findViewById(R.id.tvName);
        CardView profileCard = findViewById(R.id.profileCard);
        navHome = findViewById(R.id.navHome);
        navSchedule = findViewById(R.id.navSchedule);
        navMap = findViewById(R.id.navMap);
        navNotif = findViewById(R.id.navNotif);
        viewPager = findViewById(R.id.viewPager);

        // 4. Load Data
        loadUserData();

        // 5. Setup ViewPager (Adapter)
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Optional: Preload all 4 fragments so they don't lag when swiping
        viewPager.setOffscreenPageLimit(3);

        // 6. --- SYNC LOGIC ---

        // A. Handle SWIPING (User swipes finger -> Update Icon)
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateNavIcons(position);
            }
        });

        // B. Handle CLICKS (User clicks Icon -> Smooth Scroll to Page)
        navHome.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        navSchedule.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        navMap.setOnClickListener(v -> viewPager.setCurrentItem(2, true));
        navNotif.setOnClickListener(v -> viewPager.setCurrentItem(3, true));

        // 7. Profile Click
        profileCard.setOnClickListener(v -> {
            startActivity(new Intent(StudentHomeActivity.this, ProfileActivity.class));
        });
    }

    // --- HELPER METHODS ---

    private void updateNavIcons(int position) {
        // Reset all to inactive
        resetIcon(navHome);
        resetIcon(navSchedule);
        resetIcon(navMap);
        resetIcon(navNotif);

        // Highlight selected
        switch (position) {
            case 0: highlightIcon(navHome); break;
            case 1: highlightIcon(navSchedule); break;
            case 2: highlightIcon(navMap); break;
            case 3: highlightIcon(navNotif); break;
        }
    }

    private void resetIcon(ImageView icon) {
        icon.setColorFilter(Color.parseColor("#5C6BC0"));
        icon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
    }

    private void highlightIcon(ImageView icon) {
        icon.setColorFilter(Color.parseColor("#FFCA28"));
        icon.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).start();
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String fullName = user.getDisplayName();
            if (fullName != null && !fullName.isEmpty()) {
                tvName.setText(fullName.split(" ")[0] + "!");
            } else {
                tvName.setText("Student!");
            }
        } else {
            tvName.setText("Guest!");
        }
    }

    // --- ADAPTER CLASS (Handles Fragment List) ---
    private class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new HomeFragment();
                case 1: return new ScheduleFragment();
                case 2: return new MapsFragment();
                case 3: return new NotificationsFragment();
                default: return new HomeFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4; // We have 4 tabs
        }
    }
}