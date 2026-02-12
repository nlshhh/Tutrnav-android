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

public class ScheduleFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        RecyclerView rvSchedule = view.findViewById(R.id.rvSchedule);
        rvSchedule.setLayoutManager(new LinearLayoutManager(getContext()));

        List<ScheduleActivity.ScheduleItem> list = new ArrayList<>();
        list.add(new ScheduleActivity.ScheduleItem("Dr. Atul Mishra", "Bio Technology", "Class canceled due to emergency."));
        list.add(new ScheduleActivity.ScheduleItem("Sushmita", "Maths", "Test on Monday."));

        // Note: You might need to make ScheduleAdapter static or move it to a separate file
        rvSchedule.setAdapter(new ScheduleActivity().new ScheduleAdapter(list));

        return view;
    }
}