package com.onrender.tutrnav;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final MutableLiveData<List<TuitionModel>> tuitionsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<EnrollmentModel>> enrollmentsLiveData = new MutableLiveData<>(new ArrayList<>());

    // Quick lookup maps
    private final Map<String, TuitionModel> tuitionMap = new HashMap<>();

    private ListenerRegistration tuitionsListener;
    private ListenerRegistration enrollmentsListener;

    public TeacherViewModel() {
        startListening();
    }

    public void startListening() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        // 1. Listen to Tuitions
        tuitionsListener = db.collection("tuitions").whereEqualTo("teacherId", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<TuitionModel> list = new ArrayList<>();
                    tuitionMap.clear();
                    for (DocumentSnapshot doc : value) {
                        TuitionModel t = doc.toObject(TuitionModel.class);
                        if (t != null) {
                            list.add(t);
                            tuitionMap.put(t.getTuitionId(), t);
                        }
                    }
                    tuitionsLiveData.setValue(list);
                });

        // 2. Listen to Enrollments
        enrollmentsListener = db.collection("enrollments").whereEqualTo("teacherId", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<EnrollmentModel> list = new ArrayList<>();
                    for (DocumentSnapshot doc : value) {
                        EnrollmentModel e = doc.toObject(EnrollmentModel.class);
                        if (e != null) list.add(e);
                    }
                    enrollmentsLiveData.setValue(list);
                });
    }

    public LiveData<List<TuitionModel>> getTuitions() { return tuitionsLiveData; }
    public LiveData<List<EnrollmentModel>> getEnrollments() { return enrollmentsLiveData; }
    public TuitionModel getTuitionById(String id) { return tuitionMap.get(id); }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (tuitionsListener != null) tuitionsListener.remove();
        if (enrollmentsListener != null) enrollmentsListener.remove();
    }
}