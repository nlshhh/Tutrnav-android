package com.onrender.tutrnav;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;

    // UI Elements
    private EditText etName, etEmail, etPassword;
    private Button btnSignUp;
    private ImageView btnBack;
    private MaterialButton btnGoToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Edge-to-Edge Setup
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Note: Ensure your root ConstraintLayout in XML has android:id="@+id/main"
        // If it doesn't, this part might crash. You can remove the listener if needed.
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // 2. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // 3. Connect Views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnBack = findViewById(R.id.btnBack);
        btnGoToSignIn = findViewById(R.id.btnGoToSignIn);

        // 4. Setup Listeners
        setupListeners();
    }

    private void setupListeners() {
        // Back Button -> Close Activity
        btnBack.setOnClickListener(v -> finish());

        // "Sign In Instead" Button -> Go to Login Page
        btnGoToSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish(); // Close Sign Up so user doesn't come back here on back press
        });

        // Sign Up Logic
        btnSignUp.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validations
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        Toast.makeText(this, "Creating Account...", Toast.LENGTH_SHORT).show();

        // Create User in Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Account Created! Now let's save the Name.
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUserProfile(user, name);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Helper to save the Full Name to Firebase so we can say "Hi [Name]" later
    private void updateUserProfile(FirebaseUser user, String name) {
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Name saved successfully, go to Home
                            navigateToHome();
                        }
                    });
        }
    }

    private void navigateToHome() {
        Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SignUpActivity.this, StudentHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}