package com.asanwatch.measure.Retrofit;

import com.asanwatch.measure.Retrofit.RequestBody.RequestDevice;
import com.asanwatch.measure.Retrofit.RequestBody.RequestHR;
import com.asanwatch.measure.Retrofit.RequestBody.RequestOne;
import com.asanwatch.measure.Retrofit.RequestBody.RequestStep;
import com.asanwatch.measure.Retrofit.RequestBody.RequestThree;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetroBaseApiService {

    final String Base_URL = "http://172.30.1.60:5002";

//    @GET("/posts/{userId}")
//    Call<ResponseGet> getFirst(@Path("userId") String id);
//
//    @GET("/posts")
//    Call<List<ResponseGet>> getSecond(@Query("userId") String id);
//

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
