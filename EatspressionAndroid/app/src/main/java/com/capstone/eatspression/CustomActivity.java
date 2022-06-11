package com.capstone.eatspression;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

//https://lakue.tistory.com/52  ViewPager에 대한 내용은 여기 참고
public class CustomActivity extends AppCompatActivity {
//    RecyclerView recyclerView;
    ViewPager viewPager;
    ArrayList<Uri> uriList = new ArrayList<>();
    ViewPagerAdapter pagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);


        Button selectButton = findViewById(R.id.selectImageButton);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {        // 현재는 갤러리 어플을 이용해서 선택 중... 나중에 이쁜 UI로 바꾸자
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2222);
            }
        });

        Button startCustomButton = findViewById(R.id.startCustomButton);
        startCustomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uriList.size() > 0) {
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName(getApplicationContext(),
                            StartCustomActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("imgUris", uriList);
                    intent.putExtras(bundle);

                    intent.setComponent(componentName);
                    startActivity(intent);
//                    finish();                         // finish를 안해줘야 result화면에서 uri로 이미지에 접근 가능함..
                }
                else
                    Toast.makeText(getApplicationContext(), "이미지를 갤러리에서 가져와 주세요!", Toast.LENGTH_LONG).show();
            }
        });


        // navigation bar start
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
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName(getApplicationContext(),
                        SettingActivity.class);

                intent.setComponent(componentName);
                startActivity(intent);
                finish();
            }
        });
        Button customButton = findViewById(R.id.customButton);
        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        // navigation bar end



//        recyclerView = findViewById(R.id.selectedImages);
        viewPager = findViewById(R.id.viewPager);

        viewPager.setClipToPadding(false);

        viewPager.setPadding(50, 0, 50, 0);
        viewPager.setPageMargin(50);


        permissionCheck();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2222){
            if(data == null){                       // 어떤 이미지도 선택하지 않은 경우
                Toast.makeText(getApplicationContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
            }
            else{                                   // 이미지를 하나라도 선택한 경우
                if(data.getClipData() == null){     // 이미지를 하나만 선택한 경우
                    Uri imageUri = data.getData();
                    uriList.add(imageUri);
                    MultiImageAdapter adapter = new MultiImageAdapter(uriList, getApplicationContext());
//                    recyclerView.setAdapter(adapter);
//                    recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));

                    pagerAdapter = new ViewPagerAdapter(this, null, uriList, true, false);
                    viewPager.setAdapter(pagerAdapter);
                }
                else{                               // 이미지를 여러장 선택한 경우
                    ClipData clipData = data.getClipData();

                    if(clipData.getItemCount() > 10){
                        Toast.makeText(getApplicationContext(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                    }
                    else{
                        uriList.clear();
                        for (int i = 0; i < clipData.getItemCount(); i++){
                            Uri imageUri = clipData.getItemAt(i).getUri();
                            try {
                                uriList.add(imageUri);
                            } catch (Exception e) {
                                Log.e("MultiImageActivity", "File select error", e);
                            }
                        }

                        MultiImageAdapter adapter = new MultiImageAdapter(uriList, getApplicationContext());
//                        recyclerView.setAdapter(adapter);
//                        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                        pagerAdapter = new ViewPagerAdapter(this, null, uriList, true, false);
                        viewPager.setAdapter(pagerAdapter);
                    }
                }
                ImageView defaultImage = findViewById(R.id.defaultImage);
                defaultImage.setVisibility(View.INVISIBLE);
//                recyclerView.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.VISIBLE);
            }
        }
    }


    private PermissionSupport permission;
    public void permissionCheck() {
        // PermissionSupport.java 클래스 객체 생성
        permission = new PermissionSupport(this, this, getPackageName());
        permission.checkNeedAlert();
    }
}