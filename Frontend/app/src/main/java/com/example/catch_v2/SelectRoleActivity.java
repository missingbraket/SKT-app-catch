package com.example.catch_v2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SelectRoleActivity extends AppCompatActivity {
    private String phone;
    private String selectedRole = null; // 📌 선택된 역할 저장
    private static final String SERVER_URL = "http://greyk.iptime.org:8000/login";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectrole);

        phone = getIntent().getStringExtra("phone");
        if (phone == null || phone.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            phone = prefs.getString("phone", "");
        }

        ImageButton parentButton = findViewById(R.id.img_parent);
        ImageButton childButton = findViewById(R.id.img_child);
        ImageButton btnSignIn = findViewById(R.id.btn_signin);

        parentButton.setOnClickListener(v -> setSelectedRole("parent", parentButton, childButton));

        childButton.setOnClickListener(v -> setSelectedRole("child", childButton, parentButton));

        // 등록 버튼 클릭 시 선택된 역할 서버로 전송
        btnSignIn.setOnClickListener(v -> {
            if (selectedRole != null) {
                sendRoleToServer(selectedRole);
            } else {
                Toast.makeText(this, "역할을 선택해주세요", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 선택 버튼 표시
    private void setSelectedRole(String role, ImageButton selectedButton, ImageButton otherButton) {
        selectedRole = role;

        if (role.equals("parent")) {
            selectedButton.setImageResource(R.drawable.parent_selected); // 보라색 부모 버튼 이미지
        } else {
            selectedButton.setImageResource(R.drawable.child_selected); // 보라색 자녀 버튼 이미지
        }

        // 다른 버튼은 원래 버튼으러
        if (role.equals("parent")) {
            otherButton.setImageResource(R.drawable.child_btn);
        } else {
            otherButton.setImageResource(R.drawable.parents_btn);
        }
    }

    private void sendRoleToServer(String role) {
        try {
            JSONObject json = new JSONObject();
            json.put("phone", phone);
            json.put("role", role);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(SERVER_URL).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(SelectRoleActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            saveUserData(phone, role);
                            moveToNextScreen(role);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(SelectRoleActivity.this, "서버 오류 발생", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveUserData(String phone, String role) {
        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .putString("phone", phone)
                .putString("role", role)
                .apply();
    }

    private void moveToNextScreen(String role) {
        Intent intent = role.equals("parent") ? new Intent(this, ParentsActivity.class) : new Intent(this, ChildActivity.class);
        startActivity(intent);
        finish();
    }
}
