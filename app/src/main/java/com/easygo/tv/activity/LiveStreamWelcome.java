package com.easygo.tv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.easygo.monitor.R;
import com.easygo.monitor.model.EZOpenCameraInfo;
import com.easygo.monitor.presenter.DevicePresenter;
import com.easygo.monitor.utils.EZLog;
import com.easygo.monitor.view.DeviceView;
import com.easygo.monitor.view.avctivity.RootActivity;
import com.easygo.tv.module.login.LoginPresenter;
import com.easygo.tv.module.login.LoginContract;
import com.easygo.tv.mvp.model.LoginModel;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZDeviceInfo;

import java.util.List;

import io.realm.RealmResults;

public class LiveStreamWelcome extends RootActivity implements DeviceView, LoginContract.ILoginView {

    private static final String TAG = "LiveStreamWelcome";
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


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                CMQ.getInstance().accept(new CMQ.OnMessageListener() {
//                    @Override
//                    public void onAccept(final String msg) {
//                        if("stop".equals(msg)) {
//                            CMQ.getInstance().stop();
//                        }
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(LiveStreamWelcome.this,  msg, Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                        Log.i(TAG, "onAccept: msg --> " + msg);
//                    }
//                });
//                Log.i(TAG, "run: 停止······················");
//            }
//        }).start();


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
    public void loginFailed() {

    }

    @Override
    public void serialsSuccess() {

    }

    @Override
    public void serialsfailed() {

    }

}
