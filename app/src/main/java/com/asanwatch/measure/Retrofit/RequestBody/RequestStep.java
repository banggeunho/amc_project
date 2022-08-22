package com.asanwatch.measure.Retrofit.RequestBody;

import java.util.HashMap;

public class RequestStep {

    public final String value;
    public final String step_count;
    public final String frame_num;
    public final String sensor_name;
    public final String sensor_type;
    public final String deviceID;
    public final String time;

    public RequestStep(HashMap<String, String> params) {
        this.value = params.get("value");
        this.step_count = params.get("step_count");
        this.frame_num = params.get("frame_num");
        this.sensor_name = params.get("sensor_name");
        this.sensor_type = params.get("sensor_type");
        this.deviceID = params.get("deviceID");
        this.time = params.get("time");

    }

}

