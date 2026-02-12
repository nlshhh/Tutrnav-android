package com.onrender.tutrnav;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentHomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView tvName;
    private ImageView navHome, navSchedule, navMap, navNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Enable Edge-to-Edge (Draw behind system bars)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_home);

        // 2. Handle Window Insets (Padding for Status Bar only)
        // We do not pad the bottom, so the navigation bar sits nicely at the bottom
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // 3. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // 4. Initialize Views
        tvName = findViewById(R.id.tvName);
        CardView profileCard = findViewById(R.id.profileCard);

        // Nav Icons
        navHome = findViewById(R.id.navHome);
        navSchedule = findViewById(R.id.navSchedule);
        navMap = findViewById(R.id.navMap);
        navNotif = findViewById(R.id.navNotif);

        // 5. Load User Data (Name)
        loadUserData();

        // 6. Profile Click Listener
        profileCard.setOnClickListener(v -> {
            startActivity(new Intent(StudentHomeActivity.this, ProfileActivity.class));
        });

        // 7. --- NAVIGATION LOGIC ---

        // Default: Load Home Fragment
        loadFragment(new HomeFragment());
        updateNavIcons(navHome);

        // Click Listeners with Animations
        navHome.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            updateNavIcons(navHome);
        });

        navSchedule.setOnClickListener(v -> {
            loadFragment(new ScheduleFragment());
            updateNavIcons(navSchedule);
        });

        navMap.setOnClickListener(v -> {
            loadFragment(new MapsFragment());
            updateNavIcons(navMap);
        });

        navNotif.setOnClickListener(v -> {
            loadFragment(new NotificationsFragment());
            updateNavIcons(navNotif);
        });
    }

    /**
     * Swaps the current fragment in the container with a Fade Animation.
     */
    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    /**
     * Updates the icons with a scaling animation and color change.
     */
    private void updateNavIcons(ImageView selected) {
        // Define colors
        int activeColor = Color.parseColor("#FFCA28"); // Gold
        int inactiveColor = Color.parseColor("#5C6BC0"); // Light Purple/Grey

        ImageView[] icons = {navHome, navSchedule, navMap, navNotif};

        for (ImageView icon : icons) {
            if (icon == selected) {
                // Selected: Gold Color + Scale Up
                icon.setColorFilter(activeColor);
                icon.animate()
                        .scaleX(1.3f)
                        .scaleY(1.3f)
                        .setDuration(200)
                        .start();
            } else {
                // Unselected: Light Purple + Reset Scale
                icon.setColorFilter(inactiveColor);
                icon.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(200)
                        .start();
            }
        }
    }

    /**
     * Loads the user's first name from Firebase.
     */
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String fullName = user.getDisplayName();
            if (fullName != null && !fullName.isEmpty()) {
                // Split string to get just the first name
                String firstName = fullName.split(" ")[0];
                tvName.setText(firstName + "!");
            } else {
                tvName.setText("Student!");
            }
        } else {
            // Optional: Handle case where user is not logged in (e.g., redirect to login)
            tvName.setText("Guest!");
        }
    }
}