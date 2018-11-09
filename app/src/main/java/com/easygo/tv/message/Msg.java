package com.easygo.tv.message;

import android.util.Log;

import com.easygo.monitor.utils.DataManager;
import com.easygo.tv.message.bean.MsgBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Msg {

    public static final String TAG = "Msg";

    //动作类型
    /**
     * 盘点开始
     */
    public static final String ACTION_BP_START_RECORD = "bp_start_record";
    /**
     * 盘点结束
     */
    public static final String ACTION_BP_STOP_RECORD = "bp_stop_record";


    /**
     * 开始播放
     */
    public static final String ACTION_USER_START_PLAY = "IN";

    /**
     * 停止播放
     */
    public static final String ACTION_USER_STOP_PLAY = "OUT";

    /**
     * 测试消息
     */
    public static final String ACTION_TEST = "test";


    //其他类型
    public static final String KEY_ACTION = "action";
    /**
     * 盘点人员id
     */
    public static final String KEY_USER_ID = "user_id";
    /**
     * 门店id
     */
    public static final String KEY_SHOP_ID = "shop_id";
    /**
     * 门店名称
     */
    public static final String KEY_SHOP_NAME = "shop_name";


    public static void parse(String msg, OnMsgListener listener) {

        try {
            JSONObject jsonObject = new JSONObject(msg);
            String action = jsonObject.optString(KEY_ACTION);
            int user_id = jsonObject.optInt(KEY_USER_ID);
            int shop_id = jsonObject.optInt(KEY_SHOP_ID);
            String shop_name = jsonObject.optString(KEY_SHOP_NAME);

            Log.i(TAG, "parse: action --> " + action);


            String device_serial = null;
            if (ShopMap.sShop.containsKey(shop_id)) {
                device_serial = ShopMap.sShop.get(shop_id);
            } else {
                listener.onError("找不到门店(" + shop_id + ")，检查映射表！");
                return;
            }

            MsgBean msgBean = new MsgBean();
            msgBean.action = action;
            msgBean.user_id = user_id;
            msgBean.shop_id = shop_id;
            msgBean.shop_name = shop_name;
            msgBean.device_serial = device_serial;

            listener.onParseBefore(msgBean, device_serial);
//            listener.onParseBefore(msgBean, shop_name);


            switch (action) {
                case ACTION_BP_START_RECORD:

                    listener.onStartRecord(msgBean);

                    break;
                case ACTION_BP_STOP_RECORD:

                    listener.onStopRecord(msgBean);

                    break;
                case ACTION_USER_START_PLAY:

                    listener.onStartPlay(msgBean);

                    break;
                case ACTION_USER_STOP_PLAY:

                    listener.onStopPlay(msgBean);

                    break;
                case ACTION_TEST:

                    msgBean.width = jsonObject.optInt("width");
                    msgBean.height = jsonObject.optInt("height");

                    listener.onTest(msgBean);

                    break;
                default:

                    break;

            }


        } catch (JSONException e) {
            e.printStackTrace();
            listener.onError("JSON解析错误 --> " + msg);
        }


    }

    public interface OnMsgListener {

        void onParseBefore(MsgBean msgBean, String deviceSerial);

        void onStartRecord(MsgBean msgBean);
        void onStopRecord(MsgBean msgBean);

        void onStartPlay(MsgBean msgBean);
        void onStopPlay(MsgBean msgBean);

        void onError(String msg);
        void onTest(MsgBean msgBean);
    }

    //user_id + shop_name + 时间戳 + 类型（盘点， 用户， 黑名单）
    public static String getTVRecordFilePath(MsgBean msgBean) {

        String shop_name = msgBean.shop_name;
        int user_id = msgBean.user_id;
        String type = "user";
        switch(msgBean.action) {
            case ACTION_BP_START_RECORD:
                type = "stocktaking";
                break;

                default:
                    break;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
        String timeStamp = simpleDateFormat.format(new Date());
        return getTvFilePath()
//                + user_id + "_" + shop_name + "_" + timeStamp + "_" + type + ".mp4";
                + shop_name + "_" + timeStamp + "_" + user_id  + ".mp4";
    }

    public static String getTvFilePath() {
        return DataManager.getInstance().getRecodeFilePath() + "TV/";
    }

    public static boolean isRecordAction(MsgBean msgBean) {
        return ACTION_USER_START_PLAY.equals(msgBean.action);
    }



}
