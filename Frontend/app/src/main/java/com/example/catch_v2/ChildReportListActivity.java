package com.example.catch_v2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ChildReportListActivity extends AppCompatActivity {
    private static final String SERVER_URL = "http://greyk.iptime.org:8000/app/report_children";
    private ListView listView;
    private ArrayList<String> reportTitles;
    private ArrayList<String> reportTimes;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_report_list);

        listView = findViewById(R.id.report_child_list);
        reportTitles = new ArrayList<>();
        reportTimes = new ArrayList<>();

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

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedReportTime = reportTimes.get(position);
            String selectedTitle = reportTitles.get(position);

            Intent intent = new Intent(ChildReportListActivity.this, ChildReportDetailActivity.class);
            intent.putExtra("time", selectedReportTime);
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
                runOnUiThread(() -> Toast.makeText(ChildReportListActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
                Log.e("ChildReportListActivity", "서버 연결 실패: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> Toast.makeText(ChildReportListActivity.this, "서버 응답 오류", Toast.LENGTH_SHORT).show());
                    return;
                }

                String responseBody = response.body().string();

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONArray reportArray = jsonResponse.optJSONArray("report");

                    reportTitles.clear();
                    reportTimes.clear();

                    for (int i = 0; i < reportArray.length(); i++) {
                        JSONObject report = reportArray.getJSONObject(i);
                        String title = report.optString("title", "제목 없음");
                        String time = report.optString("time", "시간 없음");

                        reportTitles.add(title);
                        reportTimes.add(time);
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ChildReportListActivity.this, android.R.layout.simple_list_item_1, reportTitles);
                        listView.setAdapter(adapter);
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
