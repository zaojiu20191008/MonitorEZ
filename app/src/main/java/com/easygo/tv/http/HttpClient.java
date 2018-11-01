package com.easygo.tv.http;

import android.content.Context;
import android.util.Log;

import com.easygo.monitor.BuildConfig;
import com.easygo.monitor.main.EZOpenApplication;
import com.easygo.tv.util.TransCodeUtils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpClient {
    private final static String TAG = "HttpClient";
    public OkHttpClient mOkHttpClient;
    private static HttpClient mHttpClient;

    private boolean debug = BuildConfig.DEBUG;

    /**
     * 根据类型生成并获取实例
     */
    public static HttpClient getInstance() {
        if (mHttpClient == null) {
            synchronized (HttpClient.class) {
                if (mHttpClient == null) {
                    mHttpClient =  new HttpClient(EZOpenApplication.mEZOpenApplication.getApplicationContext());
                }
            }
        }
        return mHttpClient;
    }

    public HttpClient(Context context) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        if (debug) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.i(TAG, "log: " + TransCodeUtils.decodeUnicode(message));
                }
            });
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpClientBuilder.addInterceptor(httpLoggingInterceptor);
        }
        mOkHttpClient = okHttpClientBuilder.build();
    }

}


