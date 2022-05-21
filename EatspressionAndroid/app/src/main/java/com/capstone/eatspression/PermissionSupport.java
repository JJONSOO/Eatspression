package com.capstone.eatspression;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

// https://haruvely.tistory.com/9?category=523153 참고. 여러 권한을 한번에 받아주는 것을 도와주는 클래스
public class PermissionSupport {
    private Context context;
    private Activity activity;
    private String packageName;
    // 권한 요청 배열
    private String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private List permissionList;

    private final int MULTIPLE_PERMISSIONS = 1023;


    public PermissionSupport(Activity _activity, Context _context) {
        this.activity = _activity;
        this.context = _context;
        this.packageName = "";
    }

    public PermissionSupport(Activity _activity, Context _context, String packageName) {
        this.activity = _activity;
        this.context = _context;
        this.packageName = packageName;
    }

    // 권한 허용 체크
    public boolean checkPermission() {
        int result;
        permissionList = new ArrayList<>();

        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(context, pm);
            // 권한 없으면 리스트에...
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }

        if (!permissionList.isEmpty()) {
            return false;
        }
        return true;
    }

    // 여러개를 한번에 받아주는 함수 -> MULTIPLE_PERMISSIONS를 추가하면 되는구나!
    public void requestPermission() {
        ActivityCompat.requestPermissions(activity, (String[])permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
    }


    public void checkNeedAlert() {
        if (!checkPermission()) {
            // https://ddolcat.tistory.com/79 참고. 거부할 시 권한을 얻어야 하는 경고문과 앱 설정으로 이동해주는 기능
            AlertDialog.Builder permissionAlert = new AlertDialog.Builder(context);
            permissionAlert.setTitle("\'Eatspression\'에서 위치와 카메라에 접근하도록 허용해주세요.");
            permissionAlert.setMessage("위치 정보와 카메라를 사용하지 않을 경우 본 어플의 사용이 제한됩니다.");
            permissionAlert.setPositiveButton("네", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                    Intent appDetail = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName));
                    appDetail.addCategory(Intent.CATEGORY_DEFAULT);
                    appDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(appDetail);
                }
            });

            permissionAlert.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((Activity)context).finish();
                }
            });

            permissionAlert.setCancelable(false);
            permissionAlert.create().show();
        }
    }


}
