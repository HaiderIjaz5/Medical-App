package com.example.medicalapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    EditText emailField, passwordField;
    Button loginBtn;
    TextView registerRedirect;
    ImageView passwordToggle;
    ProgressBar loginProgress;
    FirebaseAuth auth;
    DatabaseReference dbRef;
    boolean passwordVisible = false;

    @Override
    protected void onStart() {
        super.onStart();
        // 🔁 Auto-login if user is already signed in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            checkUserRoleAndRedirect();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginBtn = findViewById(R.id.loginBtn);
        registerRedirect = findViewById(R.id.registerRedirect);
        passwordToggle = findViewById(R.id.passwordToggle);
        loginProgress = findViewById(R.id.loginProgress);

        loginBtn.setOnClickListener(v -> loginUser());

        registerRedirect.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        if (passwordVisible) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordToggle.setImageResource(android.R.drawable.ic_menu_view); // Eye icon
        } else {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordToggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel); // Close-eye icon
        }
        passwordField.setSelection(passwordField.getText().length());
        passwordVisible = !passwordVisible;
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // 🔐 Input Validation
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show spinner & disable login button
        loginProgress.setVisibility(android.view.View.VISIBLE);
        loginBtn.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Hide spinner & enable login button (in failure case)
                    loginProgress.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful()) {
                        checkUserRoleAndRedirect();
                    } else {
                        Toast.makeText(this, "Login failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        loginBtn.setEnabled(true);
                    }
                });
    }

    private void checkUserRoleAndRedirect() {
        String userId = auth.getCurrentUser().getUid();
        dbRef.child(userId).child("role").get().addOnSuccessListener(dataSnapshot -> {
            String role = dataSnapshot.getValue(String.class);
            if ("patient".equals(role)) {
                startActivity(new Intent(this, PatientHomeActivity.class));
            } else if ("doctor".equals(role)) {
                startActivity(new Intent(this, DoctorHomeActivity.class));
            } else {
                Toast.makeText(this, "User role not found.", Toast.LENGTH_SHORT).show();
                loginBtn.setEnabled(true);
            }
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to retrieve role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            loginBtn.setEnabled(true);
        });
    }
}
