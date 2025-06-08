package com.example.medicalapp;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class PreviewBillActivity extends AppCompatActivity {

    private TextView tvPreview;
    private Button btnConfirm;
    private String appointmentId, amount, note;
    private DatabaseReference dbRef;
    private String patientName, date, time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_bill);

        tvPreview = findViewById(R.id.tvPreview);
        btnConfirm = findViewById(R.id.btnConfirmBill);

        appointmentId = getIntent().getStringExtra("appointmentId");
        amount = getIntent().getStringExtra("amount");
        note = getIntent().getStringExtra("note");

        dbRef = FirebaseDatabase.getInstance().getReference("Appointments").child(appointmentId);

        loadAppointmentDetails();
    }

    private void loadAppointmentDetails() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientName = snapshot.child("patientName").getValue(String.class);
                date = snapshot.child("date").getValue(String.class);
                time = snapshot.child("time").getValue(String.class);

                String bill = "Patient: " + patientName +
                        "\nDate: " + date +
                        "\nTime: " + time +
                        "\nAmount: Rs. " + amount +
                        "\nNote: " + note;

                tvPreview.setText(bill);

                btnConfirm.setOnClickListener(v -> {
                    saveBillToFirebase();
                    generatePdfUsingMediaStore(bill);

                    Intent intent = new Intent(PreviewBillActivity.this, SelectAppointmentForBillingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PreviewBillActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveBillToFirebase() {
        dbRef.child("bill").setValue("Rs. " + amount + "\nNote: " + note);
        dbRef.child("billGenerated").setValue(true);
        Toast.makeText(this, "Bill saved. PDF will be generated.", Toast.LENGTH_SHORT).show();
    }

    private void generatePdfUsingMediaStore(String content) {
        String fileName = "Bill_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

        // Step 1: Create PDF in memory
        PdfDocument pdfDoc = new PdfDocument();
        Paint paint = new Paint();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = pdfDoc.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        paint.setTextSize(12f);
        paint.setColor(Color.BLACK);

        int x = 10, y = 25;
        for (String line : content.split("\n")) {
            canvas.drawText(line, x, y, paint);
            y += 20;
        }

        pdfDoc.finishPage(page);

        // Step 2: Insert into MediaStore Downloads
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
        values.put(MediaStore.Downloads.IS_PENDING, 1); // For Android 10+

        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri fileUri = getContentResolver().insert(collection, values);

        if (fileUri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(fileUri)) {
                pdfDoc.writeTo(out);
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                getContentResolver().update(fileUri, values, null, null);
                Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to access Downloads folder", Toast.LENGTH_SHORT).show();
        }

        pdfDoc.close();
    }
}
