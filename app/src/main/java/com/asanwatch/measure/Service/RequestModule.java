package com.asanwatch.measure.Service;

import android.os.AsyncTask;
import android.util.Log;

import com.asanwatch.measure.MainActivity;
import com.asanwatch.measure.Setting.SharedObjects;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class RequestModule extends AsyncTask<Void, Void, String> {

    private String TAG = "request";

    private URL url = null;
    HttpURLConnection urlConn = null;
    private HashMap<String, String> values;

    public RequestModule(String urlString, HashMap<String, String> values) {
        try{
            this.url = new URL(urlString);
            this.values = values;
        }
        catch (Exception e){
            Log.e(TAG, String.valueOf(e));
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        try{
            String strParams = "";

            for(String key: values.keySet()){
                strParams += key + "=" + values.get(key) + "&";
            }
            strParams = strParams.substring(0, strParams.length() - 1);
//            Log.d(TAG, strParams);
            urlConn = (HttpURLConnection) url.openConnection();


            urlConn.setConnectTimeout(15000);
            urlConn.setReadTimeout(5000);
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setRequestMethod("POST"); // URL 요청에 대한 메소드 설정 : POST.
            urlConn.setRequestProperty("Accept-Charset", "UTF-8"); // Accept-Charset 설정.
            urlConn.setRequestProperty("Context_Type", "application/x-www-form-urlencode");
            urlConn.setRequestProperty("apikey", ""); // ""안에 apikey를 입력

            OutputStream os = urlConn.getOutputStream();
            os.write(strParams.getBytes("UTF-8"));
            os.flush();
            os.close();

            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));
            String line;
            String page = "";
            while ((line = reader.readLine()) != null){
                page += line;
            }


            return page;
        }
        catch(Exception e){
            Log.e(TAG, String.valueOf(e));
        } finally {
            if (urlConn != null)
                urlConn.disconnect();
        }

        return null;
    }



    @Override
    protected void onPostExecute(String s) {
//        super.onPostExecute(s);
        if (SharedObjects.isWake) {
            if (s == null) MainActivity.setServerStatusText(false);
            else MainActivity.setServerStatusText(true);
        }
    }
}
