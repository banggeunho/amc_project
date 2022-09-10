package com.asanwatch.measure.Device;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.asanwatch.measure.MainActivity;
import com.asanwatch.measure.Retrofit.RequestBody.RequestDevice;
import com.asanwatch.measure.Retrofit.RetroCallback;
import com.asanwatch.measure.Service.MeasureClass;
import com.asanwatch.measure.Setting.SharedObjects;
import com.asanwatch.measure.Util.BatteryUtils;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DeviceInfo extends View {
    private HashMap<String, Object> sensorList;
    private SensorManager mSensorManager;
    public static String androidId;
    private static final String TAG = "DeviceInfo";





    public DeviceInfo(Context context) {
        super(context);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        sensorList = checkSensorAvailability();

//        Log.d(TAG, sensorList.toString());
//        Log.d(TAG, sensorList.get("sensorNames").toString());
//        Log.d(TAG, sensorList.get("sensorTypes").toString());

    }

    public void sendDeviceInfo() {
        HashMap<String, Object> values = new HashMap<String, Object>();

        values.put("deviceID", SharedObjects.deviceId);
        values.put("vendor", SharedObjects.deviceBrand);
        values.put("sensorNames", (ArrayList<String>) sensorList.get("sensorNames") );
        values.put("sensorTypes", (ArrayList<Integer>) sensorList.get("sensorTypes"));

        RequestDevice data = new RequestDevice(values);
        SharedObjects.retroClient.postDeviceData(data, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
            }

            @Override
            public void onFailure(int code) {
            }
        });
    }

    private HashMap<String, Object> checkSensorAvailability() {
        ArrayList<Integer> sensorTypeList = new ArrayList<Integer>(Arrays.asList(5, 6, 18, 21, 1, 2, 4, 9, 15));
        HashMap<String, Object> sensorList = new HashMap<String, Object>();
        ArrayList<String> sensorNames = new ArrayList<String>();
        ArrayList<Integer> sensorTypes = new ArrayList<Integer>();
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL); //Step 6: List of integrated sensors.
        for (Sensor sensor : sensors) {
            if (sensor.getType() < Sensor.TYPE_DEVICE_PRIVATE_BASE) {
                //check accessibility for 3rd party developers/public-------
                if ((mSensorManager.getDefaultSensor(sensor.getType())) != null) { //Step 7: Check for particular sensor, if return value then the sensor is accessible
//                    Log.d(TAG, sensor.toString());
                    if (sensorTypeList.contains(sensor.getType())) {
                        sensorNames.add(sensor.getName());
                        sensorTypes.add(sensor.getType());
                    }
//                    Log.d(TAG, sensor.getName() + " is available\n");
                } else { //Step 6: If return null, then sensor is not accessible and entered into else method.
                    Log.d(TAG, sensor.getName() + " is not available\n");
                }
            }
        }
        sensorList.put("sensorNames", sensorNames);
        sensorList.put("sensorTypes", sensorTypes);
        return sensorList;
    }


}
