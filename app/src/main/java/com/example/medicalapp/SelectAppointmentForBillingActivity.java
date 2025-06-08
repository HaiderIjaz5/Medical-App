package com.example.medicalapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class SelectAppointmentForBillingActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressBar progressBar;
    private ArrayList<String> appointmentList = new ArrayList<>();
    private ArrayList<String> appointmentIds = new ArrayList<>();
    private BillItemAdapter adapter;
    private DatabaseReference appointmentRef;
    private String doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_appointment_for_billing);

        listView = findViewById(R.id.selectBillingListView);
        progressBar = findViewById(R.id.progressBar);

        doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        appointmentRef = FirebaseDatabase.getInstance().getReference("Appointments");

        adapter = new BillItemAdapter(this, appointmentList);
        listView.setAdapter(adapter);

        loadCompletedAppointments();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < appointmentIds.size()) {
                String selectedId = appointmentIds.get(position);
                Intent intent = new Intent(SelectAppointmentForBillingActivity.this, GenerateBillActivity.class);
                intent.putExtra("appointmentId", selectedId);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCompletedAppointments();
    }

    private void loadCompletedAppointments() {
        progressBar.setVisibility(View.VISIBLE);

        appointmentRef.orderByChild("doctorId").equalTo(doctorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        appointmentList.clear();
                        appointmentIds.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String status = snap.child("status").getValue(String.class);
                            Boolean billGenerated = snap.child("billGenerated").getValue(Boolean.class);

                            if ("Completed".equalsIgnoreCase(status) && (billGenerated == null || !billGenerated)) {
                                String patientName = snap.child("patientName").getValue(String.class);
                                String date = snap.child("date").getValue(String.class);
                                String time = snap.child("time").getValue(String.class);

                                appointmentList.add(patientName + "\nDate: " + date + " Time: " + time);
                                appointmentIds.add(snap.getKey());
                            }
                        }

                        if (appointmentList.isEmpty()) {
                            appointmentList.add("No completed appointments pending billing.");
                        }

                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SelectAppointmentForBillingActivity.this, "Failed to load appointments.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    // Inner class for custom adapter
    private class BillItemAdapter extends ArrayAdapter<String> {

        private Context context;
        private ArrayList<String> items;

        public BillItemAdapter(Context context, ArrayList<String> items) {
            super(context, 0, items);
            this.context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(context).inflate(R.layout.list_item_bill, parent, false);
            }

            String currentItem = items.get(position);
            TextView tvBillItem = listItem.findViewById(R.id.textViewBillItem);
            tvBillItem.setText(currentItem);

            return listItem;
        }
    }
}
