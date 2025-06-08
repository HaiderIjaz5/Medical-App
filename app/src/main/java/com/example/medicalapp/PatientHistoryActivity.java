package com.example.medicalapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PatientHistoryActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> treatmentList;
    private PatientHistoryAdapter adapter;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private DatabaseReference appointmentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_history);

        listView = findViewById(R.id.patientHistoryListView);
        progressBar = findViewById(R.id.progressBar);

        treatmentList = new ArrayList<>();
        adapter = new PatientHistoryAdapter(this, treatmentList);
        listView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        String doctorId = auth.getCurrentUser().getUid();

        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        loadPatientHistory(doctorId);
    }

    private void loadPatientHistory(String doctorId) {
        progressBar.setVisibility(View.VISIBLE);

        appointmentsRef.orderByChild("doctorId").equalTo(doctorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        treatmentList.clear();
                        List<Appointment> completedAppointments = new ArrayList<>();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String status = snap.child("status").getValue(String.class);

                            if ("Completed".equalsIgnoreCase(status)) {
                                String patientName = snap.child("patientName").getValue(String.class);
                                String date = snap.child("date").getValue(String.class);  // e.g., "08/06/2025"
                                String time = snap.child("time").getValue(String.class);  // e.g., "14:30"
                                String disease = snap.child("disease").getValue(String.class);
                                String prescription = snap.child("prescription").getValue(String.class);
                                String progress = snap.child("progress").getValue(String.class);

                                completedAppointments.add(new Appointment(patientName, date, time, disease, prescription, progress));
                            }
                        }

                        // Sort by date and time (latest first)
                        Collections.sort(completedAppointments, new Comparator<Appointment>() {
                            @Override
                            public int compare(Appointment a1, Appointment a2) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                    Date d1 = sdf.parse(a1.date + " " + a1.time);
                                    Date d2 = sdf.parse(a2.date + " " + a2.time);
                                    return d2.compareTo(d1); // latest first
                                } catch (ParseException e) {
                                    return 0;
                                }
                            }
                        });

                        for (Appointment a : completedAppointments) {
                            String item = "👤 Patient: " + a.patientName +
                                    "\n📅 Date: " + a.date +
                                    "\n🕒 Time: " + (a.time != null ? a.time : "N/A") +
                                    "\n🦠 Disease: " + (a.disease != null ? a.disease : "N/A") +
                                    "\n💊 Prescription: " + (a.prescription != null ? a.prescription : "N/A") +
                                    "\n📈 Progress: " + (a.progress != null ? a.progress : "N/A");

                            treatmentList.add(item);
                        }

                        if (treatmentList.isEmpty()) {
                            treatmentList.add("No treated patient history found.");
                        }

                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PatientHistoryActivity.this, "Failed to load patient history.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    // Helper class for sorting
    private static class Appointment {
        String patientName, date, time, disease, prescription, progress;

        Appointment(String patientName, String date, String time, String disease, String prescription, String progress) {
            this.patientName = patientName;
            this.date = date;
            this.time = time;
            this.disease = disease;
            this.prescription = prescription;
            this.progress = progress;
        }
    }
}
