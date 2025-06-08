package com.example.medicalapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class DoctorHomeActivity extends AppCompatActivity {

    private TextView textName, textEmail;
    private Button btnPendingAppointments, btnTodaysAppointments, btnPatientHistory,
            btnDoctorProfile, btnUpdateHistory, btnGenerateBill, btnLogout,btnUpdateTreatment;

    private FirebaseAuth auth;
    private DatabaseReference doctorsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_home);

        // Initialize views
        textName = findViewById(R.id.textName);
        textEmail = findViewById(R.id.textEmail);
        btnPendingAppointments = findViewById(R.id.btnPendingAppointments);
        btnTodaysAppointments = findViewById(R.id.btnTodaysAppointments);
        btnPatientHistory = findViewById(R.id.btnPatientHistory);
        btnDoctorProfile = findViewById(R.id.btnDoctorProfile);
        btnUpdateHistory = findViewById(R.id.btnUpdateHistory);
        btnGenerateBill = findViewById(R.id.btnGenerateBill);
        btnLogout = findViewById(R.id.btnLogout);
       

        auth = FirebaseAuth.getInstance();
        String doctorId = auth.getCurrentUser().getUid();

        doctorsRef = FirebaseDatabase.getInstance().getReference("Users").child(doctorId);

        loadDoctorProfile();

        // Button listeners - all active now
        btnPendingAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorHomeActivity.this, PendingAppointmentsActivity.class);
            startActivity(intent);
        });

        btnTodaysAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorHomeActivity.this, TodaysAppointmentsActivity.class);
            startActivity(intent);
        });

        btnPatientHistory.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorHomeActivity.this, PatientHistoryActivity.class);
            startActivity(intent);
        });

        btnDoctorProfile.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorHomeActivity.this, DoctorProfileActivity.class);
            startActivity(intent);
        });

        btnUpdateHistory.setOnClickListener(v -> showAppointmentSelectionDialog());

        btnGenerateBill.setOnClickListener(v -> {
            // Navigate to a new screen where doctor can select a completed appointment
            Intent intent = new Intent(DoctorHomeActivity.this, SelectAppointmentForBillingActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            finish();
            Intent intent = new Intent(DoctorHomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });



    }

    private void loadDoctorProfile() {
        doctorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                textName.setText("Name: " + (name != null ? name : "N/A"));
                textEmail.setText("Email: " + (email != null ? email : "N/A"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DoctorHomeActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAppointmentSelectionDialog() {
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");
        String doctorId = auth.getCurrentUser().getUid();

        appointmentsRef.orderByChild("doctorId").equalTo(doctorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> appointmentSummaries = new ArrayList<>();
                        ArrayList<String> appointmentIds = new ArrayList<>();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String status = snap.child("status").getValue(String.class);
                            if ("Completed".equalsIgnoreCase(status)) {
                                String patientName = snap.child("patientName").getValue(String.class);
                                String date = snap.child("date").getValue(String.class);
                                String time = snap.child("time").getValue(String.class);

                                String summary = patientName + " - " + date + " " + (time != null ? time : "");
                                appointmentSummaries.add(summary);
                                appointmentIds.add(snap.getKey());
                            }
                        }

                        if (appointmentSummaries.isEmpty()) {
                            Toast.makeText(DoctorHomeActivity.this, "No completed appointments found.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        CharSequence[] items = appointmentSummaries.toArray(new CharSequence[0]);

                        new AlertDialog.Builder(DoctorHomeActivity.this)
                                .setTitle("Select Appointment to Update")
                                .setItems(items, (dialog, which) -> {
                                    String selectedAppointmentId = appointmentIds.get(which);
                                    Intent intent = new Intent(DoctorHomeActivity.this, HistoryUpdateActivity.class);
                                    intent.putExtra("appointmentId", selectedAppointmentId);
                                    startActivity(intent);
                                })
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DoctorHomeActivity.this, "Failed to load appointments.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
