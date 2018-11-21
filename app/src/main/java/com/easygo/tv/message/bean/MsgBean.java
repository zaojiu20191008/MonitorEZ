package com.easygo.tv.message.bean;

import java.io.Serializable;

public class MsgBean implements Serializable {

    public String action;
    public int user_id;
    public int shop_id;
    public String shop_name;
    public String device_serial;

    public String nick_name;
    public int pay_success_count;


    public boolean interrupt;

    //测试用
    public int width;
    public int height;
    public int video_level;
}
