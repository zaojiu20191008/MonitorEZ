package com.easygo.tv.activity;

import android.content.Intent;
import android.util.Log;

import com.easygo.monitor.R;
import com.easygo.monitor.model.EZOpenCameraInfo;
import com.easygo.monitor.presenter.DevicePresenter;
import com.easygo.monitor.view.DeviceView;
import com.easygo.tv.module.login.LoginContract;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZDeviceInfo;

import java.util.List;

public class LiveStreamWelcome extends LoginActivity implements DeviceView, LoginContract.ILoginView {

    private static final String TAG = "LiveStreamWelcome";
    private DevicePresenter mDevicePresenter;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_welcome;
    }

    @Override
    public void loadFinish() {
//        Intent toIntent = new Intent(this, LiveStreamActivity.class);
//        RealmResults<EZOpenCameraInfo> ezOpenCameraInfoList = mDevicePresenter.getEZOpenCameraInfoList();
//        LiveStreamActivity.data = ezOpenCameraInfoList;
//
//        Log.i(TAG, "loadFinish: data -> " + ezOpenCameraInfoList);
//
//        int size = ezOpenCameraInfoList.size();
//        for (int i = 0; i < size; i++) {
//            EZOpenCameraInfo ezOpenCameraInfo = LiveStreamActivity.data.get(i);
//            Log.i(TAG, "loadFinish: camera: " + ezOpenCameraInfo.getCameraName() + ", " + ezOpenCameraInfo.getDeviceSerial());
//
//        }
//
//        toIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(toIntent);
//        finish();
    }

    @Override
    public void loadFinish(List<EZDeviceInfo> list) {
        Intent toIntent = new Intent(this, LiveStreamActivity.class);
        LiveStreamActivity.list = list;
        LiveStreamActivity.data = mDevicePresenter.getEZOpenCameraInfoList();


//        Log.i(TAG, "loadFinish: list -> " + list);

//        int size = list.size();
        int size = LiveStreamActivity.data.size();
        for (int i = 0; i < size; i++) {
//            EZDeviceInfo ezDeviceInfo = LiveStreamActivity.list.get(i);
//            Log.i(TAG, "loadFinish: camera: " + ezDeviceInfo.getDeviceName() + ", " + ezDeviceInfo.getDeviceSerial());

            EZOpenCameraInfo ezOpenCameraInfo = LiveStreamActivity.data.get(i);
            Log.i(TAG, "loadFinish: camera: " + ezOpenCameraInfo.getCameraName() + ", " + ezOpenCameraInfo.getDeviceSerial());
        }

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
    public void loginFailed(String msg) {
        Log.i(TAG, "loginFailed: " + msg);
        showToast(msg);
    }

    @Override
    public void serialsSuccess() {

    }

    @Override
    public void serialsfailed() {

    }

}
