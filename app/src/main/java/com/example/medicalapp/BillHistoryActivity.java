package com.example.medicalapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class BillHistoryActivity extends AppCompatActivity {

    private ListView billListView;
    private ArrayAdapter<String> billAdapter;
    private ArrayList<String> billList;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    private static final String TAG = "BillHistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_history);

        billListView = findViewById(R.id.billListView);
        progressBar = findViewById(R.id.progressBar);

        billList = new ArrayList<>();

        // Use the new custom layout and specify TextView id
        billAdapter = new ArrayAdapter<>(this, R.layout.list_item_bill_custom, R.id.textViewBillItem, billList);

        billListView.setAdapter(billAdapter);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Appointments");

        fetchBillHistory();
    }

    private void fetchBillHistory() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String currentUserId = auth.getCurrentUser().getUid();

        databaseRef.orderByChild("patientId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        billList.clear();

                        Log.d(TAG, "DataSnapshot count: " + snapshot.getChildrenCount());

                        for (DataSnapshot appointmentSnap : snapshot.getChildren()) {
                            String status = appointmentSnap.child("status").getValue(String.class);
                            if ("Completed".equalsIgnoreCase(status)) {
                                String doctor = appointmentSnap.child("doctorName").getValue(String.class);
                                String date = appointmentSnap.child("date").getValue(String.class);
                                String bill = appointmentSnap.child("bill").getValue(String.class);

                                String item = "Dr. " + (doctor != null ? doctor : "N/A") +
                                        "\nDate: " + (date != null ? date : "N/A") +
                                        "\nBill: Rs " + (bill != null ? bill : "N/A");

                                billList.add(item);
                            }
                        }

                        if (billList.isEmpty()) {
                            billList.add("No bill history found.");
                        }

                        billAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(BillHistoryActivity.this, "Failed to load bills.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Database error: " + error.getMessage());
                    }
                });
    }
}
