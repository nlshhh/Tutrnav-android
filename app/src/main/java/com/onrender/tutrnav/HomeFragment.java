package com.onrender.tutrnav;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 vpDiscover;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        vpDiscover = view.findViewById(R.id.vpDiscover);

        // 1. Create Data
        List<DiscoverModel> list = new ArrayList<>();
        list.add(new DiscoverModel("Physics Masterclass", "By Dr. Verma | ₹800/mo", R.mipmap.ic_launcher));
        list.add(new DiscoverModel("Learn Guitar", "Rock School | ₹1200/mo", R.mipmap.ic_launcher));
        list.add(new DiscoverModel("Math Wizards", "Grade 10-12 | ₹600/mo", R.mipmap.ic_launcher));
        list.add(new DiscoverModel("Coding Bootcamp", "Python & Java | ₹1500/mo", R.mipmap.ic_launcher));
        list.add(new DiscoverModel("Yoga for Beginners", "Morning Batch | ₹500/mo", R.mipmap.ic_launcher));

        // 2. Setup Infinite Adapter
        DiscoverAdapter adapter = new DiscoverAdapter(list);
        vpDiscover.setAdapter(adapter);

        // 3. Set Start Position in the Middle
        // We start at 1000 so the user can swipe LEFT immediately if they want
        int startPosition = list.size() * 100;
        vpDiscover.setCurrentItem(startPosition, false);

        // 4. THE STACK TRANSFORMER LOGIC
        vpDiscover.setOffscreenPageLimit(3); // Keep 3 cards in memory for the stack effect
        vpDiscover.setClipToPadding(false);
        vpDiscover.setClipChildren(false);
        vpDiscover.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        vpDiscover.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                // position 0 = Current Item
                // position 1 = Next Item (Right)
                // position -1 = Previous Item (Left)

                if (position >= 0) {
                    // PAGES BEHIND THE CURRENT ONE (The Stack)

                    // 1. Scale them down as they get further back
                    float scaleFactor = 0.85f + (1 - 0.85f) * (1 - Math.abs(position));
                    page.setScaleY(scaleFactor);
                    page.setScaleX(scaleFactor);

                    // 2. Move them to the left so they overlap the current card
                    // -page.getWidth() * position cancels out the default side-by-side layout
                    // + (40 * position) adds a small "peek" so you see the cards behind
                    page.setTranslationX(-page.getWidth() * position + (40 * position));

                    // 3. Adjust elevation so the current card is on top
                    page.setTranslationZ(-position);

                    // 4. Fade them out slightly
                    page.setAlpha(1 - (0.2f * position));

                } else {
                    // PAGE LEAVING TO THE LEFT (The one being swiped away)
                    // Keep it normal size but move it naturally
                    page.setScaleY(1f);
                    page.setScaleX(1f);
                    page.setTranslationX(0f);
                    page.setTranslationZ(0f);
                    page.setAlpha(1f);
                }
            }
        });

        // 5. Auto Scroll
        vpDiscover.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });

        return view;
    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (vpDiscover != null) {
                // Just go to next item. Since it's Infinite, we don't need to check limits.
                vpDiscover.setCurrentItem(vpDiscover.getCurrentItem() + 1);
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }
}