package com.example.medicalapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class TreatmentHistoryActivity extends AppCompatActivity {

    private ListView treatmentListView;
    private ArrayAdapter<String> treatmentAdapter;
    private ArrayList<String> treatmentList;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment_history);

        treatmentListView = findViewById(R.id.treatmentListView);
        progressBar = findViewById(R.id.progressBar);

        treatmentList = new ArrayList<>();
        // Use custom layout with blue text color for list items
        treatmentAdapter = new ArrayAdapter<>(this, R.layout.list_item_treatment, R.id.textViewTreatmentItem, treatmentList);
        treatmentListView.setAdapter(treatmentAdapter);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Appointments");

        fetchTreatmentHistory();
    }

    private void fetchTreatmentHistory() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String currentUserId = auth.getCurrentUser().getUid();

        databaseRef.orderByChild("patientId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        treatmentList.clear();

                        for (DataSnapshot appointmentSnap : snapshot.getChildren()) {
                            String status = appointmentSnap.child("status").getValue(String.class);
                            if ("Completed".equalsIgnoreCase(status)) {
                                String doctor = appointmentSnap.child("doctorName").getValue(String.class);
                                String date = appointmentSnap.child("date").getValue(String.class);
                                String disease = appointmentSnap.child("disease").getValue(String.class);
                                String prescription = appointmentSnap.child("prescription").getValue(String.class);
                                String progress = appointmentSnap.child("progress").getValue(String.class);

                                String item = "Dr. " + (doctor != null ? doctor : "N/A") +
                                        "\nDate: " + (date != null ? date : "N/A") +
                                        "\nDisease: " + (disease != null ? disease : "N/A") +
                                        "\nPrescription: " + (prescription != null ? prescription : "N/A") +
                                        "\nProgress: " + (progress != null ? progress : "N/A");

                                treatmentList.add(item);
                            }
                        }

                        if (treatmentList.isEmpty()) {
                            treatmentList.add("No treatment history found.");
                        }

                        treatmentAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(TreatmentHistoryActivity.this, "Failed to load treatment history.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
