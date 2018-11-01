package com.easygo.tv.module.login;

import com.easygo.tv.bean.TokenResponse;
import com.easygo.tv.http.HttpResult;
import com.easygo.tv.mvp.base.BasePresenter;
import com.easygo.tv.mvp.RequestListener;
import com.easygo.tv.mvp.model.LoginModel;

public class LoginPresenter extends BasePresenter<LoginModel, LoginContract.ILoginView> implements LoginContract.ILoginPresenter {


    public LoginPresenter() {
    }

    //获取登录
    @Override
    public void login() {
        mModel.login(new RequestListener<HttpResult<TokenResponse>>() {

            @Override
            public void onSuccess(HttpResult<TokenResponse> result) {
                mView.loginSucces(result.getResult().getAccessToken());
            }

            @Override
            public void onFailed(String message) {
                mView.loginFailed();
            }
        });
    }


    //根据mac地址获取设备序列号
    @Override
    public void getSerials() {
        mModel.getSerials(new RequestListener() {
            @Override
            public void onSuccess(Object result) {
                mView.serialsSuccess();
            }

            @Override
            public void onFailed(String message) {
                mView.serialsfailed();
            }
        });
    }


}
