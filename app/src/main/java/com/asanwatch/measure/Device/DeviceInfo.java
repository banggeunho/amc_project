package com.asanwatch.measure.Device;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.asanwatch.measure.Retrofit.RequestBody.RequestDevice;
import com.asanwatch.measure.Retrofit.RetroCallback;
import com.asanwatch.measure.Setting.SharedObjects;

import java.util.HashMap;
import java.util.List;

public class DeviceInfo extends View {
    private HashMap<String, String> sensorList;
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
        HashMap<String, String> values = new HashMap<String, String>();
        String sensorName = sensorList.get("sensorNames").toString();
        String sensorType = sensorList.get("sensorTypes").toString();

        sensorName = sensorName.substring(0, sensorName.length()-1);
        sensorType = sensorType.substring(0, sensorType.length()-1);

        values.put("deviceID", SharedObjects.deviceId);
        values.put("deviceBrand", SharedObjects.deviceBrand);
        values.put("sensorNames", sensorName);
        values.put("sensorTypes", sensorType);

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

    private HashMap<String, String> checkSensorAvailability() {
        HashMap<String, String> sensorList = new HashMap<String, String>();
        StringBuilder sensorNames = new StringBuilder();
        StringBuilder sensorTypes = new StringBuilder();
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL); //Step 6: List of integrated sensors.
        for (Sensor sensor : sensors) {
            if (sensor.getType() < Sensor.TYPE_DEVICE_PRIVATE_BASE) {
                //check accessibility for 3rd party developers/public-------
                if ((mSensorManager.getDefaultSensor(sensor.getType())) != null) { //Step 7: Check for particular sensor, if return value then the sensor is accessible
//                    Log.d(TAG, sensor.toString());
                    sensorNames.append(sensor.getName()+",");
                    sensorTypes.append(sensor.getType()+",");
//                    Log.d(TAG, sensor.getName() + " is available\n");
                } else { //Step 6: If return null, then sensor is not accessible and entered into else method.
                    Log.d(TAG, sensor.getName() + " is not available\n");
                }
            }
        }
        sensorList.put("sensorNames", sensorNames.toString());
        sensorList.put("sensorTypes", sensorTypes.toString());
        return sensorList;
    }
}
