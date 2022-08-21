package com.asanwatch.measure.Retrofit.RequestBody;

import java.util.HashMap;

public class RequestDevice {
//    @Serial

    public final String sensorNames;
    public final String sensorTypes;
    public final String deviceBrand;
    public final String deviceId;


    public RequestDevice(HashMap<String, String> params){
        this.sensorTypes = params.get("sensorTypes");
        this.sensorNames = params.get("sensorNames");
        this.deviceBrand = params.get("deviceBrand");
        this.deviceId = params.get("deviceID");
    }

}

