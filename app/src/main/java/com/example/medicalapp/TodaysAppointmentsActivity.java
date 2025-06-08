package com.example.medicalapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class TodaysAppointmentsActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressBar progressBar;

    private ArrayList<String> appointmentList;    // Display strings (patient name + date/time)
    private ArrayList<String> appointmentIds;     // Keys of appointments in Firebase
    private AppointmentsAdapter adapter;          // Custom adapter

    private FirebaseAuth auth;
    private DatabaseReference appointmentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todays_appointments);

        listView = findViewById(R.id.todaysAppointmentsListView);
        progressBar = findViewById(R.id.progressBar);

        appointmentList = new ArrayList<>();
        appointmentIds = new ArrayList<>();

        adapter = new AppointmentsAdapter(this, appointmentList, appointmentIds);
        listView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        // Show progress bar before loading
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        loadAcceptedAppointments(auth.getCurrentUser().getUid());
    }

    private void loadAcceptedAppointments(String doctorId) {
        appointmentsRef.orderByChild("doctorId").equalTo(doctorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        appointmentList.clear();
                        appointmentIds.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String status = snap.child("status").getValue(String.class);

                            if ("Accepted".equalsIgnoreCase(status)) {
                                String patientName = snap.child("patientName").getValue(String.class);
                                String date = snap.child("date").getValue(String.class);
                                String time = snap.child("time").getValue(String.class);

                                String item = patientName + "\nDate: " + date + " Time: " + time + "\nStatus: " + status;
                                appointmentList.add(item);
                                appointmentIds.add(snap.getKey());
                            }
                        }

                        if (appointmentList.isEmpty()) {
                            appointmentList.add("No accepted appointments found.");
                        }

                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        Toast.makeText(TodaysAppointmentsActivity.this, "Failed to load appointments.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh appointments every time this screen comes back into focus
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        loadAcceptedAppointments(auth.getCurrentUser().getUid());
    }
}
