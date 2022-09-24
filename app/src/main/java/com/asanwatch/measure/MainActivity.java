package com.asanwatch.measure;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.asanwatch.measure.Retrofit.ResponseBody.ResponseGet;
import com.asanwatch.measure.Retrofit.RetroCallback;
import com.asanwatch.measure.Retrofit.RetroClient;
import com.asanwatch.measure.Util.CustomDialog;
import com.asanwatch.measure.databinding.ActivityMainBinding;
import com.asanwatch.measure.Device.DeviceInfo;
import com.asanwatch.measure.Service.MeasureClass;
import com.asanwatch.measure.Setting.SharedObjects;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static Context context;
    private static TextView serverText;
    private static TextView networkText;
    private Button startButton, settingButton, ipconfigButton;
    private ActivityMainBinding binding;
    private Intent backgroundService;
    private DeviceInfo di;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 앱 추가 권한 설정
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS, Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }

        // Main activity context 가져오기
        MainActivity.context = getApplicationContext();

        // PowerManger 설정 부분 & Wake Lock
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 서비스 클래스 인텐트 불러오기
        backgroundService = new Intent(getApplicationContext(), MeasureClass.class);


        // Device info 받아오고, 서버에 전송하기
        di = new DeviceInfo(getApplicationContext());
        di.sendDeviceInfo();
        settingMeasuringOption();

        // UI 바인딩
        serverText = binding.serverStatusText;
        networkText = binding.networkStatusText;
        startButton = binding.startButton;
        settingButton = binding.getSetting;
        ipconfigButton = binding.ipconfigButton;

        // 측정 버튼 동작 (콜백 함수)
        startButton.setOnClickListener(v -> {
            if(!isServiceRunning(MeasureClass.class)) // 측정 중이 아닐 경우
                notificationOfStart();
            else { notificationOfStop(); }
        });

        // 네트워크 설정 변경
        ipconfigButton.setOnClickListener(v -> {
            settingDialog();
        });

        // 워치 세팅 정보 받아옵니다.
        settingButton.setOnClickListener(v -> {
            settingMeasuringOption();

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedObjects.isWake = true;
        setNetworkTextWithPing(isNetworkOK(getApplicationContext()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedObjects.isWake = false;
    }

    public void settingMeasuringOption(){
        Log.d(TAG, "Get setting options from server.");
        SharedObjects.retroClient.getSettingInfo(SharedObjects.deviceId, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                ResponseGet data = (ResponseGet) receivedData;
                SharedObjects.bufferSize = data.getBufferSize();
                SharedObjects.samplerate = data.getSamplingRate();
                SharedObjects.deviceNum = data.getDeviceNum();
            }

            @Override
            public void onFailure(int code) {
                Log.d(TAG,"Bad Request : "+code);
            }
        });
    }

    public boolean isServiceRunning(Class<?> myClass) {
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo rsi : am.getRunningServices(Integer.MAX_VALUE)) {
            if (myClass.getName().equals(rsi.service.getClassName()))
            return true;
        }
        return false;
    }

    private void setNetworkTextWithPing(boolean result){
        if(result){
            SpannableString s = new SpannableString("네트워크 상태 : OK");
            Spannable span = (Spannable) s;
            span.setSpan(new ForegroundColorSpan(Color.GREEN), 9, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            networkText.setText(span);
        }else{
            SpannableString s = new SpannableString("네트워크 상태 : NOT OK");
            Spannable span = (Spannable) s;
            span.setSpan(new ForegroundColorSpan(Color.RED), 9, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            networkText.setText(span);
        }
    }


    public static void setServerStatusText(boolean result){
        if(result){
            SpannableString s = new SpannableString("서버 상태 : OK");
            Spannable span = (Spannable) s;
            // 색깔이 지정 안되어 있거나, 레드일 경우 -> 그린으로 변경
            if (SharedObjects.currentTextColor == 0 || SharedObjects.currentTextColor == Color.RED) {
                span.setSpan(new ForegroundColorSpan(Color.GREEN), 8, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                SharedObjects.currentTextColor = Color.GREEN;
            }
            // 측정중인 상태를 보여주기 위한 구문 (Green과 Cyan 반복)
            else if (SharedObjects.currentTextColor == Color.GREEN) {
                span.setSpan(new ForegroundColorSpan(Color.CYAN), 8, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                SharedObjects.currentTextColor = Color.CYAN;
            }
            else if (SharedObjects.currentTextColor == Color.CYAN) {
                span.setSpan(new ForegroundColorSpan(Color.GREEN), 8, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                SharedObjects.currentTextColor = Color.GREEN;
            }
            serverText.setText(span);

        }else{
            SpannableString s = new SpannableString("서버 상태 : NOT OK");
            Spannable span = (Spannable) s;
            span.setSpan(new ForegroundColorSpan(Color.RED), 8, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            serverText.setText(span);
        }
    }

    public static boolean isNetworkOK(Context context){ //해당 context의 서비스를 사용하기위해서 context객체를 받는다.
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if(networkInfo != null){
            return true;
        }
        return false;  //연결이 되지않은 상태
    }

    public void notificationOfStart(){
        String samplingRate;

        if (SharedObjects.samplerate == 0) samplingRate = "FASTEST MODE";
        else if (SharedObjects.samplerate == 1) samplingRate = "GAME MODE";
        else if (SharedObjects.samplerate == 2) samplingRate = "UI MODE";
        else samplingRate = "NORMAL MODE";

//        CustomDialog customDialog = new CustomDialog(MainActivity.this);
        AlertDialog.Builder setdialog = new AlertDialog.Builder(MainActivity.this);
        String sb = "Sampling rate : " + samplingRate + "\n" +
                    "Frame size : " + SharedObjects.bufferSize + "\n" +
                    "위와 같은 설정으로 측정 시작할까요?";


        setdialog.setTitle("Notification")
                .setMessage(sb)
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Service is started.");
                        startButton.setText("측정종료");
                        wakeLock.acquire();
                        startForegroundService(backgroundService);
                        Toast.makeText(MainActivity.this, "Start to measure..", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                })
                .create()
                .show();

    }

    public void notificationOfStop(){
        AlertDialog.Builder setdialog = new AlertDialog.Builder(MainActivity.this);
        String sb = "정말로 측정을 중단하시겠습니까?";

        setdialog.setTitle("Notification")
                .setMessage(sb)
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Service is stopped.");
                        startButton.setText("측정시작");
                        stopService(backgroundService);
                        wakeLock.release();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                })
//                .setView(R.layout.dialog_confirm)
                .create()
                .show();

    }

    public void settingDialog(){
        final EditText ip = new EditText(MainActivity.this);
        final EditText port = new EditText(MainActivity.this);

        LinearLayout container = new LinearLayout(MainActivity.this);
        LinearLayout.LayoutParams params = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);

        container.setOrientation(LinearLayout.VERTICAL);

        ip.setText(SharedObjects.serverIp);
        port.setText(SharedObjects.serverPort);

        ip.setLayoutParams(params);
        port.setLayoutParams(params);
        container.addView(ip, 0);
        container.addView(port, 1);

        AlertDialog.Builder setdialog = new AlertDialog.Builder(MainActivity.this);
        String sb = "서버 네트워크 주소를 입력해주세요";

        setdialog.setTitle("Network Setting")
//                .setMessage(sb)
                .setView(container)
                .setPositiveButton("변경", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "네트워크 설정 변경 완료", Toast.LENGTH_SHORT).show();
                        SharedObjects.serverIp = ip.getText().toString();
                        SharedObjects.serverPort = port.getText().toString();
                        SharedObjects.Base_URL = "http://"+ SharedObjects.serverIp + ":" + SharedObjects.serverPort;

                        RetroClient retroClient = new RetroClient(MainActivity.this);
                        SharedObjects.retroClient = retroClient.getInstance(MainActivity.this).createBaseApi();
                        Log.d(TAG, RetroClient.baseUrl);
                        di = new DeviceInfo(getApplicationContext());
                        di.sendDeviceInfo();
                        settingMeasuringOption();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                })
//                .setView(R.layout.dialog_confirm)
                .create()
                .show();


    }

    public static Context ApplicationContext(){
        return MainActivity.context;
    }

}