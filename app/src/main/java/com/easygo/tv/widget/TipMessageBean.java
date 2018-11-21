package com.easygo.tv.widget;

public class TipMessageBean {

    public static final int TYPE_PAY_SUCCESS = 0;
    public static final int TYPE_BLACK_LIST = 1;

    public int type;
    public String deviceSerial;
    public String shop;
    public int paySuccessCount;
    public String nick_name;

    public TipMessageBean(){}

    public TipMessageBean(int type, String shop, int paySuccessCount, String deviceSerial) {
        this.type = type;
        this.shop = shop;
        this.paySuccessCount = paySuccessCount;
        this.deviceSerial = deviceSerial;
    }
    public TipMessageBean(int type, String nick_name, String deviceSerial) {
        this.type = type;
        this.nick_name = nick_name;
        this.deviceSerial = deviceSerial;
    }
}
