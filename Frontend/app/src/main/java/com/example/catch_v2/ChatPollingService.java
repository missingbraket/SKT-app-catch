package com.example.catch_v2;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class ChatPollingService extends Service {
    private final Handler handler = new Handler();
    private final int INTERVAL = 5000; // 5초마다 요청

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ChatPollingService", "🚀 ChatPollingService 시작됨!"); // ✅ 서비스 실행 확인 로그
        startPolling(); // 📌 지속적인 요청 시작
    }

    private void startPolling() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("ChatPollingService", "🔍 requestChatFromFlutter() 호출!");
                requestChatFromFlutter(); // 📌 Flutter 채팅 앱에 데이터 요청
                handler.postDelayed(this, INTERVAL); // 반복 실행
            }
        }, INTERVAL);
    }

    private void requestChatFromFlutter() {
        Intent intent = new Intent();
        intent.setAction("com.example.chat_app.REQUEST_CHAT"); // ✅ 패키지명 정확히 확인할 것!
        intent.setPackage("com.example.chat_app"); // 📌 Flutter 앱 패키지명

        intent.putExtra("request", "get_latest_chat");

        Log.d("ChatPollingService", "📡 Flutter 채팅 앱에 채팅 데이터 요청 전송");
        sendBroadcast(intent); // 📌 Flutter 앱에 브로드캐스트 전송
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
