package com.asanwatch.measure.Util;


import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.DebugUtils;

import com.asanwatch.measure.MainActivity;

public class BatteryUtils {

    public static Intent getBatteryIntent() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        return MainActivity.ApplicationContext().registerReceiver(null, ifilter);
    }

    public static int getCurrentBattery(Intent intent) {
        return intent.getIntExtra(String.valueOf(BatteryManager.BATTERY_PROPERTY_CAPACITY), -1);
    }

    public static int getScale(Intent intent) {
        return intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    }

    public static int getLevel(Intent intent) {
        return intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    }

    public static int getChargeStatus(Intent intent) {
        return intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
    }

    public static boolean isCharging(int status) {
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
    }

    public static boolean isCharging(Intent intent) {
        int status = getChargeStatus(intent);
        return isCharging(status);
    }

//    public static void test() {
//        Intent intent = BatteryManager.getBatteryIntent();
//        DebugUtils.log("isCharging:" + BatteryUtils.isCharging(intent) + ";level:" + BatteryUtils.getLevel(intent) + ";Scale:" + BatteryUtils.getScale(intent));
//    }

}
