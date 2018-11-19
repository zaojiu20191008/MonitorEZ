package com.easygo.tv.module.Message;

import android.util.Log;

import com.easygo.monitor.presenter.BasePresenter;
import com.easygo.monitor.view.MessageView;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZAlarmInfo;

import java.util.Calendar;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Description: 报警消息相关操作
 *
 */
public class MessagePresenter extends BasePresenter{

    public static final String TAG = "MessagePresenter";
    private int pageSize = 50;
    /**
     * 时间区间为起始时间倒推 1分钟
     */
//    private long Interval_Time = 120*1000;
    private long Interval_Time = 90*1000;
    private MessageContract.IMessageView mView;
    private long mEndTime = 0;
    private Calendar mStartCalendar;
    private Calendar mEndCalendar;
    private int index;

    public MessagePresenter(MessageContract.IMessageView baseView){
        mView = baseView;
    }


    public void getMessage(){
        Log.i(TAG, "getMessage: 开始获取告警信息");

        index = 0;
        mEndTime = System.currentTimeMillis();
        mEndCalendar = Calendar.getInstance();
        mEndCalendar.setTimeInMillis(mEndTime);
        mStartCalendar = Calendar.getInstance();
        mStartCalendar.setTimeInMillis(mEndTime-Interval_Time);
        Observable.create(new Observable.OnSubscribe<List<EZAlarmInfo>>() {
            @Override
            public void call(Subscriber<? super List<EZAlarmInfo>> subscriber) {
                try {
                    List<EZAlarmInfo> alarmInfoList = EZOpenSDK.getAlarmList("",index,pageSize,mStartCalendar,mEndCalendar);
                    if (alarmInfoList != null && alarmInfoList.size() == 0){
                        // TODO: 2018/11/16 指定时间内均无报警消息
                    }
                    subscriber.onNext(alarmInfoList);
                } catch (BaseException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Action1<List<EZAlarmInfo>>() {
            @Override
            public void call(List<EZAlarmInfo> list) {
                mView.messageSuccess(list);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                onErrorBaseHandle(((BaseException) throwable).getErrorCode());
                // TODO: 2017/2/15 除公共错误码之后的错误码处理
                mView.onError();
            }
        });
    }


}


