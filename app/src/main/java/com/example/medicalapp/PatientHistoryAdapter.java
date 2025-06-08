package com.example.medicalapp;

import android.content.Context;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;

public class PatientHistoryAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> history;

    public PatientHistoryAdapter(Context context, ArrayList<String> history) {
        super(context, 0, history);
        this.context = context;
        this.history = history;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_patient_history, parent, false);
        }

        TextView tvHistory = convertView.findViewById(R.id.tvPatientHistory);
        tvHistory.setText(history.get(position));

        return convertView;
    }
}
