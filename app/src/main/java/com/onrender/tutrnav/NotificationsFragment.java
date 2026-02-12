package com.onrender.tutrnav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        RecyclerView rvNotifications = view.findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        List<NotificationsActivity.NotificationItem> list = new ArrayList<>();
        list.add(new NotificationsActivity.NotificationItem("The Original Company", "Good morning", "12 Jan", android.R.drawable.stat_sys_warning));
        // Add other dummy data...

        // Use the adapter from NotificationsActivity
        rvNotifications.setAdapter(new NotificationsActivity().new NotificationAdapter(list));

        return view;
    }
}