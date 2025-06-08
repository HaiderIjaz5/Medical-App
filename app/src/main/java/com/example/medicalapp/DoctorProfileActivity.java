package com.example.medicalapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class DoctorProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    private TextView tvWelcomeDoctor;
    private TextView tvDoctorEmail, tvDoctorSpecialty;
    private Button btnPatientHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

        tvWelcomeDoctor = findViewById(R.id.tvWelcomeDoctor);
        tvDoctorEmail = findViewById(R.id.tvDoctorEmail);
        tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty);
        btnPatientHistory = findViewById(R.id.btnPatientHistory);

        auth = FirebaseAuth.getInstance();
        String doctorId = auth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        loadDoctorInfo(doctorId);

        btnPatientHistory.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorProfileActivity.this, PatientHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void loadDoctorInfo(String doctorId) {
        usersRef.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String doctorName = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String specialty = snapshot.child("specialty").getValue(String.class);

                if (doctorName != null && !doctorName.isEmpty()) {
                    tvWelcomeDoctor.setText("Welcome Dr. " + doctorName);
                } else {
                    tvWelcomeDoctor.setText("Welcome Dr.");
                }

                if (email != null) {
                    tvDoctorEmail.setText("Email: " + email);
                }

                if (specialty != null) {
                    tvDoctorSpecialty.setText("Specialty: " + specialty);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvWelcomeDoctor.setText("Welcome Dr.");
                Toast.makeText(DoctorProfileActivity.this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
