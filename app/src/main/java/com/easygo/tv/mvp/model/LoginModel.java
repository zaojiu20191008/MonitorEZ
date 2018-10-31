package com.easygo.tv.mvp.model;

import com.easygo.tv.mvp.RequestListener;
import com.easygo.tv.module.login.LoginContract;

public class LoginModel implements LoginContract.ILoginModel {

    @Override
    public void login(RequestListener listener) {
        if(listener != null) {
            listener.onSuccess(null);
        }
    }

    @Override
    public void getSerials(RequestListener listener) {

    }
}
