package com.example.catch_v2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import okhttp3.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PollingService extends Service {
    private static final String SERVER_URL = "http://greyk.iptime.org:8000/app/post_alert";
    private static final String CHANNEL_ID = "danger_alert_channel";
    private static final int FOREGROUND_NOTIFICATION_ID = 2001;

    private final OkHttpClient client = new OkHttpClient();
    private final Handler handler = new Handler();
    private final int INTERVAL = 1000; // 1초마다 서버 체크

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundService();
        startPolling();
    }

    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Catch 서비스 실행 중")
                .setContentText("온라인 그루밍을 감지합니다.")
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    /**
     * ✅ 서버 폴링 시작
     */
    private void startPolling() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkDangerAlert();
                handler.postDelayed(this, INTERVAL);
            }
        }, INTERVAL);
    }

    private void checkDangerAlert() {
        String phone = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("phone", "");
        String role = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("role", "");

        Log.d("PollingService", "📱 현재 전화번호(phone): " + phone);
        Log.d("PollingService", "🔍 현재 역할(role): " + role);

        if (phone.isEmpty()) {
            Log.e("PollingService", "❌ 전화번호가 설정되지 않았습니다.");
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("phone", phone);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(SERVER_URL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("PollingService", "❌ 서버 연결 실패: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("PollingService", "📡 서버 응답: " + responseBody);

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    boolean danger = jsonResponse.optBoolean("alert", false);
                    String message = jsonResponse.optString("message", "위험한 대화 감지!");

                    if (danger) {
                        Log.d("PollingService", "🚨 위험 대화 감지! 역할에 맞는 알림만 전송.");
                        // ✅ 현재 역할에 맞는 알림만 전송
                        sendDangerNotification(message, role);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * ✅ 역할에 맞는 알림만 전송
     */
    private void sendDangerNotification(String message, String role) {
        Log.d("PollingService", "🔔 알림 생성 시작, 역할(role): ");

        Class<?> targetActivity = role.equals("parent") ? ParentsActivity.class : ChildActivity.class;

        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int requestCode = role.equals("parent") ? 1 : 2;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        int notificationId = role.equals("parent") ? 1001 : 1002;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("🚨온라인 그루밍 의심 대화 감지!🚨")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            Log.d("PollingService", "🔔 " + role + " 역할에 맞는 알림 전송 시도");
            manager.notify(notificationId, builder.build());
        } else {
            Log.e("PollingService", "❌ 알림 매니저가 null임, 알림 전송 실패");
        }
    }

    /**
     * ✅ 알림 채널 생성
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "위험 대화 감지 알림",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("위험 대화 감지 시 경고 알림을 표시합니다.");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
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
