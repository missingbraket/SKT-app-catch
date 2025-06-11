package com.example.catch_v2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.*;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class ConnectActivity extends AppCompatActivity {
    private EditText etChildPhone;
    private static final String SERVER_URL = "http://greyk.iptime.org:8000/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        etChildPhone = findViewById(R.id.connect_phone);
        ImageView btnOk = findViewById(R.id.btn_add_child);

        btnOk.setOnClickListener(v -> {
            String childPhone = etChildPhone.getText().toString().trim();
            if (!childPhone.isEmpty()) {
                try {
                    sendChildPhoneToServer(childPhone);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "전화번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendChildPhoneToServer(String phone) throws JSONException {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();

        // 부모 정보 가져옴
        String parentPhone = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("phone", null);

        json.put("phone", parentPhone); // 부모 번호 (필수)
        json.put("role", "parent"); // 부모 역할 추가
        json.put("children_phone", phone); // 자녀 번호

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(SERVER_URL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ConnectActivity.this, "연결 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ConnectActivity.this, "자녀가 추가되었습니다!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ConnectActivity.this, "서버 오류 발생", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
