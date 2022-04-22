package com.capstone.eatspression;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Button button = findViewById(R.id.backwardButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent infoIntent = new Intent();
//                ComponentName componentName = new ComponentName(getApplicationContext(), MainActivity.class);
//                infoIntent.setComponent(componentName);
//                startActivity(infoIntent);
                finish();
            }
        });
    }
}