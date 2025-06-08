package com.example.medicalapp;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class PendingAppointmentsAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> appointments;
    private ArrayList<String> appointmentIds;
    private DatabaseReference appointmentsRef;

    public PendingAppointmentsAdapter(@NonNull Context context, ArrayList<String> appointments, ArrayList<String> appointmentIds) {
        super(context, 0, appointments);
        this.context = context;
        this.appointments = appointments;
        this.appointmentIds = appointmentIds;
        this.appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pending_appointment, parent, false);
        }

        TextView tvDetails = convertView.findViewById(R.id.tvAppointmentDetails);
        Button btnAccept = convertView.findViewById(R.id.btnAccept);
        Button btnReject = convertView.findViewById(R.id.btnReject);

        String appointmentText = appointments.get(position);
        String appointmentId = appointmentIds.get(position);

        tvDetails.setText(appointmentText);

        // Handle the "No pending appointments." case
        if ("NO_ID".equals(appointmentId)) {
            // Hide Accept/Reject buttons when no appointments
            btnAccept.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
        } else {
            // Show buttons and set click listeners
            btnAccept.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.VISIBLE);

            btnAccept.setOnClickListener(v -> updateAppointmentStatus(appointmentId, "Accepted", position));
            btnReject.setOnClickListener(v -> updateAppointmentStatus(appointmentId, "Rejected", position));
        }

        return convertView;
    }

    private void updateAppointmentStatus(String appointmentId, String status, int position) {
        appointmentsRef.child(appointmentId).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Appointment " + status.toLowerCase() + ".", Toast.LENGTH_SHORT).show();

                    // Remove item from list after update
                    appointments.remove(position);
                    appointmentIds.remove(position);

                    if (appointments.isEmpty()) {
                        appointments.add("No pending appointments.");
                        appointmentIds.add("NO_ID");
                    }

                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
