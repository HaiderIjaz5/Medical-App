package com.example.medicalapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class PatientHomeActivity extends AppCompatActivity {

    private static final String TAG = "PatientHomeActivity";

    private TextView tvWelcome, nameTextView, emailTextView;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        nameTextView = findViewById(R.id.textName);
        emailTextView = findViewById(R.id.textEmail);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        String userId = auth.getCurrentUser().getUid();

        Button btnBillHistory = findViewById(R.id.btnBillHistory);
        Button btnTreatmentHistory = findViewById(R.id.btnTreatmentHistory);
        Button btnBookAppointment = findViewById(R.id.btnBookAppointment);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Disable buttons until data loads
        btnBillHistory.setEnabled(false);
        btnTreatmentHistory.setEnabled(false);
        btnBookAppointment.setEnabled(false);

        progressBar.setVisibility(View.VISIBLE);

        // Load user profile data
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    tvWelcome.setText("Welcome, " + (name != null ? name : "Patient") + "!");
                    nameTextView.setText("Name: " + (name != null ? name : "N/A"));
                    emailTextView.setText("Email: " + (email != null ? email : "N/A"));

                    btnBillHistory.setEnabled(true);
                    btnTreatmentHistory.setEnabled(true);
                    btnBookAppointment.setEnabled(true);

                } else {
                    Toast.makeText(PatientHomeActivity.this, "Unable to load your profile. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failed to load profile: ", error.toException());
                Toast.makeText(PatientHomeActivity.this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        });

        btnBillHistory.setOnClickListener(v -> startActivity(new Intent(PatientHomeActivity.this, BillHistoryActivity.class)));

        btnTreatmentHistory.setOnClickListener(v -> startActivity(new Intent(PatientHomeActivity.this, TreatmentHistoryActivity.class)));

        btnBookAppointment.setOnClickListener(v -> startActivity(new Intent(PatientHomeActivity.this, BookAppointmentActivity.class)));

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(PatientHomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Optional: confirm on back pressed to exit/logout or simply finish app
    @Override
    public void onBackPressed() {
        // You can add confirmation dialog here if you want
        super.onBackPressed();
    }
}
