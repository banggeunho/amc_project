package com.asanwatch.measure.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.asanwatch.measure.MainActivity;
import com.asanwatch.measure.Retrofit.RequestBody.RequestHR;
import com.asanwatch.measure.Retrofit.RequestBody.RequestOne;
import com.asanwatch.measure.Retrofit.RequestBody.RequestStep;
import com.asanwatch.measure.Retrofit.RequestBody.RequestThree;
import com.asanwatch.measure.Retrofit.ResponseBody.ResponseGet;
import com.asanwatch.measure.Retrofit.RetroCallback;
import com.asanwatch.measure.Retrofit.RetroClient;
import com.asanwatch.measure.Setting.SharedObjects;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MeasureClass extends Service implements SensorEventListener {
    private static final String TAG = "MeasureForegroundService";
    private SensorManager manager;
    private Sensor[] mSensors;
    private RetroClient retroClient;


    @Override
    public void onStart(Intent intent, int startId) {
        foregroundNotification();
        setSensorData();
        initSensors();
        retroClient = SharedObjects.retroClient;
//        Log.d(TAG, retroClient.toString());
    }


    @Override
    public void onDestroy(){
        unregister();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        long date = System.currentTimeMillis();


        if (SharedObjects.sensorType.get(sensor.getType()) == 1){
            if(sensor.getType() == Sensor.TYPE_STEP_DETECTOR || sensor.getType() == Sensor.TYPE_HEART_RATE)
                sendData_quickly(sensor, event, date);

            else sendData_1(sensor, event, date);
        }

        if (SharedObjects.sensorType.get(sensor.getType()) == 3){
            sendData_3(sensor, event, date);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    void foregroundNotification() { // foreground 실행 후 신호 전달 (안하면 앱 강제종료 됨)
        NotificationCompat.Builder builder;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "measuring_service_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Measuring Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        builder.setContentTitle("측정시작됨")
                .setContentIntent(pendingIntent);

        startForeground(1, builder.build());
    }

    private void initSensors(){

        manager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mSensors = new Sensor[SharedObjects.sensorType.size()];
        Integer i = 0;
        Iterator<Map.Entry<Integer, Integer>> entries = SharedObjects.sensorType.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<Integer, Integer> entry = entries.next();
//            if (entry.getValue()==3) continue;
            mSensors[i] = manager.getDefaultSensor(entry.getKey());
            Log.d(TAG, entry.getKey().toString());
            manager.registerListener(this, mSensors[i], SharedObjects.samplerate);
        }

    }

    public void unregister() { // unregister listener
        manager.unregisterListener(this);
        SharedObjects.sensorData.clear();
        SharedObjects.sensorType.clear();
    }

    public int getBattery() {
        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return batLevel;
    }

    public void setSensorData(){
        SharedObjects.sensorData = new HashMap<Integer, HashMap>();
        SharedObjects.sensorType = new HashMap<Integer, Integer>();

        SharedObjects.sensorType.put(5, 1); // Light
        SharedObjects.sensorType.put(6, 1); // Barometer
        SharedObjects.sensorType.put(18, 1); // step detector
//        SharedObjects.sensorType.put(19, 1); // step counter
        SharedObjects.sensorType.put(21, 1); // Heart_rate
        SharedObjects.sensorType.put(1, 3); // Accelerometer
        SharedObjects.sensorType.put(2, 3); // Magnetometer
        SharedObjects.sensorType.put(4, 3); // GyroScope
        SharedObjects.sensorType.put(9, 3); // Gravity
        SharedObjects.sensorType.put(15, 3); // Game_rotation

        Iterator<Map.Entry<Integer, Integer>> entries = SharedObjects.sensorType.entrySet().iterator();
        while(entries.hasNext()){
            HashMap<String, String> temp = new HashMap<String, String>();
            Map.Entry<Integer, Integer> entry = entries.next();
            if (entry.getValue() == 1){
                temp.put("time", "");
                temp.put("value", "");
                temp.put("count", "0");
                temp.put("frame_num", "0");
                SharedObjects.sensorData.put(entry.getKey(), temp);
            }
            else if (entry.getValue() == 3) {
                temp.put("time", "");
                temp.put("x", "");
                temp.put("y", "");
                temp.put("z", "");
                temp.put("count", "0");
                temp.put("frame_num", "0");
                SharedObjects.sensorData.put(entry.getKey(), temp);
            }
        }
        Log.d(TAG, SharedObjects.sensorType.toString());
        Log.d(TAG, SharedObjects.sensorData.toString());
    }

    public void sendData_1(Sensor sensor, SensorEvent event, long date)
    {
//        Log.d(TAG, sensor.getName() + event.values[0]);
        String time = SharedObjects.sensorData.get(sensor.getType()).get("time").toString();
        Integer count = str2int(SharedObjects.sensorData.get(sensor.getType()).get("count").toString());
        Integer frame_number = str2int(SharedObjects.sensorData.get(sensor.getType()).get("frame_num").toString());
        String value = SharedObjects.sensorData.get(sensor.getType()).get("value").toString();
        // 교체를 위한 삭제
        SharedObjects.sensorData.get(sensor.getType()).remove("time");
        SharedObjects.sensorData.get(sensor.getType()).remove("count");
        SharedObjects.sensorData.get(sensor.getType()).remove("value");

        if(count % SharedObjects.bufferSize == 0 && count != 0) {
//            int battery = getBattery();
//            Log.d(TAG, "현재 배터리 상태: "+ Integer.toString(battery));

            time += Long.toString(date);
            value += event.values[0];

            HashMap<String, String> values = new HashMap<String, String>();

            values.put("time", time);
            values.put("value", value);
            values.put("frame_num", String.valueOf(frame_number));
            values.put("sensor_type", int2str(sensor.getType()));
            values.put("sensor_name", sensor.getName());
            values.put("deviceID", SharedObjects.deviceId);

            Log.d(TAG, sensor.getName()+'/'+int2str(count)+'/'+int2str(frame_number));

            RequestOne data = new RequestOne(values);
            retroClient.postOneData(data, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    ResponseGet data = (ResponseGet) receivedData;
                }

                @Override
                public void onFailure(int code) {
                }
            });


            count = 0;
            frame_number += 1;
            SharedObjects.sensorData.get(sensor.getType()).put("time", "");
            SharedObjects.sensorData.get(sensor.getType()).put("value", "");
            SharedObjects.sensorData.get(sensor.getType()).put("count", int2str(count));
            SharedObjects.sensorData.get(sensor.getType()).remove("frame_num");
            SharedObjects.sensorData.get(sensor.getType()).put("frame_num", int2str(frame_number));

//            if(SharedObjects.isWake){ MainActivity.setServerStatusText(); }

        } else{
            time += Long.toString(date) + ",";
            value += event.values[0] + ",";
            SharedObjects.sensorData.get(sensor.getType()).put("time", time);
            SharedObjects.sensorData.get(sensor.getType()).put("value", value);

        }
        count += 1;
        SharedObjects.sensorData.get(sensor.getType()).put("count", int2str(count));
    }

    public void sendData_3(Sensor sensor, SensorEvent event, long date)
    {
//        Log.d(TAG, SharedObjects.sensorData.toString());
        String time = SharedObjects.sensorData.get(sensor.getType()).get("time").toString();
        Integer count = str2int(SharedObjects.sensorData.get(sensor.getType()).get("count").toString());
        Integer frame_number = str2int(SharedObjects.sensorData.get(sensor.getType()).get("frame_num").toString());

        String x = SharedObjects.sensorData.get(sensor.getType()).get("x").toString();
        String y = SharedObjects.sensorData.get(sensor.getType()).get("y").toString();
        String z = SharedObjects.sensorData.get(sensor.getType()).get("z").toString();
        // 교체를 위한 삭제
        SharedObjects.sensorData.get(sensor.getType()).remove("time");
        SharedObjects.sensorData.get(sensor.getType()).remove("count");
        SharedObjects.sensorData.get(sensor.getType()).remove("x");
        SharedObjects.sensorData.get(sensor.getType()).remove("y");
        SharedObjects.sensorData.get(sensor.getType()).remove("z");

        if(count % SharedObjects.bufferSize == 0 && count != 0) {
//            Log.d(TAG, Integer.toString(battery) + int2str(SharedObjects.framesize));

            time += Long.toString(date);
            x += event.values[0];
            y += event.values[1];
            z += event.values[2];

            HashMap<String, String> values = new HashMap<String, String>();
            values.put("time", time);
            values.put("x", x);
            values.put("y", y);
            values.put("z", z);
            values.put("frame_num", String.valueOf(frame_number));
            values.put("sensor_type", int2str(sensor.getType()));
            values.put("sensor_name", sensor.getName());
            values.put("deviceID", SharedObjects.deviceId);

            Log.d(TAG, sensor.getName()+'/'+int2str(count)+'/'+int2str(frame_number));

            RequestThree data = new RequestThree(values);
            retroClient.postThreeData(data, new RetroCallback() {
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

            count = 0;
            frame_number += 1;
            SharedObjects.sensorData.get(sensor.getType()).put("time", "");
            SharedObjects.sensorData.get(sensor.getType()).put("x", "");
            SharedObjects.sensorData.get(sensor.getType()).put("y", "");
            SharedObjects.sensorData.get(sensor.getType()).put("z", "");
            SharedObjects.sensorData.get(sensor.getType()).put("count", int2str(count));
            SharedObjects.sensorData.get(sensor.getType()).remove("frame_num");
            SharedObjects.sensorData.get(sensor.getType()).put("frame_num", int2str(frame_number));

//            if (SharedObjects.isWake) { MainActivity.setServerStatusText();}
        } else{
            time += Long.toString(date) + ",";
            x += event.values[0] + ",";
            y += event.values[1] + ",";
            z += event.values[2] + ",";
            SharedObjects.sensorData.get(sensor.getType()).put("time", time);
            SharedObjects.sensorData.get(sensor.getType()).put("x", x);
            SharedObjects.sensorData.get(sensor.getType()).put("y", y);
            SharedObjects.sensorData.get(sensor.getType()).put("z", z);

        }
        count += 1;
        SharedObjects.sensorData.get(sensor.getType()).put("count", int2str(count));

    }

    public void sendData_quickly(Sensor sensor, SensorEvent event, long date)
    {
        Integer frame_number = str2int(SharedObjects.sensorData.get(sensor.getType()).get("frame_num").toString());
        Log.d(TAG, sensor.getName() + event.values[0]);
//        Log.d(TAG, "현재 배터리 상태: "+ Integer.toString(battery));
        HashMap<String, String> values = new HashMap<String, String>();
        if (sensor.getType() == sensor.TYPE_STEP_DETECTOR)
        {
            SharedObjects.step_count ++;

            values.put("time", Long.toString(date/1000));
            values.put("value", Float.toString(event.values[0]));
            values.put("frame_num", String.valueOf(frame_number));
            values.put("step_count", int2str(SharedObjects.step_count));
            values.put("sensor_name", sensor.getName());
            values.put("sensor_type", int2str(sensor.getType()));
            values.put("deviceID", SharedObjects.deviceId);
            Log.d(TAG, sensor.getName()+"/걸음수"+int2str(SharedObjects.step_count));

            RequestStep data = new RequestStep(values);
            retroClient.postStepData(data, new RetroCallback() {
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
        else {
            values.put("time", Long.toString(date/1000));
            values.put("value", Float.toString(event.values[0]));
            values.put("frame_num", String.valueOf(frame_number));
            values.put("battery", int2str(getBattery()));
            values.put("sensor_name", sensor.getName());
            values.put("sensor_type", int2str(sensor.getType()));
            values.put("deviceID", SharedObjects.deviceId);


            RequestHR data = new RequestHR(values);
            retroClient.postHRData(data, new RetroCallback() {
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

        frame_number += 1;
        SharedObjects.sensorData.get(sensor.getType()).remove("frame_num");
        SharedObjects.sensorData.get(sensor.getType()).put("frame_num", int2str(frame_number));

    }

    public Integer str2int(String str)
    {
        return Integer.parseInt(str);
    }

    public String int2str(Integer i)
    {
        return Integer.toString(i);
    }

}
