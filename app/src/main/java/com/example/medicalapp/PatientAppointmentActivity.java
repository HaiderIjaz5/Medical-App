package com.example.medicalapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.Calendar;
import java.util.HashMap;

public class PatientAppointmentActivity extends AppCompatActivity {

    private EditText editSymptoms;
    private Spinner spinnerDoctor;
    private TextView textDate;
    private Button btnSelectDate, btnSubmit;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    private String selectedDoctor = "", selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_appointment);

        editSymptoms = findViewById(R.id.editSymptoms);
        spinnerDoctor = findViewById(R.id.spinnerDoctor);
        textDate = findViewById(R.id.textDate);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSubmit = findViewById(R.id.btnSubmit);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Dummy doctor list for Spinner
        String[] doctorList = {"Dr. Ahmed", "Dr. Sara", "Dr. Usman"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, doctorList);
        spinnerDoctor.setAdapter(adapter);

        spinnerDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedDoctor = doctorList[pos];
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSelectDate.setOnClickListener(v -> showDatePicker());

        btnSubmit.setOnClickListener(v -> submitAppointment());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate = day + "/" + (month + 1) + "/" + year;
            textDate.setText("Selected Date: " + selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void submitAppointment() {
        String symptoms = editSymptoms.getText().toString().trim();
        if (symptoms.isEmpty() || selectedDate.isEmpty() || selectedDoctor.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String appointmentId = databaseReference.child("Appointments").push().getKey();

        HashMap<String, Object> appointmentData = new HashMap<>();
        appointmentData.put("patientId", uid);
        appointmentData.put("doctorName", selectedDoctor);
        appointmentData.put("date", selectedDate);
        appointmentData.put("symptoms", symptoms);
        appointmentData.put("status", "Pending");

        assert appointmentId != null;
        databaseReference.child("Appointments").child(appointmentId).setValue(appointmentData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Appointment booked!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
