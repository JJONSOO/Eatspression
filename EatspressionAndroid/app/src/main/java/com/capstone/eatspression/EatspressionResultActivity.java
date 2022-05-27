package com.capstone.eatspression;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class EatspressionResultActivity extends AppCompatActivity {
    public ArrayList<String> responseList = new ArrayList<>();
    public HttpURLConnection conn;
    int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eatspression_result);

        Log.i("tag", "get result!!");
        userId = getIntent().getExtras().getInt("userId");


    }

    @Override
    public void onResume() {
        super.onResume();

        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
            final StringBuilder sb = new StringBuilder();
            final Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            String page = "http://54.241.56.66:8080/restraunt/finish";

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
                                conn.setConnectTimeout(20000);

                                // POST 요청방식
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("Content-Type", "application/json");
                                conn.setRequestProperty("Accept", "application/json");
                                conn.setDoOutput(true);
                                conn.setDoInput(true);
                                Log.i("tag", "get result!!");
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
                                    responseList.add(jsonResponse.getString("img"));
//                                    responseList.add(jsonResponse.getString("restaurantName"));
                                    responseList.add(jsonResponse.getString("address"));

                                } else {
                                    Log.i("tag", "오류가 있는듯...");
                                }
                            }
                            //
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

        ImageView favoriteImg = findViewById(R.id.favoriteImage);
        favoriteImg.setVisibility(View.VISIBLE);
        Glide.with(this).load(responseList.get(0)).into(favoriteImg);


        EditText restaurantName = findViewById(R.id.restaurantName);
//        restaurantName.setText(responseList.get(1));
        restaurantName.setText("샤브담 인하대점");

        Button siteButton = findViewById(R.id.siteButton);
        siteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(responseList.get(2)));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(responseList.get(1)));
                startActivity(intent);
            }
        });


//        Button roadButton = findViewById(R.id.roadButton);
//        roadButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(responseList.get(3)));
////                startActivity(intent);
//            }
//        });
    }
}