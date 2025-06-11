package com.example.catch_v2;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 스플래시 화면을 전체로 채우기 위해 테마 적용
        setContentView(R.layout.activity_splash);

        // 2초 후 LoginActivity로 이동
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // 스플래시 액티비티 종료 (뒤로 가기 시 다시 안 보이게)
        }, 2000); // 2초 (2000ms)
    }
}
