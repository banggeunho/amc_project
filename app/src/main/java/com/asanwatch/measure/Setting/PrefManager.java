package com.asanwatch.measure.Setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PrefManager {

    private Context context;
    private static SharedPreferences sharedPreferences;
    private static String TAG = "prefManager";

    public PrefManager(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("device_setting", Context.MODE_PRIVATE);
    }

    public static void getPreferences(){ //설정 가져오기
        SharedObjects.framesize = sharedPreferences.getInt("framesize", 200);
        SharedObjects.samplerate = sharedPreferences.getInt("samplerate", 3);
        SharedObjects.sensor_accuracy = sharedPreferences.getInt("sensor_accuracy",1);
        Log.d(TAG, "framesize : " + SharedObjects.framesize + ", samplerate : " + SharedObjects.samplerate + ", sensor_accuracy : " + SharedObjects.sensor_accuracy);
    }

    public static void setPreferences(int framesize, int samplerate, int sensor_accuracy){ //설정 저장하기
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("framesize", framesize);
        editor.putInt("samplerate", samplerate);
        editor.putInt("sensor_accuracy", sensor_accuracy);
        editor.commit();

        getPreferences();
    }

}
