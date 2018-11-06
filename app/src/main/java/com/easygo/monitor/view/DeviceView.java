package com.easygo.monitor.view;

import com.videogo.openapi.bean.EZDeviceInfo;

import java.util.List;

/**
 * Description:
 * Created by dingwei3
 *
 * @date : 2016/12/9
 */
public interface DeviceView extends BaseView{

    public void loadFinish();
    public void loadFinish(List<EZDeviceInfo> list);


    public void refreshFinish();
}


