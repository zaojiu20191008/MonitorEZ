package com.easygo.tv.activity;

import android.content.Intent;
import android.os.Bundle;

import com.easygo.monitor.R;
import com.easygo.monitor.presenter.DevicePresenter;
import com.easygo.monitor.test.Constant;
import com.easygo.monitor.utils.EZLog;
import com.easygo.monitor.view.DeviceView;
import com.easygo.monitor.view.avctivity.RootActivity;
import com.easygo.tv.module.login.LoginPresenter;
import com.easygo.tv.module.login.LoginContract;
import com.easygo.tv.mvp.model.LoginModel;
import com.videogo.openapi.EZOpenSDK;

public class PreviewWelcome extends RootActivity implements DeviceView, LoginContract.ILoginView {

    private static final String TAG = "PreviewWelcome";
    private DevicePresenter mDevicePresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        EZLog.i(TAG,"Welcome");

        LoginPresenter loginPresenter = new LoginPresenter();
        loginPresenter.attach(new LoginModel(), this);
        loginPresenter.login();



        //todo  test

//        EZOpenSDK.setAccessToken(Constant.TOKEN);
//
//        mDevicePresenter = new DevicePresenter(this);
//
//        mDevicePresenter.loadDeviceList();

    }


    @Override
    public void loadFinish() {
        Intent toIntent = new Intent(this, PreviewActivity.class);
        PreviewActivity.data = mDevicePresenter.getEZOpenCameraInfoList();
        toIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(toIntent);
        finish();
    }

    @Override
    public void refreshFinish() {

    }

    @Override
    public void loginSucces(String token) {
//        EZOpenSDK.setAccessToken(Constant.TOKEN);
        EZOpenSDK.setAccessToken(token);

        mDevicePresenter = new DevicePresenter(this);

        mDevicePresenter.loadDeviceList();

    }

    @Override
    public void loginFailed() {

    }

    @Override
    public void serialsSuccess() {

    }

    @Override
    public void serialsfailed() {

    }

}
