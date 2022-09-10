package com.asanwatch.measure.Retrofit.RequestBody;

import android.os.BatteryManager;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestDevice {
//    @Serial

    public final ArrayList<String> sensorNames;
    public final ArrayList<Integer> sensorTypes;
    public final String vendor;
    public final String deviceID;


    public RequestDevice(HashMap<String, Object> params){
        this.sensorTypes = (ArrayList<Integer>) params.get("sensorTypes");
        this.sensorNames = (ArrayList<String>) params.get("sensorNames");
        this.vendor = (String) params.get("vendor");
        this.deviceID = (String) params.get("deviceID");
    }

}

