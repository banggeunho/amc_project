package com.asanwatch.measure.Retrofit.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestData {

    public final HashMap<String, Object> value = new HashMap<String, Object>();
    public final boolean is3axis;
    public final Integer frameNum;
    public final String sensorName;
    public final Integer sensorType;
    public final String deviceID;
    public final Long startTime;

    // 버퍼에 저장하지 않는 변수
    public final Integer batteryStatus;
    public final Integer stepCount;

    public RequestData(HashMap<String, Object> params) {
        this.is3axis = (boolean) params.get("is3axis");
        this.value.put("timestamp", (ArrayList<Long>) params.get("timestamp"));
        this.value.put("value", (ArrayList<Float>) params.get("value"));
        this.value.put("x", (ArrayList<Float>) params.get("x"));
        this.value.put("y", (ArrayList<Float>) params.get("y"));
        this.value.put("z", (ArrayList<Float>) params.get("z"));
        this.frameNum = (Integer) params.get("frame_num");
        this.sensorName = (String) params.get("sensor_name");
        this.sensorType = (Integer) params.get("sensor_type");
        this.deviceID = (String) params.get("deviceID");
        this.batteryStatus = (Integer) params.get("battery");
        this.stepCount = (Integer) params.get("step_count");
        this.startTime = (Long) params.get("startTime");
    }

}

