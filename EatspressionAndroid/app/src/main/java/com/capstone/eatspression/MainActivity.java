package com.capstone.eatspression;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private PermissionSupport permission;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        SharedPreferences pref = getSharedPreferences("isFirst", Activity.MODE_PRIVATE);
//        boolean first = pref.getBoolean("isFirst", false);
//        if(first==false){
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putBoolean("isFirst",true);
//            editor.commit();
//            //앱 최초 실행시 하고 싶은 작업
//            Intent infoIntent = new Intent();
//            ComponentName componentName = new ComponentName(getApplicationContext(), ManualActivity.class);
//            infoIntent.setComponent(componentName);
//            startActivity(infoIntent);
//        }


        Button mainButton = findViewById(R.id.mainButton);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName(getApplicationContext(),
                        SettingActivity.class);

                intent.setComponent(componentName);
                startActivity(intent);

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
            }
        });

        Button manualButton = findViewById(R.id.manualButton);
        manualButton.setOnClickListener(new View.OnClickListener() {
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


    // 권한 체크
    private void permissionCheck() {
        // PermissionSupport.java 클래스 객체 생성
        permission = new PermissionSupport(this, this);

        // 권한 체크 후 리턴이 false...
        if (!permission.checkPermission()){
            //권한 요청
            permission.requestPermission();
        }

    }
}