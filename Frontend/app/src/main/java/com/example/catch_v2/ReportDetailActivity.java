package com.example.catch_v2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ReportDetailActivity extends AppCompatActivity {
    private static final String SERVER_URL = "http://greyk.iptime.org:8000/app/report_detail_parent";
    private String reportTitle;
    private String phoneNumber;
    private TextView reportContent;
    private ImageButton reportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        reportContent = findViewById(R.id.report_content);
        reportButton = findViewById(R.id.report_button);

        reportTitle = getIntent().getStringExtra("title");
        phoneNumber = getIntent().getStringExtra("phone");

        fetchReportDetail();

        reportButton.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:1366"))));
    }

    private void fetchReportDetail() {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse(SERVER_URL).newBuilder()
                .addQueryParameter("title", reportTitle)
                .addQueryParameter("phone", phoneNumber)
                .build();

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ReportDetailActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d("ReportDetailActivity", "서버 응답: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String content = jsonResponse.optString("report", "내용 없음");
                        String time = jsonResponse.optString("time", "시간 정보 없음");

                        String fullContent = "제목: " + reportTitle + "\n\n내용: " + content + "\n\n시간: " + time;

                        runOnUiThread(() -> reportContent.setText(fullContent));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> reportContent.setText("데이터 파싱 오류 발생"));
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(ReportDetailActivity.this, "서버 응답 실패", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
