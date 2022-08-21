package com.asanwatch.measure.Retrofit.RequestBody;

import java.util.HashMap;

public class RequestHR {
//    @Serial

    public final String battery;
    public final String value;
    public final String frame_num;
    public final String sensor_name;
    public final String sensor_type;
    public final String deviceID;
    public final String time;


    public RequestHR(HashMap<String, String> params){
        this.battery = params.get("battery");
        this.value = params.get("value");
        this.frame_num = params.get("frame_num");
        this.sensor_name = params.get("sensor_name");
        this.sensor_type = params.get("sensor_type");
        this.deviceID = params.get("deviceID");
        this.time = params.get("time");

    }

}

