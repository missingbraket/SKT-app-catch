package com.example.catch_v2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText phoneInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ 자동 로그인 체크
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedPhone = prefs.getString("phone", null);
        String savedRole = prefs.getString("role", null);

        if (savedPhone != null && savedRole != null) {
            moveToNextScreen(savedRole);
            return;
        }

        setContentView(R.layout.activity_login);
        phoneInput = findViewById(R.id.input_phone);
        ImageButton nextButton = findViewById(R.id.btn_next);

        // 전화번호 입력 시 버튼 활성화
        phoneInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nextButton.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        nextButton.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();
            Log.d("DEBUG", "전화번호 입력: " + phone);

            if (!phone.isEmpty()) {
                savePhoneLocally(phone); // 서버로 보내지 않고 로컬에 저장
                moveToSelectRoleActivity(phone);
            } else {
                Toast.makeText(this, "전화번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 📌 전화번호를 로컬에만 저장
    private void savePhoneLocally(String phone) {
        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .putString("phone", phone)
                .apply();
    }

    private void moveToSelectRoleActivity(String phone) {
        Intent intent = new Intent(this, SelectRoleActivity.class);
        intent.putExtra("phone", phone);
        startActivity(intent);
        finish();
    }

    private void moveToNextScreen(String role) {
        Intent intent = role.equals("parent") ? new Intent(this, ParentsActivity.class) : new Intent(this, ChildActivity.class);
        startActivity(intent);
        finish();
    }
}
