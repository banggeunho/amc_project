package com.asanwatch.measure.Retrofit;

import com.asanwatch.measure.Retrofit.RequestBody.RequestDevice;
import com.asanwatch.measure.Retrofit.RequestBody.RequestHR;
import com.asanwatch.measure.Retrofit.RequestBody.RequestOne;
import com.asanwatch.measure.Retrofit.RequestBody.RequestStep;
import com.asanwatch.measure.Retrofit.RequestBody.RequestThree;
import com.asanwatch.measure.Retrofit.ResponseBody.ResponseGet;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetroBaseApiService {

    final String Base_URL = "http://210.102.178.105:7000";

    @GET("/api/watch/{deviceId}")
    Call<ResponseGet> getSettingInfo(@Path("deviceId") String id);

    @POST("/api/watch/receiver")
    Call<Void> postOneData(@Body RequestOne requestOne);

    @POST("/api/watch/receiver")
    Call<Void> postThreeData(@Body RequestThree requestThree);

    @POST("/api/watch/receiver")
    Call<Void> postHRData(@Body RequestHR requestHR);

    @POST("/api/watch/receiver")
    Call<Void> postStepData(@Body RequestStep requestStep);

    @POST("/api/watch/info")
    Call<Void> postDeviceData(@Body RequestDevice requestDevice);
//    Call<Void> postStepData(@FieldMap HashMap<String, String> parameters);

//    @PUT("/")
//    Call<RequestPut> putFirst(@Body RequestPut parameters);

//    @FormUrlEncoded
//    @PATCH("/posts/1")
//    Call<ResponseGet> patchFirst(@Field("title") String title);
//
//    @DELETE("/posts/1")
//    Call<ResponseBody> deleteFirst();
}
