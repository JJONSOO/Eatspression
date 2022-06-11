package com.capstone.eatspression;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class SendStructure {
    public Bitmap img;
    public int imgCnt;
    public SendStructure(Bitmap img, int imgCnt) {
        this.img = img;
        this.imgCnt = imgCnt;
    }
}

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    SurfaceHolder holder;
    Camera camera = null;
    public Bitmap image;
    int h, w, format;
    boolean customFlag = false;
    ArrayList<SendStructure> sendItemList = new ArrayList<>();
    public ArrayList<Integer> dataList = new ArrayList<>();
    int sendCnt = 0;
    int totalImageNum;
    // image를 보내는 연결된 HttpURLConnection!
    // 보내는 방법은 https://sesang06.tistory.com/19를 참조하자!


    ArrayList<Thread> futureList = new ArrayList<>();

    public CameraSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        //초기화를 위한 메소드
        holder = getHolder();
        holder.addCallback(this);
    }

    private int findFrontSideCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo cmInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cmInfo);
            if (cmInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }

        return cameraId;
    }

    boolean isFirstTime = true;
    long startTime = 0;
    private String getStringFromBitmap(Bitmap bitmapPicture) {
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //만들어지는시점
        camera  = Camera.open(findFrontSideCamera());//카메라 객체 참조
        try{
            camera.setPreviewDisplay(holder);

            camera.setPreviewCallback(new Camera.PreviewCallback() {
                boolean sendFirst = false;
                public void onPreviewFrame(byte[] data, Camera camera2) {
                    synchronized(dataList) {
                        if (dataList.size() != 2 || dataList.get(0) < 3)
                            return;
                    }

                    if (isFirstTime) {
                        isFirstTime = false;
                        startTime = System.currentTimeMillis();
                    } else {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - startTime < 300)
                            return;
                        startTime = currentTime;
                    }


                    if (sendFirst) {
                        synchronized(dataList) {
                            if (dataList.get(0) < 3)
                                return;
                        }

                        try {
                            synchronized (camera2) {
                                if (camera2 != null) {
                                    Camera.Parameters params = camera2.getParameters();
                                    w = params.getPreviewSize().width;
                                    h = params.getPreviewSize().height;
                                    format = params.getPreviewFormat();
                                }
                            }
                        } catch(Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        YuvImage yuv = new YuvImage(data,  format, w, h, null);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        Rect area = new Rect(0, 0, w, h);
                        yuv.compressToJpeg(area, 50, out);
                        Bitmap img = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());

                        int width = 300; // 축소시킬 너비
                        int height = 300; // 축소시킬 높이

                        if (w > width) {
                            // 원하는 너비보다 클 경우의 설정
                            float mWidth = w / 100;
                            float scale = width/ mWidth;
                            w *= (scale / 100);
                            h *= (scale / 100);
                        } else if (h > height) {
                            // 원하는 높이보다 클 경우의 설정
                            float mHeight = h / 100;
                            float scale = height/ mHeight;
                            w *= (scale / 100);
                            h *= (scale / 100);
                        }

                        image = Bitmap.createScaledBitmap(img, w, h, true);

                        synchronized(dataList) {
                            synchronized(sendItemList) {
                                sendItemList.add(new SendStructure(image.copy(image.getConfig(), true), dataList.get(0) - 3));
                            }
                        }

                        final Thread th = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String page;
                                    if (customFlag) {
                                        page = "http://18.144.29.108:8080/restraunt/custom/image";
                                    } else {
                                        page = "http://18.144.29.108:8080/restraunt/image";
                                    }

                                    // URL 객체 생성
                                    URL url = new URL(page);
                                    // 연결 객체 생성
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                                    // Post 파라미터
                                    JSONObject jsonObject = new JSONObject();
                                    synchronized (dataList) {
                                        jsonObject.put("user_id", dataList.get(1).toString());
                                    }
                                    int imgNum;
                                    synchronized (sendItemList) {
                                        if (sendCnt >= sendItemList.size())
                                            return;
                                        jsonObject.put("image", getStringFromBitmap(sendItemList.get(sendCnt).img));
                                        imgNum = sendItemList.get(sendCnt++).imgCnt;
                                    }
                                    jsonObject.put("img_num", imgNum);


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

//                                    // url에 접속 성공하면 (200)
                                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                            Log.i("tag", "success: send face photo complete!" + imgNum);
                                        } else {
                                            Log.i("tag", "error: can't send face photo!!");
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.i("tag", "error :" + e);
                                }
                            }
                        });

                        th.start();
                        synchronized (futureList) {
                            futureList.add(th);
                        }
                    } else {
                        try {
                            synchronized (camera2) {
                                if (camera2 != null) {
                                    Camera.Parameters params = camera2.getParameters();
                                    w = params.getPreviewSize().width;
                                    h = params.getPreviewSize().height;
                                    format = params.getPreviewFormat();
                                }
                            }
                        } catch(Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        YuvImage yuv = new YuvImage(data,  format, w, h, null);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        Rect area = new Rect(0, 0, w, h);
                        yuv.compressToJpeg(area, 50, out);
                        Bitmap img = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());

                        int width = 300; // 축소시킬 너비
                        int height = 300; // 축소시킬 높이

                        if (w > width) {
                            // 원하는 너비보다 클 경우의 설정
                            float mWidth = w / 100;
                            float scale = width/ mWidth;
                            w *= (scale / 100);
                            h *= (scale / 100);
                        } else if (h > height) {
                            // 원하는 높이보다 클 경우의 설정
                            float mHeight = h / 100;
                            float scale = height/ mHeight;
                            w *= (scale / 100);
                            h *= (scale / 100);
                        }

                        image = Bitmap.createScaledBitmap(img, w, h, true);

                        synchronized(dataList) {
                            synchronized(sendItemList) {
                                sendItemList.add(new SendStructure(image.copy(image.getConfig(), true), dataList.get(0) - 3));
                            }
                        }

                        final Thread th = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String page;
                                    if (customFlag) {
                                        page = "http://54.241.56.66:8080/restraunt/first";
                                    } else {
                                        page = "http://54.241.56.66:8080/restraunt/first";
                                    }

                                    // URL 객체 생성
                                    URL url = new URL(page);
                                    // 연결 객체 생성
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                                    // Post 파라미터
                                    JSONObject jsonObject = new JSONObject();
                                    synchronized (sendItemList) {
                                        jsonObject.put("image", getStringFromBitmap(sendItemList.get(sendCnt).img));
                                        jsonObject.put("img_num", sendItemList.get(sendCnt++).imgCnt);
                                    }
                                    synchronized (dataList) {
                                        jsonObject.put("user_id", dataList.get(1).toString());
                                    }
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

//                                    // url에 접속 성공하면 (200)
                                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                            Log.i("tag", "success: send first face photo complete!");
                                        } else {
                                            Log.i("tag", "error: can't send face photo!!");
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.i("tag", "error :" + e);
                                }
                            }
                        });

                        th.start();
                        synchronized (futureList) {
                            futureList.add(th);
                        }
                        sendFirst = true;

                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // camera.setDisplayOrientation(90); // 카메라 미리보기 오른쪽으로 90도 회전
        camera.startPreview();
        Log.i("tag", "surface change");
    }

    // 크기 조절 사용자에게 안보이게...
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.setMeasuredDimension(2, 2);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {


        //소멸
        try {
            while(true) {
                synchronized (sendItemList) {
                    if (sendCnt >= sendItemList.size()) {
                        break;
                    }
                }
                Log.i("tag", "continue...");
                Thread.sleep(100);
            }


            synchronized (camera) {
                if (camera != null) {
                    camera.stopPreview();       //미리보기중지
                    camera.setPreviewCallback(null);
                    camera.release();
                }
            }
        } catch(Exception e) {}

        // 최종적으로 Thread들 join 해서 없애주기
        synchronized(futureList) {
            for (Thread elem : futureList) {
                try {
                    elem.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        camera = null;
        Log.i("tag", "결과 카메라 종료");
    }
}