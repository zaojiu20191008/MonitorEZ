package com.easygo.tv.http;

import com.easygo.tv.bean.TokenResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface HttpService {


    @POST("api/easygo/ysSeven/get_access_token")
    Call<HttpResult<TokenResponse>> getAccessToken();


    @FormUrlEncoded
    @POST("api/")
    Call getCaptureDeviceSerial(@Field("mac")String mac);
}
