package com.capstone.eatspression;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class StartCustomActivity extends AppCompatActivity {
    ViewPager viewPager;
    int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_custom);

        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "중간에 중단하여 결과가 만들어지지 못했습니다.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        viewPager = findViewById(R.id.viewPager);

        viewPager.setClipToPadding(false);

        viewPager.setPadding(100, 0, 100, 0);
        viewPager.setPageMargin(50);
        ArrayList<Uri> uriList = getIntent().getExtras().getParcelableArrayList("imgUris");

        viewPager.setAdapter(new ViewPagerAdapter(getApplicationContext(), null, uriList, true));
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            @Override
            public void run() {
                if(currentPage == uriList.size()) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int cnt = 0;
            @Override
            public void run() {
                if (cnt < uriList.size())
                    handler.post(Update);
                else if (cnt == uriList.size()) {
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName(getApplicationContext(),
                            CustomResultActivity.class);
                    intent.setComponent(componentName);
                    startActivity(intent);
                    finish();
                }
                cnt++;
            }
        }, 0, 2000);

    }
}