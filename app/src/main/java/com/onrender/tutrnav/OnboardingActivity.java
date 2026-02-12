package com.onrender.tutrnav;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MaterialButton btnTeacher, btnStudent;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Edge-to-Edge Setup
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Initialize Views
        viewPager = findViewById(R.id.viewPager);
        btnTeacher = findViewById(R.id.btnTeacher);
        btnStudent = findViewById(R.id.btnStudent);

        // 3. Setup Slideshow Data (5 Variants)
        List<OnboardingItem> sliderItems = new ArrayList<>();
        sliderItems.add(new OnboardingItem("Find Tutors Nearby", "Map-based search for local teachers", "#6A4BC3")); // Purple
        sliderItems.add(new OnboardingItem("Master New Skills", "From Music to Kung Fu", "#4CAF50")); // Green
        sliderItems.add(new OnboardingItem("Expert Guidance", "Verified teachers for best results", "#E91E63")); // Pink
        sliderItems.add(new OnboardingItem("Flexible Schedule", "Book classes at your convenience", "#FF9800")); // Orange
        sliderItems.add(new OnboardingItem("Track Progress", "See your improvement over time", "#2196F3")); // Blue

        // 4. Set Adapter
        viewPager.setAdapter(new OnboardingAdapter(sliderItems));

        // 5. Setup Auto-Looping Slideshow
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000); // Slide every 3 seconds
            }
        });

        // 6. Button Listeners
        btnStudent.setOnClickListener(v -> saveUserTypeAndProceed("student"));
        btnTeacher.setOnClickListener(v -> saveUserTypeAndProceed("teacher"));
    }

    // --- Helper to Save Choice and Move to Auth ---
    private void saveUserTypeAndProceed(String type) {
        // Save choice in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("TutrnavPrefs", MODE_PRIVATE);
        prefs.edit().putString("userType", type).apply();

        Toast.makeText(this, "Welcome " + type + "!", Toast.LENGTH_SHORT).show();

        // Move to Auth Activity
        Intent intent = new Intent(OnboardingActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }

    // --- Runnable for Auto-Sliding ---
    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int currentItem = viewPager.getCurrentItem();
            int totalItems = viewPager.getAdapter().getItemCount();

            // Loop back to 0 if at end
            if (currentItem < totalItems - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                viewPager.setCurrentItem(0);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    // =================================================================
    // INNER CLASSES: ADAPTER & MODEL (So you don't need extra files)
    // =================================================================

    // 1. Data Model
    public static class OnboardingItem {
        String title;
        String description;
        String colorHex;

        public OnboardingItem(String title, String description, String colorHex) {
            this.title = title;
            this.description = description;
            this.colorHex = colorHex;
        }
    }

    // 2. Adapter
    public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {
        private List<OnboardingItem> onBoardingItems;

        public OnboardingAdapter(List<OnboardingItem> onBoardingItems) {
            this.onBoardingItems = onBoardingItems;
        }

        @NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new OnboardingViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_onboarding, parent, false
                    )
            );
        }

        @Override
        public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
            holder.setOnboardingData(onBoardingItems.get(position));
        }

        @Override
        public int getItemCount() {
            return onBoardingItems.size();
        }

        // 3. View Holder
        class OnboardingViewHolder extends RecyclerView.ViewHolder {
            private TextView textTitle;
            private TextView textDescription;
            private ConstraintLayout container;
            private ImageView imageSlide;

            OnboardingViewHolder(@NonNull View itemView) {
                super(itemView);
                textTitle = itemView.findViewById(R.id.slideTitle);
                textDescription = itemView.findViewById(R.id.slideDesc);
                container = itemView.findViewById(R.id.container);
                imageSlide = itemView.findViewById(R.id.slideImage);
            }

            void setOnboardingData(OnboardingItem item) {
                textTitle.setText(item.title);
                textDescription.setText(item.description);
                container.setBackgroundColor(Color.parseColor(item.colorHex));

                // If you had different images for each slide, you would set them here
                // imageSlide.setImageResource(item.imageResId);
            }
        }
    }
}