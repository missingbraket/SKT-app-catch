package com.example.catch_v2;

import android.util.Log;
import okhttp3.*;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class ChatService {
    private static final String SERVER_URL = "http://10.0.2.2:8000";//은비언니
    private final OkHttpClient client = new OkHttpClient();

    public void sendChatToServer(String contents, String imageUrl, String childPhone) {
        JSONObject json = new JSONObject();
        try {
            json.put("contents", contents);
            json.put("imageUrl", imageUrl);
            json.put("childPhone", childPhone);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(SERVER_URL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ChatService", "❌ 백엔드 전송 실패: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("ChatService", "✅ 백엔드로 채팅 전송 성공");
            }
        });
    }
}
