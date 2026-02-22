package com.onrender.tutrnav;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TeacherHomeActivity extends AppCompatActivity {

    // --- UI Components ---
    // FrameLayouts act as the expanded clickable areas for better UX
    private FrameLayout navDashboard, navMyTuition, navSchedule;
    // ImageViews act as the visual icons that we will animate and tint
    private ImageView iconDash, iconTuition, iconSchedule;

    private ViewPager2 viewPager;
    private TextView tvGreeting, tvStatus;
    private ImageView imgProfileSmall;
    private View cardProfile;

    // --- Configuration ---
    private final int COLOR_ACTIVE = Color.parseColor("#FFCA28"); // Golden Yellow
    private final int COLOR_INACTIVE = Color.parseColor("#9FA8DA"); // Muted Indigo

    // --- Data Engine ---
    private TeacherViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_home);

        // Handle immersive edge-to-edge screens gracefully
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // 🔥 THE LEGENDARY UPGRADE: Initialize the Shared ViewModel immediately.
        viewModel = new ViewModelProvider(this).get(TeacherViewModel.class);

        initViews();
        setupViewPager();
        loadProfile();
        setupSmartBackPress();

        // Profile Click Listener
        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
    }

    private void initViews() {
        // Find Clickable Frames
        navDashboard = findViewById(R.id.navDashboard);
        navMyTuition = findViewById(R.id.navMyTuition);
        navSchedule = findViewById(R.id.navSchedule);

        // Find Animatable Icons
        iconDash = findViewById(R.id.iconDash);
        iconTuition = findViewById(R.id.iconTuition);
        iconSchedule = findViewById(R.id.iconSchedule);

        // Find Header Elements
        viewPager = findViewById(R.id.viewPager);
        imgProfileSmall = findViewById(R.id.imgProfileSmall);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvStatus = findViewById(R.id.tvStatus);
        cardProfile = findViewById(R.id.cardProfile);

        // Click Listeners for Custom Bottom Navigation
        navDashboard.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        navMyTuition.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        navSchedule.setOnClickListener(v -> viewPager.setCurrentItem(2, true));
    }

    private void setupViewPager() {
        TeacherPagerAdapter adapter = new TeacherPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Keeps all 3 fragments alive in memory.
        viewPager.setOffscreenPageLimit(3);

        // Removed the RecyclerView dependency by directly using Android's built-in View constant
        viewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateNavUI(position);
            }
        });
    }

    private void updateNavUI(int pos) {
        // Animate the icons based on the currently selected ViewPager position
        animateIcon(iconDash, pos == 0);
        animateIcon(iconTuition, pos == 1);
        animateIcon(iconSchedule, pos == 2);
    }

    private void animateIcon(ImageView icon, boolean isActive) {
        // Change color smoothly
        icon.setColorFilter(isActive ? COLOR_ACTIVE : COLOR_INACTIVE);

        // Pop animation (scales up to 1.2x if active, back to 1.0x if inactive)
        icon.animate()
                .scaleX(isActive ? 1.25f : 1.0f)
                .scaleY(isActive ? 1.25f : 1.0f)
                .setDuration(250)
                .start();
    }

    private void loadProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();

            // 🔥 FIX: Added so it correctly extracts the First Name as a String!
            String firstName = (name != null && !name.trim().isEmpty()) ? name.split(" ")[0] : (user.getPhoneNumber() != null ? user.getPhoneNumber().split(" ")[0] : "Teacher");

            if (tvGreeting != null) tvGreeting.setText("Hi " + firstName + "!");
            if (tvStatus != null) tvStatus.setText("You are Online");

            // Load Circular Profile Image
            if (user.getPhotoUrl() != null && imgProfileSmall != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.mipmap.ic_launcher)
                        .into(imgProfileSmall);
            }
        }
    }

    /**
     * Smart Back Press Logic:
     * If the user is on the "My Tuition" or "Schedule" tab, pressing the system Back button
     * will take them to the "Dashboard" (Tab 0) instead of closing the app immediately.
     */
    private void setupSmartBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewPager.getCurrentItem() != 0) {
                    // Route back to Dashboard
                    viewPager.setCurrentItem(0, true);
                } else {
                    // Allow normal exit
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // ==========================================
    //           VIEW PAGER ADAPTER
    // ==========================================
    private static class TeacherPagerAdapter extends FragmentStateAdapter {
        public TeacherPagerAdapter(@NonNull FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new TeacherDashboardFragment();
                case 1: return new TeacherTuitionFragment();
                case 2: return new TeacherScheduleFragment();
                default: return new TeacherDashboardFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}