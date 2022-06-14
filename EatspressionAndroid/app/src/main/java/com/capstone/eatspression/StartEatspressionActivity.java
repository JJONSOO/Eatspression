package com.capstone.eatspression;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import org.json.JSONArray;
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

public class StartEatspressionActivity extends AppCompatActivity {
    public ViewPager viewPager;
    public int currentPage = 0;
    public int dist = 1000;
    public int recommendNum = 1;
    public double latitude, longitude;
    public CameraSurfaceView surfaceView;
    private Timer timer;
    public HttpURLConnection conn;
    private String serverIp = "54.176.103.52";

    public ArrayList<String> urlList = new ArrayList<>();
    public ArrayList<Integer> idList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dist = getIntent().getExtras().getInt("dist");
        recommendNum = getIntent().getExtras().getInt("recommendNum");
        latitude = getIntent().getExtras().getDouble("longitude");
        longitude = getIntent().getExtras().getDouble("latitude");

        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
            final StringBuilder sb = new StringBuilder();
            final Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            String page = "http://" + serverIp + ":8080/restraunt/";

                            // URL 객체 생성
                            URL url = new URL(page);
                            // 연결 객체 생성
                            conn = (HttpURLConnection) url.openConnection();

                            // Post 파라미터
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("x", 126.6618189);
                            jsonObject.put("y", 37.4509063);
                            jsonObject.put("dist", dist);
                            jsonObject.put("recommendNum", recommendNum);



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
                                            sb.append(responseLine.trim().replace("\'", ""));
                                        }
                                    }

                                    Log.i("tag", "결과 문자열 :" + sb.toString());

                                    // 응답 Json 타입
                                    JSONObject jsonResponse = new JSONObject(sb.toString());
                                    for (int i = 1; i <= recommendNum; ++i) {
//                                        urlList.add(jsonResponse.getString(Integer.toString(i)));
                                        JSONArray jArray = jsonResponse.getJSONArray(Integer.toString(i));
                                        urlList.add((String)jArray.get(0));
                                    }

                                    try {
                                        idList.add(jsonResponse.getInt("user_id"));
                                    } catch(Exception e) {
                                        Log.i("tag", "id was not sent...");
                                    }
                                }
                            }
                            //
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.i("tag", "get error :" + e);
                            urlList.clear();
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
        setContentView(R.layout.activity_start_eatspression);


        surfaceView = findViewById(R.id.surfaceViewAuto);
        viewPager = findViewById(R.id.viewPagerAuto);

        synchronized(surfaceView.dataList) {
            surfaceView.dataList.add(0);
            surfaceView.dataList.add(idList.get(0));
            surfaceView.totalImageNum = urlList.size();
        }
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        viewPager.setClipToPadding(false);

        viewPager.setPadding(5, 0, 5, 0);
        viewPager.setPageMargin(5);

        viewPager.setAdapter(new ViewPagerAdapter(getApplicationContext(), urlList, null, false));
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            @Override
            public void run() {
                if(currentPage == urlList.size() + 3) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
                synchronized (surfaceView.dataList) {
                    surfaceView.dataList.set(0, currentPage - 1);
                }
            }
        };

        timer = new Timer();
        timer.schedule(new TimerTask() {
            int cnt = 0;
            @Override
            public void run() {
                if (cnt < urlList.size() + 3)
                    handler.post(Update);
                else if (cnt == urlList.size() + 3) {
                    // 페이지 종료
                    conn.disconnect();
                    surfaceView.surfaceDestroyed(surfaceView.getHolder());

                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName(getApplicationContext(),
                            EatspressionResultActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("userId", idList.get(0));
                    intent.putExtras(bundle);
                    intent.setComponent(componentName);

                    Log.i("tag", "startActivity~");
                    startActivity(intent);

                    finish();
                }
                cnt++;
            }
        }, 0, 2020);


        Button button2 = findViewById(R.id.stopButtonAuto);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                conn.disconnect();
                surfaceView.surfaceDestroyed(surfaceView.getHolder());
                timer.cancel();
                finish();
            }
        });
    }
}