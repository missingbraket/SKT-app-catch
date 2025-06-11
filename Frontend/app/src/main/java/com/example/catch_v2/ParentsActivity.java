package com.example.catch_v2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

public class ParentsActivity extends AppCompatActivity {
    private static final int REQUEST_CALL_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parents);

        startService(new Intent(this, PollingService.class));

        ImageView btnAddChild = findViewById(R.id.btn_add_child);
        btnAddChild.setOnClickListener(v -> startActivity(new Intent(this, ConnectActivity.class)));

        ImageView btnReportCall = findViewById(R.id.btn_reportcall);
        btnReportCall.setOnClickListener(v -> makeEmergencyCall());

        ImageView reportButton = findViewById(R.id.emergencereport);
        reportButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String phoneNumber = prefs.getString("phone", "");

            Intent intent = new Intent(ParentsActivity.this, ReportListActivity.class);
            intent.putExtra("phone", phoneNumber);
            startActivity(intent);
        });
    }

    private void makeEmergencyCall() {
        String phoneNumber = "tel:1366";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(phoneNumber)));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeEmergencyCall();
            } else {
                Toast.makeText(this, "전화 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
