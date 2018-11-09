package com.easygo.tv.mvp.model;

import com.easygo.tv.bean.TokenResponse;
import com.easygo.tv.http.HttpResult;
import com.easygo.tv.http.MonitorAPI;
import com.easygo.tv.mvp.RequestListener;
import com.easygo.tv.module.login.LoginContract;


import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginModel implements LoginContract.ILoginModel {

    @Override
    public void login(final RequestListener<HttpResult<TokenResponse>> listener) {


        Call<HttpResult<TokenResponse>> call = MonitorAPI.sAPIService.getAccessToken();


        call.enqueue(new Callback<HttpResult<TokenResponse>>() {
            @Override
            public void onResponse(Call<HttpResult<TokenResponse>> call, Response<HttpResult<TokenResponse>> response) {
                if(response.isSuccessful()) {

                    HttpResult<TokenResponse> token = response.body();
                    if(listener != null) {
                        listener.onSuccess(token);
                    }
                } else {
                    if(listener != null) {
                        listener.onFailed(response.raw().toString());
                    }
                }

            }

            @Override
            public void onFailure(Call<HttpResult<TokenResponse>> call, Throwable t) {
                if(listener != null) {
                    listener.onFailed(t.getMessage());
                }
            }
        });


    }

    @Override
    public void getSerials(RequestListener listener) {

    }
}
