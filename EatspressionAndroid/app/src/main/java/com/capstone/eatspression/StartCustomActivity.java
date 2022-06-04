package com.capstone.eatspression;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class StartCustomActivity extends AppCompatActivity {
    ViewPager viewPager;
    int currentPage = 0;
    ArrayList<Integer> idList = new ArrayList<>();
    CameraSurfaceView surfaceView;
    Timer timer;
    public HttpURLConnection conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ArrayList<Uri> uriList = getIntent().getExtras().getParcelableArrayList("imgUris");

        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
            final StringBuilder sb = new StringBuilder();
            final Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            String page = "http://13.52.242.111:8080/restraunt/custom";

                            // URL 객체 생성
                            URL url = new URL(page);
                            // 연결 객체 생성
                            conn = (HttpURLConnection) url.openConnection();

                            // Post 파라미터
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("num", uriList.size());



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

                                    // 응답 Json 타입
                                    JSONObject jsonResponse = new JSONObject(sb.toString());

                                    idList.add(jsonResponse.getInt("user_id"));
                                } else {
                                    Log.i("tag", "오류가 있는듯..");
                                }
                            }
                            //
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.i("tag", "get error :" + e);
                            idList.clear();
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


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_custom);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.customFlag = true;
        synchronized(surfaceView.dataList) {
            surfaceView.dataList.add(idList.get(0));
            surfaceView.dataList.add(0);
        }


        viewPager = findViewById(R.id.viewPager);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        viewPager.setClipToPadding(false);

        viewPager.setPadding(50, 0, 50, 0);
        viewPager.setPageMargin(20);


        viewPager.setAdapter(new ViewPagerAdapter(getApplicationContext(), null, uriList, true));
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            @Override
            public void run() {
                if(currentPage == uriList.size()) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
                synchronized(surfaceView.dataList) {
                    surfaceView.dataList.set(0, currentPage - 1);
                }
            }
        };



        timer = new Timer();
        timer.schedule(new TimerTask() {
            int cnt = 0;
            @Override
            public void run() {
                if (cnt < uriList.size())
                    handler.post(Update);
                else if (cnt == uriList.size()) {
                    surfaceView.surfaceDestroyed(surfaceView.getHolder());
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName(getApplicationContext(),
                            CustomResultActivity.class);
                    intent.setComponent(componentName);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("imgUris", uriList);
                    bundle.putInt("userId", idList.get(0));
                    intent.putExtras(bundle);
                    startActivity(intent);

                    finish();
                }
                cnt++;
            }
        }, 100, 2020);
        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                surfaceView.surfaceDestroyed(surfaceView.getHolder());
                timer.cancel();

                Toast.makeText(getApplicationContext(), "중간에 중단하여 결과가 만들어지지 못했습니다.", Toast.LENGTH_LONG).show();

                finish();
            }
        });
    }
}