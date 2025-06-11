package com.example.catch_v2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ChatReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.example.chat_app.SEND_CHAT")) {
            String contents = intent.getStringExtra("contents");
            String imageUrl = intent.getStringExtra("imageUrl");

            Log.d("ChatReceiver", "📥 Flutter에서 받은 채팅 데이터: " + contents);
            Log.d("ChatReceiver", "📥 Flutter에서 받은 이미지 URL: " + imageUrl);

            // 📌 받은 데이터를 백엔드로 전송
            ChatService chatService = new ChatService();
            chatService.sendChatToServer(contents, imageUrl, "자녀 전화번호");
        }
    }
}
