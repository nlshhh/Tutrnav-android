package com.onrender.tutrnav;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeacherDashboardFragment extends Fragment {

    private TextView tvHeroTitle, tvHeroTime, tvTotalEarnings, tvActiveStudents;
    private LinearLayout emptyStateView;
    private RecyclerView rvRequests;

    private RequestAdapter adapter;
    private List<EnrollmentModel> pendingRequests = new ArrayList<>();
    private TeacherViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_dashboard, container, false);

        initViews(view);

        // Initialize Shared ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(TeacherViewModel.class);
        observeData();

        return view;
    }

    private void initViews(View v) {
        tvHeroTitle = v.findViewById(R.id.tvHeroTitle);
        tvHeroTime = v.findViewById(R.id.tvHeroTime);
        tvTotalEarnings = v.findViewById(R.id.tvTotalEarnings);
        tvActiveStudents = v.findViewById(R.id.tvActiveStudents);
        emptyStateView = v.findViewById(R.id.emptyStateView);
        rvRequests = v.findViewById(R.id.rvRequests);

        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRequests.setNestedScrollingEnabled(false);
        adapter = new RequestAdapter(pendingRequests);
        rvRequests.setAdapter(adapter);

        setupSwipeToAct();

        v.findViewById(R.id.btnStartClass).setOnClickListener(view ->
                Toast.makeText(getContext(), "Launching Live Broadcast...", Toast.LENGTH_SHORT).show()
        );
    }

    private void observeData() {
        viewModel.getTuitions().observe(getViewLifecycleOwner(), tuitions -> {
            updateHeroCard(tuitions);
            recalculateStats();
        });

        viewModel.getEnrollments().observe(getViewLifecycleOwner(), enrollments -> {
            pendingRequests.clear();
            for (EnrollmentModel e : enrollments) {
                if ("pending".equals(e.getStatus())) {
                    pendingRequests.add(e);
                }
            }
            adapter.notifyDataSetChanged();

            emptyStateView.setVisibility(pendingRequests.isEmpty() ? View.VISIBLE : View.GONE);
            rvRequests.setVisibility(pendingRequests.isEmpty() ? View.GONE : View.VISIBLE);

            recalculateStats();
        });
    }

    private void recalculateStats() {
        List<EnrollmentModel> enrollments = viewModel.getEnrollments().getValue();
        if (enrollments == null) return;

        int activeCount = 0;
        double totalEarnings = 0.0;

        for (EnrollmentModel e : enrollments) {
            if ("approved".equals(e.getStatus())) {
                activeCount++;
                TuitionModel t = viewModel.getTuitionById(e.getTuitionId());
                if (t != null && t.getFee() != null) {
                    try {
                        totalEarnings += Double.parseDouble(t.getFee().replaceAll("", ""));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        tvActiveStudents.setText(String.valueOf(activeCount));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        currencyFormat.setMaximumFractionDigits(0);
        tvTotalEarnings.setText(totalEarnings >= 100000 ? "₹" + (int)(totalEarnings / 1000) + "k" : currencyFormat.format(totalEarnings));
    }

    private void updateHeroCard(List<TuitionModel> tuitions) {
        if (tuitions == null || tuitions.isEmpty()) {
            tvHeroTitle.setText("No Active Classes");
            tvHeroTime.setText("Go to 'My Tuition' to create one");
            return;
        }
        // In a real app, parse `time` vs Current Time here.
        // For now, we beautifully display the first item.
        TuitionModel next = tuitions.get(0);
        tvHeroTitle.setText(next.getTitle());
        tvHeroTime.setText((next.getTime() != null ? next.getTime() : "TBD") + " • Hosted by you");
    }

    // --- LEGENDARY SWIPE-TO-ACT UX ---
    private void setupSwipeToAct() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                EnrollmentModel target = pendingRequests.get(position);
                String newStatus = (direction == ItemTouchHelper.RIGHT) ? "approved" : "rejected";

                // Optimistic UI Update
                pendingRequests.remove(position);
                adapter.notifyItemRemoved(position);
                emptyStateView.setVisibility(pendingRequests.isEmpty() ? View.VISIBLE : View.GONE);

                FirebaseFirestore.getInstance().collection("enrollments").document(target.getEnrollmentId())
                        .update("status", newStatus)
                        .addOnSuccessListener(aVoid -> {
                            Snackbar.make(requireView(), target.getStudentName() + " " + newStatus, Snackbar.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // Revert on failure
                            pendingRequests.add(position, target);
                            adapter.notifyItemInserted(position);
                            Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // Draws a colored background behind the swiped item
                View itemView = vh.itemView;
                int backgroundCornerOffset = 20;

                if (dX > 0) { // Swiping to the right (Approve)
                    c.clipRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
                    c.drawColor(Color.parseColor("#4CAF50")); // Green
                } else if (dX < 0) { // Swiping to the left (Reject)
                    c.clipRect(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    c.drawColor(Color.parseColor("#F44336")); // Red
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(rvRequests);
    }

    private class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {
        List<EnrollmentModel> list;
        public RequestAdapter(List<EnrollmentModel> list) { this.list = list; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_request, parent, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EnrollmentModel item = list.get(position);
            holder.tvName.setText(item.getStudentName() != null ? item.getStudentName() : "Unknown");

            TuitionModel classInfo = viewModel.getTuitionById(item.getTuitionId());
            holder.tvClass.setText("Wants to join: " + (classInfo != null ? classInfo.getTitle() : "Your Class"));

            if (item.getStudentPhoto() != null && !item.getStudentPhoto().isEmpty()) {
                Glide.with(holder.itemView.getContext()).load(item.getStudentPhoto()).circleCrop().into(holder.imgProfile);
            } else {
                holder.imgProfile.setImageResource(R.mipmap.ic_launcher);
            }

            // Hide buttons since we rely on Swipe now, or keep them as fallback
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.GONE);
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvClass; ImageView imgProfile;
            MaterialButton btnApprove, btnDecline;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvStudentName);
                tvClass = v.findViewById(R.id.tvRequestClass);
                imgProfile = v.findViewById(R.id.imgStudent);
                btnApprove = v.findViewById(R.id.btnApprove);
                btnDecline = v.findViewById(R.id.btnDecline);
            }
        }
    }
}