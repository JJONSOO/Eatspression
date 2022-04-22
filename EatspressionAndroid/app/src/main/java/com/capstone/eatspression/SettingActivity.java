package com.capstone.eatspression;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {
    String[] distanceStrArr = {"100m", "300m", "500m", "1km", "식당까지의 거리"};
    String[] recommendNumStrArr = {"10개", "15개", "20개", "25개", "30개", "식당 추천 후보 개수"};
    int[] distanceArr = {100, 300, 500, 1000};
    int[] recommendNumArr = {10, 15, 20, 25, 30};
    int selectedDistIdx = -1;
    int selectedRecommendNumIdx = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Spinner distanceSpinner = findViewById(R.id.distanceSpinner);
        spinnerInit(distanceSpinner, distanceStrArr, true);
        Spinner recommendNumSpinner = findViewById(R.id.recommendNumSpinner);
        spinnerInit(recommendNumSpinner, recommendNumStrArr, false);

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedDistIdx != distanceArr.length && selectedRecommendNumIdx != recommendNumArr.length) {
                    Toast.makeText(getApplicationContext(), "시작하자!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "거리와 후보 개수를 지정해주세요!", Toast.LENGTH_LONG).show();
                }
            }
        });
        Button homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button mainButton = findViewById(R.id.mainButton);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        Button customButton = findViewById(R.id.customButton);
        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName(getApplicationContext(),
                        CustomActivity.class);

                intent.setComponent(componentName);
                startActivity(intent);
                finish();
            }
        });

        Button infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName(getApplicationContext(),
                        InfoActivity.class);

                intent.setComponent(componentName);
                startActivity(intent);
            }
        });


        permissionCheck();

    }

    protected void spinnerInit(Spinner spinner, String[] stringArr, boolean isDist) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_spinner,
                stringArr) {
            @Override
            public int getCount() {
                return super.getCount() - 1;
            }
        };

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getCount());

        if (isDist) {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    selectedDistIdx = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    selectedDistIdx = -1;
                }
            });
        }
        else {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    selectedRecommendNumIdx = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    selectedRecommendNumIdx = -1;
                }
            });
        }
    }


    private PermissionSupport permission;
    public void permissionCheck() {
        // PermissionSupport.java 클래스 객체 생성
        permission = new PermissionSupport(this, this, getPackageName());
        permission.checkNeedAlert();
    }
}