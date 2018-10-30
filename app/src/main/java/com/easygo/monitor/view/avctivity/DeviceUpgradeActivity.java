package com.easygo.monitor.view.avctivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.easygo.monitor.R;

import com.easygo.monitor.common.EZOpenConstant;
import com.easygo.monitor.presenter.DeviceUpgradePresenter;

public class DeviceUpgradeActivity extends RootActivity {
    private DeviceUpgradePresenter mDeviceSettingPresenter;

    public static void startDeviceUpgradeActivity(Context context,String deviceVersionDes){
        Intent intent = new Intent(context,DeviceUpgradeActivity.class);
        intent.putExtra(EZOpenConstant.EXTRA_DEVICE_VERSION_DES,deviceVersionDes);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_upgrade);
        mDeviceSettingPresenter = new DeviceUpgradePresenter();
        initDate();
        initView();
    }

    private void initView() {

    }

    private void initDate() {

    }

}
