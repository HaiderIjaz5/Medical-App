package com.example.medicalapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HistoryUpdateActivity extends AppCompatActivity {

    private EditText etPrescription, etDisease, etProgress;
    private Button btnSave;
    private ProgressDialog progressDialog;

    private DatabaseReference appointmentRef;
    private String appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_update);

        etPrescription = findViewById(R.id.etPrescription);
        etDisease = findViewById(R.id.etDisease);
        etProgress = findViewById(R.id.etProgress);
        btnSave = findViewById(R.id.btnSaveHistory);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving history...");
        progressDialog.setCancelable(false);

        // Get appointment ID passed from intent
        appointmentId = getIntent().getStringExtra("appointmentId");
        if (appointmentId == null) {
            Toast.makeText(this, "No appointment ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Reference to the appointment in Firebase
        appointmentRef = FirebaseDatabase.getInstance().getReference("Appointments").child(appointmentId);

        // Load any existing data
        loadExistingHistory();

        // Save button click handler
        btnSave.setOnClickListener(v -> saveHistory());
    }

    private void loadExistingHistory() {
        appointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String prescription = snapshot.child("prescription").getValue(String.class);
                String disease = snapshot.child("disease").getValue(String.class);
                String progress = snapshot.child("progress").getValue(String.class);

                if (prescription != null) etPrescription.setText(prescription);
                if (disease != null) etDisease.setText(disease);
                if (progress != null) etProgress.setText(progress);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistoryUpdateActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveHistory() {
        String prescription = etPrescription.getText().toString().trim();
        String disease = etDisease.getText().toString().trim();
        String progress = etProgress.getText().toString().trim();

        if (TextUtils.isEmpty(prescription)) {
            etPrescription.setError("Enter prescription or 'N/A'");
            etPrescription.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(disease)) {
            etDisease.setError("Enter disease or 'N/A'");
            etDisease.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(progress)) {
            etProgress.setError("Enter progress or 'N/A'");
            etProgress.requestFocus();
            return;
        }

        progressDialog.show();

        // Update all fields including billGenerated = false
        appointmentRef.child("prescription").setValue(prescription);
        appointmentRef.child("disease").setValue(disease);
        appointmentRef.child("progress").setValue(progress);
        appointmentRef.child("status").setValue("Completed");
        appointmentRef.child("billGenerated").setValue(false)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(HistoryUpdateActivity.this, "Completed", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(HistoryUpdateActivity.this, "Failed to Complete", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
