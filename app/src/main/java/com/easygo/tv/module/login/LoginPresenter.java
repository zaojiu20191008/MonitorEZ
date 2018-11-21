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
            public void onFailed(String msg) {
                mView.loginFailed(msg);
            }
        });
    }

}
