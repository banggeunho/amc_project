package com.asanwatch.measure.Setting;


import com.asanwatch.measure.MainActivity;
import com.asanwatch.measure.Device.DeviceInfoUtil;
import com.asanwatch.measure.Retrofit.RetroClient;

import java.util.HashMap;

public class SharedObjects {
    public static RetroClient retroClient = RetroClient.getInstance(MainActivity.ApplicationContext()).createBaseApi();

    public static String deviceBrand = DeviceInfoUtil.getDeviceBrand();
    public static String deviceId = DeviceInfoUtil.getDeviceId(MainActivity.ApplicationContext());
    public static HashMap<Integer, HashMap> sensorData;
    public static HashMap<Integer, Integer> sensorType;

    public static boolean isWake = true;

    //한 번에 얼마나 보낼지
    public static int framesize = 200;

    //SENSOR_DELAY_FASTEST 0
    //SENSOR_DELAY_GAME 1
    //SENSOR_DELAY_UI 2
    //SENSOR_DELAY_NORMAL 3
    public static int samplerate = 3;

    //SENSOR_STATUS_ACCURACY_LOW 1
    //SENSOR_STATUS_ACCURACY_MEDIUM 2
    //SENSOR_STATUS_ACCURACY_HIGH 3
    public static int sensor_accuracy = 1;

    public static int step_count = 0;

    public static int currentTextColor = 0;

}