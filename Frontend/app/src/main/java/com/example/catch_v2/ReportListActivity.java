package com.example.catch_v2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ReportListActivity extends AppCompatActivity {
    private static final String SERVER_URL = "http://greyk.iptime.org:8000/app/report_parent";
    private ListView listView;
    private ArrayList<String> reportTitles;
    private ArrayList<String> reportIds;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);

        listView = findViewById(R.id.report_list);
        reportTitles = new ArrayList<>();
        reportIds = new ArrayList<>();

        phoneNumber = getIntent().getStringExtra("phone");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            phoneNumber = prefs.getString("phone", "");
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "전화번호 없음", Toast.LENGTH_SHORT).show();
            return;
        }

        fetchReportList();

        // 📌 리스트 항목 클릭 시 상세 페이지로 이동 (전화번호 + 제목 전달)
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedReportId = reportIds.get(position);
            String selectedTitle = reportTitles.get(position);

            Intent intent = new Intent(ReportListActivity.this, ReportDetailActivity.class);
            intent.putExtra("reportId", selectedReportId);
            intent.putExtra("phone", phoneNumber);
            intent.putExtra("title", selectedTitle);
            startActivity(intent);
        });
    }

    private void fetchReportList() {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse(SERVER_URL).newBuilder()
                .addQueryParameter("phone", phoneNumber)
                .build();

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ReportListActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> Toast.makeText(ReportListActivity.this, "서버 응답 오류", Toast.LENGTH_SHORT).show());
                    return;
                }

                String responseBody = response.body().string();

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (jsonResponse.has("error")) {
                        String errorMessage = jsonResponse.getString("error");
                        runOnUiThread(() -> Toast.makeText(ReportListActivity.this, "오류: " + errorMessage, Toast.LENGTH_SHORT).show());
                        return;
                    }

                    JSONArray reportArray = jsonResponse.getJSONArray("report");

                    reportTitles.clear();
                    reportIds.clear();

                    for (int i = 0; i < reportArray.length(); i++) {
                        JSONObject report = reportArray.getJSONObject(i);
                        String summary = report.optString("title", "제목 없음");
                        String reportId = report.optString("time", "시간 없음");

                        reportTitles.add(summary);
                        reportIds.add(reportId);
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ReportListActivity.this, android.R.layout.simple_list_item_1, reportTitles);
                        listView.setAdapter(adapter);
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(ReportListActivity.this, "JSON 파싱 오류", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
