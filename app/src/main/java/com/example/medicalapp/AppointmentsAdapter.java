package com.example.medicalapp;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;

public class AppointmentsAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> appointments;
    private ArrayList<String> appointmentIds;

    public AppointmentsAdapter(Context context, ArrayList<String> appointments, ArrayList<String> appointmentIds) {
        super(context, 0, appointments);
        this.context = context;
        this.appointments = appointments;
        this.appointmentIds = appointmentIds;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        }

        TextView tvDetails = convertView.findViewById(R.id.tvAppointmentDetails);
        Button btnUpdateHistory = convertView.findViewById(R.id.btnUpdateHistory);

        tvDetails.setText(appointments.get(position));

        btnUpdateHistory.setOnClickListener(v -> {
            String appointmentId = appointmentIds.get(position);
            Intent intent = new Intent(context, HistoryUpdateActivity.class);
            intent.putExtra("appointmentId", appointmentId);
            context.startActivity(intent);
        });

        return convertView;
    }
}
