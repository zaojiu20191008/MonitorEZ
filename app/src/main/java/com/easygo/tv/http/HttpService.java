package com.easygo.tv.http;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface HttpService {


    @FormUrlEncoded
    @POST("/api/")
    Call getAccessToken(@Field("accessToken")String accessToken, @Field("deviceSerial")String deviceSerial);


    @FormUrlEncoded
    @POST("/api/")
    Call getCaptureDeviceSerial(@Field("accessToken")String accessToken);
}
