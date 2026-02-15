package com.onrender.tutrnav;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    // Cloudinary Config - REPLACE THIS WITH YOUR PRESET NAME
    // You must create an "Unsigned" preset in Cloudinary Settings -> Upload -> Upload Presets
    private static final String UPLOAD_PRESET = "tutornav_preset";

    private FirebaseAuth mAuth;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    // UI Components
    private ImageView btnBack, imgProfile, btnViewPass;
    private TextView tvProfileName, tvProfilePhone, tvProfilePass;
    private MaterialButton btnPolicies, btnPermissions, btnSupport, btnSignOut;
    private CardView btnDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        setupWindowInsets();

        mAuth = FirebaseAuth.getInstance();

        initViews();
        loadUserInfo();
        setupImagePicker();
        setupClickListeners();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgProfile = findViewById(R.id.imgProfile);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfilePass = findViewById(R.id.tvProfilePass);
        btnViewPass = findViewById(R.id.btnViewPass); // Ensure you add this ID to your XML

        btnPolicies = findViewById(R.id.btnPolicies);
        btnPermissions = findViewById(R.id.btnPermissions);
        btnSupport = findViewById(R.id.btnSupport);

        btnSignOut = findViewById(R.id.btnSignOut);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
    }

    private void setupClickListeners() {
        // 1. Back
        btnBack.setOnClickListener(v -> finish());

        // 2. Upload Image (Cloudinary)
        imgProfile.setOnClickListener(v -> {
            // Opens modern Android photo picker
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        // 3. Edit Name
        tvProfileName.setOnClickListener(v -> showEditNameDialog());

        // 4. View/Edit Password Logic
        btnViewPass.setOnClickListener(v -> handlePasswordClick());
        tvProfilePass.setOnClickListener(v -> handlePasswordClick());

        // 5. Grid Buttons
        btnPolicies.setOnClickListener(v -> showInfoDialog("Policies", "Our policies are simple: Your data is yours. We use Cloudinary for secure image storage."));
        btnPermissions.setOnClickListener(v -> openAppSettings());
        btnSupport.setOnClickListener(v -> contactSupport());

        // 6. Account Actions
        btnSignOut.setOnClickListener(v -> showLogoutDialog());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    // --- CLOUDINARY UPLOAD LOGIC ---
    private void setupImagePicker() {
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                uploadToCloudinary(uri);
            }
        });
    }

    private void uploadToCloudinary(Uri fileUri) {
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        // Standard Cloudinary Upload code from documentation
        MediaManager.get().upload(fileUri)
                .unsigned(UPLOAD_PRESET) // Important: Must be Unsigned preset
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Upload started");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Optional: Update a progress bar here
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // 1. Get the URL
                        String downloadUrl = (String) resultData.get("secure_url");
                        Log.d("Cloudinary", "Upload success: " + downloadUrl);

                        // 2. Update Firebase Auth Profile
                        updateFirebaseProfile(null, Uri.parse(downloadUrl));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "Error: " + error.getDescription());
                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Upload Failed: " + error.getDescription(), Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    // --- FIREBASE & UI UPDATES ---
    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Name
            String name = user.getDisplayName();
            tvProfileName.setText((name != null && !name.isEmpty()) ? name : "Set Name");

            // Email/Phone
            tvProfilePhone.setText(user.getEmail());

            // Password State (Google vs Email)
            boolean isGoogle = user.getProviderData().stream()
                    .anyMatch(p -> "google.com".equals(p.getProviderId()));

            if (isGoogle) {
                tvProfilePass.setText("Signed in via Google");
                btnViewPass.setAlpha(0.5f);
            } else {
                tvProfilePass.setText("**************");
            }

            // Load Image using Glide
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.mipmap.ic_launcher) // Default image
                        .circleCrop()
                        .into(imgProfile);
            }
        }
    }

    private void updateFirebaseProfile(String newName, Uri newPhotoUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();
        if (newName != null) request.setDisplayName(newName);
        if (newPhotoUri != null) request.setPhotoUri(newPhotoUri);

        user.updateProfile(request.build()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    if (newName != null) tvProfileName.setText(newName);
                    if (newPhotoUri != null) {
                        Glide.with(this).load(newPhotoUri).circleCrop().into(imgProfile);
                    }
                });
            }
        });
    }

    // --- HELPER DIALOGS ---

    private void showEditNameDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(tvProfileName.getText());
        input.setPadding(50, 40, 50, 40);

        new AlertDialog.Builder(this)
                .setTitle("Update Name")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String val = input.getText().toString().trim();
                    if (!val.isEmpty()) updateFirebaseProfile(val, null);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handlePasswordClick() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        boolean isGoogle = user.getProviderData().stream().anyMatch(p -> "google.com".equals(p.getProviderId()));
        if (isGoogle) {
            Toast.makeText(this, "Google account passwords are managed by Google.", Toast.LENGTH_LONG).show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Reset Password")
                    .setMessage("Send password reset email to " + user.getEmail() + "?")
                    .setPositiveButton("Send", (d, w) -> {
                        mAuth.sendPasswordResetEmail(user.getEmail());
                        Toast.makeText(this, "Email Sent", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void contactSupport() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:support@tutornav.com"));
        try { startActivity(intent); } catch (Exception e) { Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show(); }
    }

    private void showInfoDialog(String title, String msg) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton("OK", null).show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (d, w) -> {
                    mAuth.signOut();
                    navigateToOnboarding();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().delete().addOnSuccessListener(v -> navigateToOnboarding());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToOnboarding() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}