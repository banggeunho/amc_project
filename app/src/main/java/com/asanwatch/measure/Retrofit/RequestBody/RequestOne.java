package com.asanwatch.measure.Retrofit.RequestBody;

import java.util.HashMap;

public class RequestOne {
//    @Serial


    public final String value;
    public final String frame_num;
    public final String sensor_name;
    public final String sensor_type;
    public final String deviceID;
    public final String time;

    public RequestOne(HashMap<String, String> params) {

        this.frame_num = params.get("frame_num");
        this.value = params.get("value");
        this.sensor_name = params.get("sensor_name");
        this.sensor_type = params.get("sensor_type");
        this.deviceID = params.get("deviceID");
        this.time = params.get("time");
    }

}

