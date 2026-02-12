package com.onrender.tutrnav;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrent, etNew, etConfirm, etVerify;
    private ImageView toggleCurrent, toggleNew, toggleConfirm, btnBack;
    private MaterialButton btnUpdate, btnCancel;
    private boolean isCurrentVisible = false, isNewVisible = false, isConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        // Initialize Views
        etCurrent = findViewById(R.id.etCurrentPass);
        etNew = findViewById(R.id.etNewPass);
        etConfirm = findViewById(R.id.etConfirmPass);
        etVerify = findViewById(R.id.etVerifyCode);

        toggleCurrent = findViewById(R.id.toggleCurrent);
        toggleNew = findViewById(R.id.toggleNew);
        toggleConfirm = findViewById(R.id.toggleConfirm);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);

        // 1. Password Visibility Toggles
        toggleCurrent.setOnClickListener(v -> isCurrentVisible = togglePass(etCurrent, isCurrentVisible));
        toggleNew.setOnClickListener(v -> isNewVisible = togglePass(etNew, isNewVisible));
        toggleConfirm.setOnClickListener(v -> isConfirmVisible = togglePass(etConfirm, isConfirmVisible));

        // 2. Navigation
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        // 3. Update Logic
        btnUpdate.setOnClickListener(v -> validateAndChangePassword());
    }

    private boolean togglePass(EditText editText, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        editText.setSelection(editText.getText().length());
        return !isVisible;
    }

    private void validateAndChangePassword() {
        String currentPass = etCurrent.getText().toString();
        String newPass = etNew.getText().toString();
        String confirmPass = etConfirm.getText().toString();

        if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            etConfirm.setError("Passwords do not match");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // Firebase requires re-authentication for sensitive changes
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Update Failed: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    etCurrent.setError("Incorrect Current Password");
                }
            });
        }
    }
}