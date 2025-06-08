package com.example.medicalapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class PendingAppointmentsActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressBar progressBar;
    private ArrayList<String> appointmentList;
    private ArrayList<String> appointmentIds;
    private PendingAppointmentsAdapter adapter;

    private FirebaseAuth auth;
    private DatabaseReference appointmentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_appointments);

        listView = findViewById(R.id.pendingAppointmentsListView);
        progressBar = findViewById(R.id.progressBar);

        appointmentList = new ArrayList<>();
        appointmentIds = new ArrayList<>();
        adapter = new PendingAppointmentsAdapter(this, appointmentList, appointmentIds);
        listView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        String doctorId = auth.getCurrentUser().getUid();
        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        loadPendingAppointments(doctorId);
    }

    private void loadPendingAppointments(String doctorId) {
        // Show spinner and hide list while loading
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        appointmentsRef.orderByChild("doctorId").equalTo(doctorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        appointmentList.clear();
                        appointmentIds.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String status = snap.child("status").getValue(String.class);
                            if ("Pending".equalsIgnoreCase(status)) {
                                String patientName = snap.child("patientName").getValue(String.class);
                                String date = snap.child("date").getValue(String.class);
                                String time = snap.child("time").getValue(String.class);

                                String item = patientName + "\nDate: " + date + " Time: " + time;
                                appointmentList.add(item);
                                appointmentIds.add(snap.getKey());
                            }
                        }

                        if (appointmentList.isEmpty()) {
                            // Add a placeholder for no pending appointments
                            appointmentList.add("No pending appointments.");
                            appointmentIds.add("NO_ID"); // Dummy ID to keep lists in sync
                        }

                        adapter.notifyDataSetChanged();

                        // Hide spinner and show list after loading
                        progressBar.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        Toast.makeText(PendingAppointmentsActivity.this, "Failed to load appointments.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
