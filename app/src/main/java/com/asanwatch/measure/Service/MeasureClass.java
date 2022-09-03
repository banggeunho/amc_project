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
import com.asanwatch.measure.Retrofit.RequestBody.RequestData;
import com.asanwatch.measure.Retrofit.RetroCallback;
import com.asanwatch.measure.Retrofit.RetroClient;
import com.asanwatch.measure.Setting.SharedObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MeasureClass extends Service implements SensorEventListener {
    private static final String TAG = "MeasureForegroundService";
    private long startTime;
    private SensorManager manager;
    private Sensor[] mSensors;
    private RetroClient retroClient;


    @Override
    public void onStart(Intent intent, int startId) {
        startTime = System.currentTimeMillis();
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

        if(sensor.getType() == Sensor.TYPE_STEP_DETECTOR || sensor.getType() == Sensor.TYPE_HEART_RATE)
            notBuffered_data(sensor, event, date);

        else buffered_data(sensor, event, date);

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

    public void setSensorData(){ // 이 부분 수정 sensorType 받는 것도 자동화 해야함
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
            HashMap<String, Object> temp = new HashMap<String, Object>();
            Map.Entry<Integer, Integer> entry = entries.next();
                temp.put("time", new ArrayList<Long>());
                temp.put("value", new ArrayList<Float>());
                temp.put("x", new ArrayList<Float>());
                temp.put("y", new ArrayList<Float>());
                temp.put("z", new ArrayList<Float>());
                temp.put("count", 0);
                temp.put("frame_num", 0);
                SharedObjects.sensorData.put(entry.getKey(), temp);
        }
        Log.d(TAG, SharedObjects.sensorType.toString());
        Log.d(TAG, SharedObjects.sensorData.toString());
    }

    public void buffered_data(Sensor sensor, SensorEvent event, long date)
    {

        boolean is3axis;
        if (event.values.length < 2) is3axis = false;
        else is3axis = true;

        // TIME, VALUE,
//        Log.d(TAG, sensor.getName() + event.values[0]);
        ArrayList<Long> time = (ArrayList) SharedObjects.sensorData.get(sensor.getType()).get("time");
        ArrayList<Float> value = (ArrayList) SharedObjects.sensorData.get(sensor.getType()).get("value");
        ArrayList<Float> x = (ArrayList) SharedObjects.sensorData.get(sensor.getType()).get("x");
        ArrayList<Float> y = (ArrayList) SharedObjects.sensorData.get(sensor.getType()).get("y");
        ArrayList<Float> z = (ArrayList) SharedObjects.sensorData.get(sensor.getType()).get("z");
        Integer count = (Integer) SharedObjects.sensorData.get(sensor.getType()).get("count");
        Integer frame_number = (Integer) SharedObjects.sensorData.get(sensor.getType()).get("frame_num");

//        Log.d(TAG, event.values.length +""+Boolean.toString(is3axis));
//        Log.d(TAG, SharedObjects.sensorData.get(sensor.getType()).toString());

        // 교체를 위한 삭제
        SharedObjects.sensorData.get(sensor.getType()).remove("time");
        SharedObjects.sensorData.get(sensor.getType()).remove("count");
        SharedObjects.sensorData.get(sensor.getType()).remove("value");
        SharedObjects.sensorData.get(sensor.getType()).remove("x");
        SharedObjects.sensorData.get(sensor.getType()).remove("y");
        SharedObjects.sensorData.get(sensor.getType()).remove("z");


        if(count % (SharedObjects.bufferSize+1) == 0 && count != 0) {

            time.add(date);
            if (!is3axis) value.add(event.values[0]);
            else {
                x.add(event.values[0]);
                y.add(event.values[1]);
                z.add(event.values[2]);
            }

            HashMap<String, Object> values = new HashMap<String, Object>();

            values.put("timestamp", time);
            values.put("value", value);
            values.put("x", x);
            values.put("y", y);
            values.put("z", z);
            values.put("frame_num", frame_number);
            values.put("sensor_type", sensor.getType());
            values.put("sensor_name", sensor.getName());
            values.put("startTime", startTime);
            values.put("is3axis", is3axis);
            values.put("deviceID", SharedObjects.deviceId);

//            Log.d(TAG, sensor.getName()+'/'+int2str(count)+'/'+int2str(frame_number));

            RequestData data = new RequestData(values);
            retroClient.postMeasuredData(data, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
//                    ResponseGet data = (ResponseGet) receivedData;
                }

                @Override
                public void onFailure(int code) {
                    Log.d(TAG, "BAD Request: " + code);
                }
            });

            count = 0;
            frame_number += 1;
            SharedObjects.sensorData.get(sensor.getType()).put("time", new ArrayList<Long>());
            SharedObjects.sensorData.get(sensor.getType()).put("value", new ArrayList<Float>());
            SharedObjects.sensorData.get(sensor.getType()).put("x", new ArrayList<Float>());
            SharedObjects.sensorData.get(sensor.getType()).put("z", new ArrayList<Float>());
            SharedObjects.sensorData.get(sensor.getType()).put("y", new ArrayList<Float>());
            SharedObjects.sensorData.get(sensor.getType()).put("count", count);
            SharedObjects.sensorData.get(sensor.getType()).remove("frame_num");
            SharedObjects.sensorData.get(sensor.getType()).put("frame_num", frame_number);

//            if(SharedObjects.isWake){ MainActivity.setServerStatusText(); }

        } else{
            time.add(date);
            if (!is3axis)  value.add(event.values[0]);
            else {
                x.add(event.values[0]);
                y.add(event.values[1]);
                z.add(event.values[2]);
            }

            SharedObjects.sensorData.get(sensor.getType()).put("time", time);
            SharedObjects.sensorData.get(sensor.getType()).put("value", value);
            SharedObjects.sensorData.get(sensor.getType()).put("x", x);
            SharedObjects.sensorData.get(sensor.getType()).put("y", y);
            SharedObjects.sensorData.get(sensor.getType()).put("z", z);

        }
        count += 1;
        SharedObjects.sensorData.get(sensor.getType()).put("count", count);
    }


    public void notBuffered_data(Sensor sensor, SensorEvent event, long date)
    {
        Integer frame_number = str2int(SharedObjects.sensorData.get(sensor.getType()).get("frame_num").toString());

        HashMap<String, Object> values = new HashMap<String, Object>();
        if (sensor.getType() == sensor.TYPE_STEP_DETECTOR) {
            SharedObjects.step_count++;
            values.put("step_count", SharedObjects.step_count);
        }
        values.put("is3axis", false);
        values.put("time", new ArrayList<Long>(Arrays.asList(date)));
        values.put("value", new ArrayList<Float>(Arrays.asList(event.values[0])));
        values.put("frame_num", frame_number);
        values.put("battery", getBattery());
        values.put("sensor_name", sensor.getName());
        values.put("sensor_type", sensor.getType());
        values.put("deviceID", SharedObjects.deviceId);
        values.put("startTime", startTime);


        RequestData data = new RequestData(values);
        retroClient.postMeasuredData(data, new RetroCallback() {
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

        frame_number += 1;
        SharedObjects.sensorData.get(sensor.getType()).remove("frame_num");
        SharedObjects.sensorData.get(sensor.getType()).put("frame_num", frame_number);

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
