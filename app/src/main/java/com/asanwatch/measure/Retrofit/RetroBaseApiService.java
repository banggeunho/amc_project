package com.asanwatch.measure.Retrofit;

import com.asanwatch.measure.Retrofit.RequestBody.RequestData;
import com.asanwatch.measure.Retrofit.RequestBody.RequestDevice;
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

    // 센서 데이터를 request하는 함수들을 하나로 통합하는 작업 필요
    // dto 생성 시 구별하는 변수를 하나 포함시켜, 각 데이터마다 어떤 값이 들어가는지 자동적으로 분류해줄 수 있는 조건 작성 필요
    //

    @POST("/api/watch/receiver")
    Call<Void> postMeasuredData(@Body RequestData requestData);

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
