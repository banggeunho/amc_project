package com.asanwatch.measure.Retrofit.RequestBody;

import java.util.HashMap;

public class RequestThree {

    public final String z;
    public final String y;
    public final String x;
    public final String frame_num;
    public final String sensor_name;
    public final String sensor_type;
    public final String deviceID;
    public final String time;

    public RequestThree(HashMap<String, String> params) {
        this.z = params.get("z");
        this.y = params.get("y");
        this.x = params.get("x");
        this.frame_num = params.get("frame_num");
        this.sensor_name = params.get("sensor_name");
        this.sensor_type = params.get("sensor_type");
        this.deviceID = params.get("deviceID");
        this.time = params.get("time");
    }

}

