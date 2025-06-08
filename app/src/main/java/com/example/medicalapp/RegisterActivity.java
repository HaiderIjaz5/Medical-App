package com.example.medicalapp;

import android.content.Intent;
import android.os.Bundle;
import android.content.res.ColorStateList;

import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.CompoundButtonCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText email, password, name;
    RadioGroup roleGroup;
    Spinner spinnerSpecialty;
    Button registerBtn;
    ProgressBar registerProgressBar;
    TextView tvLoginRedirect;
    RadioButton radioPatient, radioDoctor;

    FirebaseAuth auth;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        name = findViewById(R.id.registerName);
        roleGroup = findViewById(R.id.roleGroup);
        spinnerSpecialty = findViewById(R.id.spinnerSpecialty);
        registerBtn = findViewById(R.id.registerBtn);
        registerProgressBar = findViewById(R.id.registerProgressBar);
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect);

        radioPatient = findViewById(R.id.radioPatient);
        radioDoctor = findViewById(R.id.radioDoctor);

        // Firebase init
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        // Apply tint to radio buttons
        ColorStateList tintList = ContextCompat.getColorStateList(this, R.color.radio_button_tint);
        if (tintList != null) {
            CompoundButtonCompat.setButtonTintList(radioPatient, tintList);
            CompoundButtonCompat.setButtonTintList(radioDoctor, tintList);
        }

        // Spinner setup for Doctor Specialties (using custom spinner_item.xml)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.doctor_specialties,
                R.layout.spinner_item // custom layout for selected item
        );
        adapter.setDropDownViewResource(R.layout.spinner_item); // same layout for dropdown
        spinnerSpecialty.setAdapter(adapter);
        spinnerSpecialty.setVisibility(View.GONE);

        // Toggle spinner visibility based on role selection
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRoleBtn = findViewById(checkedId);
            if (selectedRoleBtn != null) {
                String role = selectedRoleBtn.getText().toString().toLowerCase();
                spinnerSpecialty.setVisibility(role.equals("doctor") ? View.VISIBLE : View.GONE);
            }
        });

        // Register button click
        registerBtn.setOnClickListener(v -> registerUser());

        // Redirect to login
        tvLoginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String emailVal = email.getText().toString().trim();
        String passwordVal = password.getText().toString().trim();
        String nameVal = name.getText().toString().trim();

        int selectedId = roleGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = ((RadioButton) findViewById(selectedId)).getText().toString().toLowerCase();
        String specialty = role.equals("doctor") ? spinnerSpecialty.getSelectedItem().toString() : null;

        // Input validation
        if (TextUtils.isEmpty(emailVal) || TextUtils.isEmpty(passwordVal) || TextUtils.isEmpty(nameVal)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passwordVal.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (role.equals("doctor") && (specialty == null || specialty.trim().isEmpty())) {
            Toast.makeText(this, "Please select a specialty", Toast.LENGTH_SHORT).show();
            return;
        }

        registerBtn.setEnabled(false);
        registerProgressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(emailVal, passwordVal)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", nameVal);
                        userMap.put("email", emailVal);
                        userMap.put("role", role);
                        if (specialty != null) {
                            userMap.put("specialty", specialty);
                        }

                        dbRef.child(uid).setValue(userMap)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    registerBtn.setEnabled(true);
                                    registerProgressBar.setVisibility(View.GONE);
                                });
                    } else {
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        registerBtn.setEnabled(true);
                        registerProgressBar.setVisibility(View.GONE);
                    }
                });
    }
}
