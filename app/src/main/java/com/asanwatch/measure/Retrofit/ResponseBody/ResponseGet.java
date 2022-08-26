package com.asanwatch.measure.Retrofit.ResponseBody;

import com.google.gson.annotations.SerializedName;

public class ResponseGet {

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("deviceNum")
    private int deviceNum;

    @SerializedName("samplingRate")
    private int samplingRate;

    @SerializedName("bufferSize")
    private int bufferSize;

    @Override
    public String toString() {
        return "Setting Info{" +
                "deviceId=" + deviceId +
                ", deviceNum=" + deviceNum +
                ", samplingRate=" + samplingRate +
                ", bufferSize=" + bufferSize +
                "}";
    }

    public String getDeviceId(){
        return this.deviceId;
    }

    public int getDeviceNum(){
        return this.deviceNum;
    }

    public int getSamplingRate(){
        return this.samplingRate;
    }

    public int getBufferSize(){
        return this.bufferSize;
    }
}
