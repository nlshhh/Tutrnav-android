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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentHomeActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;

    // UI Components
    private TextView tvName;
    private ImageView imgProfileSmall; // The image inside the card
    private CardView profileCard;
    private ImageView navHome, navSchedule, navMap, navNotif;
    private ViewPager2 viewPager;

    // Colors
    private final int COLOR_ACTIVE = Color.parseColor("#FFCA28"); // Gold/Yellow
    private final int COLOR_INACTIVE = Color.parseColor("#5C6BC0"); // Muted Purple

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_home);

        setupWindowInsets();

        // 1. Init Firebase
        mAuth = FirebaseAuth.getInstance();

        // 2. Init Views
        initViews();

        // 3. Setup ViewPager (Tabs)
        setupViewPager();

        // 4. Setup Click Listeners (Navigation & Profile)
        setupClickListeners();

        // 5. Set Initial State (Highlight Home)
        updateNavUI(0);
    }

    /**
     * Called every time the activity comes to the foreground.
     * Essential for updating the Profile Image/Name after returning from ProfileActivity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Only apply top/left/right padding, leave bottom for the floating nav
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        profileCard = findViewById(R.id.profileCard);
        imgProfileSmall = findViewById(R.id.imgProfileSmall); // ID from your XML

        navHome = findViewById(R.id.navHome);
        navSchedule = findViewById(R.id.navSchedule);
        navMap = findViewById(R.id.navMap);
        navNotif = findViewById(R.id.navNotif);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3); // Keep all tabs in memory for smooth performance

        // Sync Swipe with Icons
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateNavUI(position);
            }
        });
    }

    private void setupClickListeners() {
        // Navigation Clicks (Smooth Scroll)
        navHome.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        navSchedule.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        navMap.setOnClickListener(v -> viewPager.setCurrentItem(2, true));
        navNotif.setOnClickListener(v -> viewPager.setCurrentItem(3, true));

        // Profile Card Click
        profileCard.setOnClickListener(v -> {
            Intent intent = new Intent(StudentHomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            // No finish() here, because we want to come back
        });
    }

    // --- DATA LOADING ---

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // 1. Set Name (First name only for style)
            String fullName = user.getDisplayName();
            if (fullName != null && !fullName.isEmpty()) {
                // Split "John Doe" -> "John!"
                tvName.setText(fullName.split(" ")[0] + "!");
            } else {
                tvName.setText("Student!");
            }

            // 2. Load Image with Glide
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.mipmap.ic_launcher) // Default while loading
                        .error(R.mipmap.ic_launcher)       // Default if error
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache efficiently
                        .into(imgProfileSmall);
            }
        } else {
            // Fallback for Guest/Error
            tvName.setText("Guest!");
            imgProfileSmall.setImageResource(R.mipmap.ic_launcher);
        }
    }

    // --- UI ANIMATIONS ---

    private void updateNavUI(int position) {
        // 1. Reset all icons to default state
        animateIcon(navHome, false);
        animateIcon(navSchedule, false);
        animateIcon(navMap, false);
        animateIcon(navNotif, false);

        // 2. Highlight the selected one
        switch (position) {
            case 0: animateIcon(navHome, true); break;
            case 1: animateIcon(navSchedule, true); break;
            case 2: animateIcon(navMap, true); break;
            case 3: animateIcon(navNotif, true); break;
        }
    }

    private void animateIcon(ImageView icon, boolean isActive) {
        if (isActive) {
            icon.setColorFilter(COLOR_ACTIVE);
            icon.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).start();
        } else {
            icon.setColorFilter(COLOR_INACTIVE);
            icon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
        }
    }

    // --- FRAGMENT ADAPTER ---

    private static class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Ensure you have created these Fragment Java classes!
            // Right Click Package -> New -> Fragment -> Blank Fragment
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
            return 4;
        }
    }
}