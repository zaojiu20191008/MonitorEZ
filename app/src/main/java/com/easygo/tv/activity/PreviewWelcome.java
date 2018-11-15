package com.easygo.tv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;

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
import com.videogo.openapi.bean.EZDeviceInfo;

import java.util.List;

public class PreviewWelcome extends LoginActivity implements DeviceView, LoginContract.ILoginView {

    private static final String TAG = "PreviewWelcome";
    private DevicePresenter mDevicePresenter;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_welcome;
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
    public void loadFinish(List<EZDeviceInfo> list) {

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
    public void loginFailed(String msg) {
        showToast(msg);

        mHandler.sendEmptyMessageDelayed(MSG_LOGIN_FAILED, 1000);
    }

    @Override
    public void serialsSuccess() {

    }

    @Override
    public void serialsfailed() {

    }

}
