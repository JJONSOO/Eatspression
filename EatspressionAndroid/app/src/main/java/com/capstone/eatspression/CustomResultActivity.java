package com.capstone.eatspression;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CustomResultActivity extends AppCompatActivity {
    public HttpURLConnection conn;
    public ArrayList<String> responseList = new ArrayList<>();          // 결과 데이터가 String이 저장됨. 각 이미지마다 쏴줄 것임
    ArrayList<Uri> uriList;
    int userId;
    EditText resultText;
    Handler handler;
    Thread th;
    @Override
    protected void onCreate(Bundle savedInstanceState) {Log.i("tag", "get result!!");
        uriList = getIntent().getExtras().getParcelableArrayList("imgUris");
        userId = getIntent().getExtras().getInt("userId");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_result);

        handler = new Handler();
        th = new Thread(new Runnable() {
            @Override
            public void run() {

                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
                    final StringBuilder sb = new StringBuilder();
                    final Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    String page = "http://18.144.29.108:8080/restraunt/custom/finish";

                                    // URL 객체 생성
                                    URL url = new URL(page);
                                    // 연결 객체 생성
                                    conn = (HttpURLConnection) url.openConnection();

                                    // Post 파라미터
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("user_id", userId);

                                    // 연결되면
                                    if (conn != null) {
                                        Log.i("tag", "conn 연결");
                                        // 응답 타임아웃 설정
                                        conn.setConnectTimeout(10000);

                                        // POST 요청방식
                                        conn.setRequestMethod("POST");
                                        conn.setRequestProperty("Content-Type", "application/json");
                                        conn.setRequestProperty("Accept", "application/json");
                                        conn.setDoOutput(true);
                                        conn.setDoInput(true);

                                        // 포스트 파라미터 전달
                                        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                                        wr.write(jsonObject.toString());
                                        wr.flush();

                                        // url에 접속 성공하면 (200)
                                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                            // 결과 값 읽어오는 부분
                                            try (BufferedReader br = new BufferedReader(
                                                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                                                String responseLine = null;
                                                while ((responseLine = br.readLine()) != null) {
                                                    sb.append(responseLine.trim());
                                                }
                                            }

                                            Log.i("tag", "결과 문자열 :" + sb.toString());

                                            // 응답 Json 타입 이부분은 이미지별 결과를 받아오는 걸로 수정이 필요함
                                            JSONObject jsonResponse = new JSONObject(sb.toString());
                                            responseList.add(jsonResponse.getString("idx"));
                                        }
                                    }
                                    break;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.i("tag", "get error :" + e);
                                }
                            }
                        }
                    });
                    th.start();
                    try {
                        th.join();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                if (responseList.size() != 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ProgressBar proBar = findViewById(R.id.progressBarCustom);
                            ViewPager viewPager = findViewById(R.id.viewPagerCustomResult);
                            proBar.setVisibility(View.INVISIBLE);
                            viewPager.setVisibility(View.VISIBLE);
                            viewPager.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    return true;
                                }
                            });
                            viewPager.setClipToPadding(false);


                            viewPager.setAdapter(new ViewPagerAdapter(getApplicationContext(), null, uriList, true));
                            viewPager.setCurrentItem(Integer.parseInt(responseList.get(0)));


                            resultText = findViewById(R.id.resultText);
                            resultText.setText("가장 선호도가 높았던 사진입니다.");
                        }
                    });
                } else {
                    resultText = findViewById(R.id.resultText);
                    resultText.setText("서버에서 데이터를 불러오는 중 오류가 발생했습니다...");
                }
                Button toMain = findViewById(R.id.toMainCustom);
                toMain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        ComponentName componentName = new ComponentName(getApplicationContext(),
                                MainActivity.class);
                        intent.setComponent(componentName);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
        th.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            th.join();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}