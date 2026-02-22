package com.onrender.tutrnav;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherScheduleFragment extends Fragment {

    // UI
    private RecyclerView rvSchedule;
    private ChipGroup chipGroup;
    private ExtendedFloatingActionButton fabBroadcast;
    private LinearLayout layoutEmptyState, layoutStandardHeader, layoutSelectionMode;
    private TextView tvSelectionCount, tvEmptyText;
    private EditText etSearchStudent;
    private ImageView btnCloseSelection, btnSelectAll;

    // Data Engine
    private TeacherViewModel viewModel;
    private TeacherScheduleAdapter adapter;
    private final List<EnrollmentModel> activeStudents = new ArrayList<>();
    private final List<EnrollmentModel> displayList = new ArrayList<>();

    // State
    private String selectedTuitionId = "ALL";
    private String selectedTuitionTitle = "All Students";
    private String currentSearchQuery = "";
    private boolean isSelectionMode = false;
    private final List<EnrollmentModel> selectedStudents = new ArrayList<>();

    // --- 🛠️ FIX: MOVED INTERFACE HERE TO FIX "Illegal static declaration" ---
    public interface OnStudentInteractListener {
        void onMessageClick(EnrollmentModel student);
        void onLongPress(EnrollmentModel student);
        void onTap(EnrollmentModel student);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_schedule, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearchAndListeners();

        // Bind to centralized ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(TeacherViewModel.class);
        observeData();

        return view;
    }

    private void initViews(View v) {
        rvSchedule = v.findViewById(R.id.rvTeacherSchedule);
        chipGroup = v.findViewById(R.id.chipGroupTuitions);
        fabBroadcast = v.findViewById(R.id.fabBroadcast);
        layoutEmptyState = v.findViewById(R.id.layoutEmptyState);
        tvEmptyText = v.findViewById(R.id.tvEmptyText);

        // Header views
        layoutStandardHeader = v.findViewById(R.id.layoutStandardHeader);
        layoutSelectionMode = v.findViewById(R.id.layoutSelectionMode);
        tvSelectionCount = v.findViewById(R.id.tvSelectionCount);
        btnCloseSelection = v.findViewById(R.id.btnCloseSelection);
        btnSelectAll = v.findViewById(R.id.btnSelectAll);
        etSearchStudent = v.findViewById(R.id.etSearchStudent);
    }

    private void setupSearchAndListeners() {
        etSearchStudent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnCloseSelection.setOnClickListener(v -> toggleSelectionMode(false));

        btnSelectAll.setOnClickListener(v -> {
            if (selectedStudents.size() == displayList.size()) {
                selectedStudents.clear(); // Deselect all
            } else {
                selectedStudents.clear();
                selectedStudents.addAll(displayList); // Select all
            }
            updateSelectionUI();
            adapter.notifyDataSetChanged();
        });

        fabBroadcast.setOnClickListener(v -> {
            if (isSelectionMode) {
                if (selectedStudents.isEmpty()) {
                    Toast.makeText(getContext(), "Select at least 1 student", Toast.LENGTH_SHORT).show();
                    return;
                }
                openBottomSheetMessage(selectedStudents, "Custom Selection");
            } else {
                if (displayList.isEmpty()) {
                    Toast.makeText(getContext(), "No students to message.", Toast.LENGTH_SHORT).show();
                    return;
                }
                openBottomSheetMessage(displayList, selectedTuitionTitle);
            }
        });
    }

    private void setupRecyclerView() {
        rvSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TeacherScheduleAdapter(displayList, new OnStudentInteractListener() {
            @Override
            public void onMessageClick(EnrollmentModel student) {
                List<EnrollmentModel> target = new ArrayList<>(); target.add(student);
                openBottomSheetMessage(target, student.getStudentName());
            }

            @Override
            public void onLongPress(EnrollmentModel student) {
                if (!isSelectionMode) toggleSelectionMode(true);
                toggleStudentSelection(student);
            }

            @Override
            public void onTap(EnrollmentModel student) {
                if (isSelectionMode) toggleStudentSelection(student);
            }
        });
        rvSchedule.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.getEnrollments().observe(getViewLifecycleOwner(), enrollments -> {
            activeStudents.clear();
            for (EnrollmentModel e : enrollments) {
                if ("approved".equals(e.getStatus())) {
                    activeStudents.add(e);
                }
            }
            buildFilterChips();
            applyFilters();
        });
    }

    private void buildFilterChips() {
        chipGroup.removeAllViews();
        if (!activeStudents.isEmpty()) addChip("ALL", "All Students");

        List<TuitionModel> classes = viewModel.getTuitions().getValue();
        if (classes != null) {
            for (TuitionModel t : classes) {

                // 🛠️ THE FIX: Skip corrupted Firestore documents that are missing an ID
                if (t.getTuitionId() == null) continue;

                // Ensure chip only shows if class has approved students
                boolean hasStudents = false;
                for (EnrollmentModel e : activeStudents) {
                    if (t.getTuitionId().equals(e.getTuitionId())) {
                        hasStudents = true;
                        break;
                    }
                }

                // Fallback for missing titles
                String title = t.getTitle() != null ? t.getTitle() : "Unnamed Class";

                if (hasStudents) {
                    addChip(t.getTuitionId(), title);
                }
            }
        }
    }

    private void addChip(String id, String label) {
        Chip chip = new Chip(requireContext());
        chip.setText(label);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setChipBackgroundColorResource(android.R.color.white);
        chip.setTextColor(Color.parseColor("#2E2345"));

        chip.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                selectedTuitionId = id;
                selectedTuitionTitle = label;
                applyFilters();
            }
        });
        if (id.equals(selectedTuitionId)) chip.setChecked(true);
        chipGroup.addView(chip);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void applyFilters() {
        displayList.clear();
        for (EnrollmentModel m : activeStudents) {
            boolean matchesClass = "ALL".equals(selectedTuitionId) || selectedTuitionId.equals(m.getTuitionId());
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    (m.getStudentName() != null && m.getStudentName().toLowerCase().contains(currentSearchQuery));
            if (matchesClass && matchesSearch) displayList.add(m);
        }

        // Smart FAB Update
        if (!isSelectionMode) {
            fabBroadcast.setText("ALL".equals(selectedTuitionId) ? "Broadcast to All" : "Message Class");
            fabBroadcast.setIconResource("ALL".equals(selectedTuitionId) ? android.R.drawable.ic_menu_send : android.R.drawable.ic_menu_sort_by_size);
        }

        adapter.notifyDataSetChanged();

        if (displayList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            tvEmptyText.setText(currentSearchQuery.isEmpty() ? "No students in this class" : "No results for '" + currentSearchQuery + "'");
            rvSchedule.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvSchedule.setVisibility(View.VISIBLE);
        }
    }

    // --- MULTI-SELECTION LOGIC ---
    private void toggleSelectionMode(boolean active) {
        isSelectionMode = active;
        selectedStudents.clear();
        adapter.notifyDataSetChanged(); // UI update applied natively through adapter bindings

        layoutStandardHeader.setVisibility(active ? View.GONE : View.VISIBLE);
        layoutSelectionMode.setVisibility(active ? View.VISIBLE : View.GONE);

        if (active) {
            fabBroadcast.setIconResource(android.R.drawable.ic_menu_send);
        } else {
            applyFilters(); // Reset FAB text
        }
        updateSelectionUI();
    }

    private void toggleStudentSelection(EnrollmentModel student) {
        if (selectedStudents.contains(student)) selectedStudents.remove(student);
        else selectedStudents.add(student);

        if (selectedStudents.isEmpty() && isSelectionMode) toggleSelectionMode(false);
        else updateSelectionUI();

        adapter.notifyDataSetChanged();
    }

    private void updateSelectionUI() {
        tvSelectionCount.setText(selectedStudents.size() + " Selected");
        fabBroadcast.setText("Message (" + selectedStudents.size() + ")");
    }

    // --- LEGENDARY BOTTOM SHEET MESSAGING ---
    @SuppressLint("SetTextI18n")
    private void openBottomSheetMessage(List<EnrollmentModel> targets, String titleOverride) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_bottom_message, null);
        dialog.setContentView(sheetView);

        // Ensure transparent background applies correctly to show our beautiful rounded XML background
        if (sheetView.getParent() != null) {
            ((View) sheetView.getParent()).setBackgroundColor(Color.TRANSPARENT);
        }

        TextView tvTitle = sheetView.findViewById(R.id.tvSheetTitle);
        TextView tvSub = sheetView.findViewById(R.id.tvSheetSubtitle);
        EditText etMsg = sheetView.findViewById(R.id.etMessageBody);
        MaterialButton btnSend = sheetView.findViewById(R.id.btnSendMessage);

        // Quick Reply Chips
        sheetView.findViewById(R.id.chipQuick1).setOnClickListener(v -> etMsg.setText(((Chip)v).getText()));
        sheetView.findViewById(R.id.chipQuick2).setOnClickListener(v -> etMsg.setText(((Chip)v).getText()));
        sheetView.findViewById(R.id.chipQuick3).setOnClickListener(v -> etMsg.setText(((Chip)v).getText()));

        tvTitle.setText(targets.size() == 1 ? "Message " + titleOverride : "Broadcast");
        tvSub.setText(targets.size() == 1 ? "Direct Message" : "Sending to " + targets.size() + " students in " + titleOverride);

        btnSend.setOnClickListener(v -> {
            String txt = etMsg.getText().toString().trim();
            if (txt.isEmpty()) { etMsg.setError("Cannot be empty"); return; }

            String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String senderName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

            // Loop through selected targets and dispatch messages (Simplified for demo)
            for (EnrollmentModel target : targets) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("text", txt);
                msg.put("senderId", senderId);
                msg.put("senderName", senderName != null ? senderName : "Teacher");
                msg.put("studentId", target.getStudentId());
                msg.put("tuitionId", target.getTuitionId());
                msg.put("timestamp", new Date());
                msg.put("type", targets.size() == 1 ? "PRIVATE" : "BROADCAST");

                FirebaseFirestore.getInstance().collection("messages").add(msg);
            }

            Toast.makeText(getContext(), "Message Sent Successfully!", Toast.LENGTH_SHORT).show();
            if (isSelectionMode) toggleSelectionMode(false);
            dialog.dismiss();
        });

        dialog.show();
    }

    // --- ADAPTER ---
    public class TeacherScheduleAdapter extends RecyclerView.Adapter<TeacherScheduleAdapter.ViewHolder> {
        private final List<EnrollmentModel> items;
        private final OnStudentInteractListener listener;

        public TeacherScheduleAdapter(List<EnrollmentModel> items, OnStudentInteractListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EnrollmentModel item = items.get(position);

            TuitionModel classData = viewModel.getTuitionById(item.getTuitionId());

            holder.tvSubjectName.setText(item.getStudentName() != null ? item.getStudentName() : "Unknown Student");
            holder.tvTopic.setText(classData != null ? classData.getTitle() : "Enrolled Class");

            holder.tvStatus.setText("ACTIVE");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.tvTimeStart.setText("Student");
            holder.tvDuration.setVisibility(View.GONE);
            holder.tvTutorName.setText(classData != null && classData.getTime() != null ? classData.getTime() : "Timings TBD");
            holder.tvLocation.setVisibility(View.GONE);

            if (item.getStudentPhoto() != null && !item.getStudentPhoto().isEmpty()) {
                Glide.with(holder.itemView.getContext()).load(item.getStudentPhoto()).circleCrop().into(holder.imgProfile);
            } else {
                holder.imgProfile.setImageResource(R.mipmap.ic_launcher);
            }

            // Visual feedback for selection
            boolean isSelected = selectedStudents.contains(item);

            // 🛠️ FIX: References the Fragment's top level `isSelectionMode` variable instead of an unresolvable inner one
            holder.itemView.setAlpha(isSelectionMode && !isSelected ? 0.6f : 1.0f);
            holder.itemView.setBackgroundColor(isSelected ? Color.parseColor("#33FFCA28") : Color.TRANSPARENT);

            holder.btnAction.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
            holder.btnAction.setText("Message");
            holder.btnAction.setIconResource(android.R.drawable.ic_menu_send);
            holder.btnAction.setOnClickListener(v -> listener.onMessageClick(item));

            holder.itemView.setOnLongClickListener(v -> { listener.onLongPress(item); return true; });
            holder.itemView.setOnClickListener(v -> listener.onTap(item));
        }

        @Override public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSubjectName, tvTopic, tvStatus, tvTimeStart, tvDuration, tvTutorName, tvLocation;
            ImageView imgProfile; MaterialButton btnAction;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvSubjectName = v.findViewById(R.id.tvSubjectName);
                tvTopic       = v.findViewById(R.id.tvTopic);
                tvStatus      = v.findViewById(R.id.tvStatus);
                tvTimeStart   = v.findViewById(R.id.tvTimeStart);
                tvDuration    = v.findViewById(R.id.tvDuration);
                tvTutorName   = v.findViewById(R.id.tvTutorName);
                tvLocation    = v.findViewById(R.id.tvLocation);
                imgProfile    = v.findViewById(R.id.imgTutor);
                btnAction     = v.findViewById(R.id.btnAction);
            }
        }
    }
}