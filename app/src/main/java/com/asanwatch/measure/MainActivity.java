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
import android.os.Trace;
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

import org.w3c.dom.Text;

import java.security.Signature;

import retrofit2.Response;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static Context context;
    private static TextView serverText;
    private static TextView networkText;
    private static TextView patientNumText;
    private static TextView patientRoomText;
    private static TextView watchNumText;
    private Button startButton, settingButton, ipconfigButton;
    private ActivityMainBinding binding;
    private Intent backgroundService;
    private DeviceInfo di;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ??? ?????? ?????? ??????
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS, Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }

        // Main activity context ????????????
        MainActivity.context = getApplicationContext();

        // PowerManger ?????? ?????? & Wake Lock
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ????????? ????????? ????????? ????????????
        backgroundService = new Intent(getApplicationContext(), MeasureClass.class);


        // Device info ????????????, ????????? ????????????
        di = new DeviceInfo(getApplicationContext());
        di.sendDeviceInfo();
        settingMeasuringOption();

        // UI ?????????
        serverText = binding.serverStatusText;
        networkText = binding.networkStatusText;
        startButton = binding.startButton;
        settingButton = binding.getSetting;
        ipconfigButton = binding.ipconfigButton;
        patientNumText = binding.patientNumText;
        patientRoomText = binding.roomNumText;
        watchNumText = binding.watchNumText;

        // ?????? ?????? ?????? (?????? ??????)
        startButton.setOnClickListener(v -> {
            if(!isServiceRunning(MeasureClass.class)) // ?????? ?????? ?????? ??????
                notificationOfStart();
            else { notificationOfStop(); }
        });

        // ???????????? ?????? ??????
        ipconfigButton.setOnClickListener(v -> {
            settingDialog();
        });

        // ?????? ?????? ?????? ???????????????.
        settingButton.setOnClickListener(v -> {
            di.sendDeviceInfo();
            settingMeasuringOption();

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedObjects.isWake = true;
        setNetworkTextWithPing(isNetworkOK(getApplicationContext()));
        di.sendDeviceInfo();
        settingMeasuringOption();
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
                Toast.makeText(MainActivity.ApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT);
            }

            @Override
            public void onSuccess(int code, Object receivedData) {

                ResponseGet data = (ResponseGet) receivedData;
                Log.d(TAG, data.toString());
                // buffer size??? 0??? ??????
                if (data.getBufferSize() == 0){
                    SharedObjects.bufferSize = 50;
                }
                else { SharedObjects.bufferSize = data.getBufferSize();}
                SharedObjects.samplerate = data.getSamplingRate();
                SharedObjects.deviceNum = data.getDeviceNum();
                if (SharedObjects.isWake) {
                    MainActivity.setServerStatusText(true);
                    MainActivity.setPatientText(data);
                }
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
            SpannableString s = new SpannableString("???????????? ?????? : OK");
            Spannable span = (Spannable) s;
            span.setSpan(new ForegroundColorSpan(Color.GREEN), 9, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            networkText.setText(span);
        }else{
            SpannableString s = new SpannableString("???????????? ?????? : NOT OK");
            Spannable span = (Spannable) s;
            span.setSpan(new ForegroundColorSpan(Color.RED), 9, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            networkText.setText(span);
        }
    }


    public static void setServerStatusText(boolean result){
        if(result){
            SpannableString s = new SpannableString("?????? ?????? : OK");
            Spannable span = (Spannable) s;
            // ????????? ?????? ????????? ?????????, ????????? ?????? -> ???????????? ??????
            if (SharedObjects.currentTextColor == 0 || SharedObjects.currentTextColor == Color.RED) {
                span.setSpan(new ForegroundColorSpan(Color.GREEN), 8, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                SharedObjects.currentTextColor = Color.GREEN;
            }
            // ???????????? ????????? ???????????? ?????? ?????? (Green??? Cyan ??????)
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
            SpannableString s = new SpannableString("?????? ?????? : NOT OK");
            Spannable span = (Spannable) s;
            span.setSpan(new ForegroundColorSpan(Color.RED), 8, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            serverText.setText(span);
        }
    }

    public static boolean isNetworkOK(Context context){ //?????? context??? ???????????? ????????????????????? context????????? ?????????.
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if(networkInfo != null){
            return true;
        }
        return false;  //????????? ???????????? ??????
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
                    "?????? ?????? ???????????? ?????? ????????????????";


        setdialog.setTitle("Notification")
                .setMessage(sb)
                .setPositiveButton("???", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Service is started.");
                        startButton.setText("????????????");
                        wakeLock.acquire();
                        startForegroundService(backgroundService);
                        Toast.makeText(MainActivity.this, "Start to measure..", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
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
        String sb = "????????? ????????? ?????????????????????????";

        setdialog.setTitle("Notification")
                .setMessage(sb)
                .setPositiveButton("???", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Service is stopped.");
                        startButton.setText("????????????");
                        stopService(backgroundService);
                        wakeLock.release();
                    }
                })
                .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
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
        String sb = "?????? ???????????? ????????? ??????????????????";

        setdialog.setTitle("Network Setting")
//                .setMessage(sb)
                .setView(container)
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "???????????? ?????? ?????? ??????", Toast.LENGTH_SHORT).show();
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
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                })
//                .setView(R.layout.dialog_confirm)
                .create()
                .show();


    }


    public static void setPatientText(ResponseGet result ){
        SpannableString s1 = new SpannableString("?????? ID: "+result.getPatientId());
        SpannableString s2 = new SpannableString("??????: "+result.getRoomNum());
        SpannableString s3 = new SpannableString("?????? ID: "+result.getDeviceNum());
//        Log.d(TAG, result.toString());
        Spannable patientNum = (Spannable) s1;
        Spannable roomNum = (Spannable) s2;
        Spannable deviceNum = (Spannable) s3;
        patientNumText.setText(patientNum);
        patientRoomText.setText(roomNum);
        watchNumText.setText(deviceNum);
    }


    public static Context ApplicationContext(){
        return MainActivity.context;
    }

}