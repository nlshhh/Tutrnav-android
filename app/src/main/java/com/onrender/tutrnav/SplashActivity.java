package com.onrender.tutrnav;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Wait 2 seconds, then decide where to go
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // 1. Check if user is logged in
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                // User is logged in -> Go to Home
                startActivity(new Intent(SplashActivity.this, StudentHomeActivity.class));
            } else {
                // User is NOT logged in -> Check if they saw Onboarding before
                SharedPreferences prefs = getSharedPreferences("TutrnavPrefs", MODE_PRIVATE);
                boolean isFirstTime = prefs.getString("userType", "").isEmpty();

                if (isFirstTime) {
                    // Go to Onboarding
                    startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                } else {
                    // Use has selected type but logged out -> Go to Auth directly
                    startActivity(new Intent(SplashActivity.this, AuthActivity.class));
                }
            }

            finish(); // Close Splash
        }, 2000);
    }
}