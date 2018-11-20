package com.easygo.tv.widget;

public class TipMessageBean {

    public static final int TYPE_PAY_SUCCESS = 0;
    public static final int TYPE_BLACK_LIST = 1;

    public int type;
    public String deviceSerial;
    public int paySuccessCount;
    public String blackListName;

    public TipMessageBean(){}

    public TipMessageBean(int type, int paySuccessCount, String deviceSerial) {
        this.type = type;
        this.paySuccessCount = paySuccessCount;
        this.deviceSerial = deviceSerial;
    }
    public TipMessageBean(int type, String blackListName, String deviceSerial) {
        this.type = type;
        this.blackListName = blackListName;
        this.deviceSerial = deviceSerial;
    }
}
