package com.example.medicalapp;

import android.os.Bundle;
import android.widget.*;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class GenerateBillActivity extends AppCompatActivity {

    private TextView tvPatientInfo;
    private EditText etAmount, etNote;
    private Button btnGenerate;
    private DatabaseReference dbRef;
    private String appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_bill);

        tvPatientInfo = findViewById(R.id.tvPatientInfo);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        btnGenerate = findViewById(R.id.btnGenerateBill);

        appointmentId = getIntent().getStringExtra("appointmentId");
        dbRef = FirebaseDatabase.getInstance().getReference("Appointments").child(appointmentId);

        loadAppointmentDetails();

        btnGenerate.setOnClickListener(v -> {
            String amount = etAmount.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (amount.isEmpty()) {
                etAmount.setError("Enter amount");
                return;
            }

            Intent intent = new Intent(this, PreviewBillActivity.class);
            intent.putExtra("appointmentId", appointmentId);
            intent.putExtra("amount", amount);
            intent.putExtra("note", note);
            startActivity(intent);
        });

    }

    private void loadAppointmentDetails() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String patient = snapshot.child("patientName").getValue(String.class);
                String date = snapshot.child("date").getValue(String.class);
                String time = snapshot.child("time").getValue(String.class);

                tvPatientInfo.setText("Patient: " + patient + "\nDate: " + date + " Time: " + time);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GenerateBillActivity.this, "Failed to load appointment.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
