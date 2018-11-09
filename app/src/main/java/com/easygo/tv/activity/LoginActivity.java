package com.easygo.tv.activity;

import android.os.Bundle;

import com.easygo.monitor.R;
import com.easygo.monitor.view.avctivity.RootActivity;
import com.easygo.tv.module.login.LoginContract;
import com.easygo.tv.module.login.LoginPresenter;
import com.easygo.tv.mvp.model.LoginModel;

public class LoginActivity extends RootActivity implements LoginContract.ILoginView {

    private static final String TAG = "LoginActivity";
    private LoginPresenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        loginPresenter = new LoginPresenter();
        loginPresenter.attach(new LoginModel(), this);
        loginPresenter.login();
    }

    protected int getLayoutId() {
        return R.layout.activity_welcome;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginPresenter.detach();
    }

    @Override
    public void loginSucces(String token) {

    }

    @Override
    public void loginFailed(String msg) {

    }

    @Override
    public void serialsSuccess() {

    }

    @Override
    public void serialsfailed() {

    }
}
