package com.easygo.tv.http;

import android.os.Build;

import com.easygo.monitor.BuildConfig;
import com.easygo.monitor.http.EZOpenAPIService;
import com.easygo.monitor.http.EZOpenHttpClient;
import com.easygo.monitor.main.EZOpenApplication;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MonitorAPI {

    private static final String TAG = "MonitorAPI";

    public static HttpService sAPIService;

    public static String API_URL = BuildConfig.API_URL;
    static {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(API_URL).
                addConverterFactory(GsonConverterFactory.create())
                .client(HttpClient.getInstance().mOkHttpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build();
        sAPIService = retrofit.create(HttpService.class);
    }


}


