package com.asanwatch.measure.Retrofit;

import android.content.Context;
import android.util.Log;

import com.asanwatch.measure.MainActivity;
import com.asanwatch.measure.Retrofit.RequestBody.RequestData;
import com.asanwatch.measure.Retrofit.RequestBody.RequestDevice;
import com.asanwatch.measure.Retrofit.ResponseBody.ResponseGet;
import com.asanwatch.measure.Setting.SharedObjects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroClient {

    private static final String TAG = "Retrofit Client";
    private RetroBaseApiService apiService;
    public static String baseUrl = SharedObjects.Base_URL;
    private static Context mContext;
    private static Retrofit retrofit;


    private static class SingletonHolder {
        private static RetroClient INSTANCE = new RetroClient(mContext);
    }

    public static RetroClient getInstance(Context context) {
        if (context != null) {
            mContext = context;
        }
        return SingletonHolder.INSTANCE;
    }

    public RetroClient(Context context) {
        baseUrl = SharedObjects.Base_URL;
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build();
    }

    public RetroClient createBaseApi() {
        apiService = create(RetroBaseApiService.class);
        return this;
    }

    /**
     * create you ApiService
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     */
    public <T> T create(final Class<T> service) {
        if (service == null) {
            throw new RuntimeException("Api service is null!");
        }
        return retrofit.create(service);
    }


    public void getSettingInfo(String id, final RetroCallback callback) {
        apiService.getSettingInfo(id).enqueue(new Callback<ResponseGet>() {
            @Override
            public void onResponse(Call<ResponseGet> call, Response<ResponseGet> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
//                    Log.d(TAG, response.body().toString());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseGet> call, Throwable t) {
                if (SharedObjects.isWake) {
                    MainActivity.setServerStatusText(false);
                }
                callback.onError(t);
            }
        });
    }


    public void postDeviceData(RequestDevice parameters, final RetroCallback callback) {
        apiService.postDeviceData(parameters).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (SharedObjects.isWake) {
                        MainActivity.setServerStatusText(true);
                    }
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (SharedObjects.isWake) {
                    MainActivity.setServerStatusText(false);
                }
                callback.onError(t);
            }
        });
    }

    public void postMeasuredData(RequestData parameters, final RetroCallback callback) {
        apiService.postMeasuredData(parameters).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (SharedObjects.isWake) {
                        MainActivity.setServerStatusText(true);
                    }
                    callback.onSuccess(response.code(), response.body());
                } else {
                    if (SharedObjects.isWake) {
                        MainActivity.setServerStatusText(false);
                    }
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (SharedObjects.isWake) {
                    MainActivity.setServerStatusText(false);
                }
                callback.onError(t);
            }
        });
        SharedObjects.frame_num++; // ?????? ?????? ??????
    }


/*    public void putFirst(HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.putFirst(new RequestPut(parameters)).enqueue(new Callback<ResponseGet>() {
            @Override
            public void onResponse(Call<ResponseGet> call, Response<ResponseGet> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseGet> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void patchFirst(String title, final RetroCallback callback) {
        apiService.patchFirst(title).enqueue(new Callback<ResponseGet>() {
            @Override
            public void onResponse(Call<ResponseGet> call, Response<ResponseGet> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseGet> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void deleteFirst(final RetroCallback callback) {
        apiService.deleteFirst().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onError(t);
            }
        });
    }*/
}

