package com.example.medicalapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class BookAppointmentActivity extends AppCompatActivity {

    Spinner spinnerDoctors;
    EditText editDate, editTime;
    Button btnBook;

    FirebaseAuth auth;
    DatabaseReference userRef, appointmentRef;

    HashMap<String, String> doctorMap = new HashMap<>();
    ArrayAdapter<String> adapter;
    List<String> doctorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        spinnerDoctors = findViewById(R.id.spinnerDoctors);
        editDate = findViewById(R.id.editDate);
        editTime = findViewById(R.id.editTime);
        btnBook = findViewById(R.id.btnBookAppointment);

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        appointmentRef = FirebaseDatabase.getInstance().getReference("Appointments");

        loadDoctors();

        editDate.setOnClickListener(v -> showDatePicker());
        editTime.setOnClickListener(v -> showTimePicker());

        btnBook.setOnClickListener(v -> bookAppointment());
    }

    private void loadDoctors() {
        userRef.orderByChild("role").equalTo("doctor")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        doctorList.clear();
                        doctorMap.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String doctorName = ds.child("name").getValue(String.class);
                            String specialty = ds.child("specialty").getValue(String.class); // Ensure field name matches DB
                            String doctorId = ds.getKey();

                            if (doctorName != null && doctorId != null) {
                                String displayName = doctorName;
                                if (specialty != null && !specialty.isEmpty()) {
                                    displayName += " (" + specialty + ")";
                                }

                                doctorMap.put(displayName, doctorId);
                                doctorList.add(displayName);
                            }
                        }

                        if (doctorList.isEmpty()) {
                            Toast.makeText(BookAppointmentActivity.this, "No doctors available.", Toast.LENGTH_SHORT).show();
                        }

                        // Use your custom spinner item layout here
                        adapter = new ArrayAdapter<>(BookAppointmentActivity.this, R.layout.spinner_item, doctorList);
                        adapter.setDropDownViewResource(R.layout.spinner_item);
                        spinnerDoctors.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BookAppointmentActivity.this, "Failed to load doctors", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dateString = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    editDate.setText(dateString);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String amPm = hourOfDay >= 12 ? "PM" : "AM";
                    int hour = hourOfDay % 12;
                    if (hour == 0) hour = 12;
                    String timeString = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm);
                    editTime.setText(timeString);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);

        timePickerDialog.show();
    }

    private void bookAppointment() {
        String selectedDoctorDisplay = spinnerDoctors.getSelectedItem() != null ? spinnerDoctors.getSelectedItem().toString() : null;
        String doctorId = selectedDoctorDisplay != null ? doctorMap.get(selectedDoctorDisplay) : null;

        String date = editDate.getText().toString().trim();
        String time = editTime.getText().toString().trim();
        String patientId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (doctorId == null) {
            Toast.makeText(this, "Please select a doctor.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDateTimeValid(date, time)) {
            Toast.makeText(this, "Please select a valid date and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (patientId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String patientName = snapshot.child("name").getValue(String.class);
                if (patientName == null) patientName = "N/A";

                String appointmentId = appointmentRef.push().getKey();
                HashMap<String, Object> appointment = new HashMap<>();
                appointment.put("doctorId", doctorId);

                // Extract doctor name from display string (before " (")
                String doctorName = selectedDoctorDisplay.split(" \\(")[0];
                appointment.put("doctorName", doctorName);

                appointment.put("patientId", patientId);
                appointment.put("patientName", patientName);
                appointment.put("date", date);
                appointment.put("time", time);
                appointment.put("status", "Pending");

                if (appointmentId != null) {
                    appointmentRef.child(appointmentId).setValue(appointment)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(BookAppointmentActivity.this, "Appointment Booked!", Toast.LENGTH_SHORT).show();
                                clearFields();

                                // Navigate to PatientHomeActivity after booking
                                Intent intent = new Intent(BookAppointmentActivity.this, PatientHomeActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(BookAppointmentActivity.this, "Booking Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(BookAppointmentActivity.this, "Failed to generate appointment ID", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookAppointmentActivity.this, "Error loading patient info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isDateTimeValid(String date, String time) {
        if (date.isEmpty() || time.isEmpty()) return false;

        try {
            String[] dateParts = date.split("/");
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1;
            int year = Integer.parseInt(dateParts[2]);

            String[] timeParts = time.split("[: ]");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            String amPm = timeParts[2];

            if (amPm.equalsIgnoreCase("PM") && hour != 12) hour += 12;
            if (amPm.equalsIgnoreCase("AM") && hour == 12) hour = 0;

            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day, hour, minute, 0);

            Calendar now = Calendar.getInstance();

            return selected.after(now);
        } catch (Exception e) {
            return false;
        }
    }

    private void clearFields() {
        editDate.setText("");
        editTime.setText("");
        if (!doctorList.isEmpty()) spinnerDoctors.setSelection(0);
    }
}
