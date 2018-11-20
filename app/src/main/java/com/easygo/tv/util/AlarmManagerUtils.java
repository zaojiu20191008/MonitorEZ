package com.easygo.tv.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class AlarmManagerUtils {

    private static final long TIME_INTERVAL = 38400 * 1000;//闹钟执行任务的时间间隔
    private Context context;
    public static AlarmManager am;
    public static PendingIntent pendingIntent;
    //
    public AlarmManagerUtils(Context aContext) {
        this.context = aContext;
    }

    //饿汉式单例设计模式
    private static AlarmManagerUtils instance = null;

    public static AlarmManagerUtils getInstance(Context aContext) {
        if (instance == null) {
            synchronized (AlarmManagerUtils.class) {
                if (instance == null) {
                    instance = new AlarmManagerUtils(aContext);
                }
            }
        }
        return instance;
    }

    public void createAlarmManager(Intent intent) {

        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //Intent intent = new Intent("WakeUp");
        //intent.putExtra("msg", "GetWxAuthInfo");
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);//每隔一定时间发送一次广播
    }

    @SuppressLint("NewApi")
    public void alarmManagerStartWork() {


        //版本适配
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0及以上
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4及以上
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    pendingIntent);
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), TIME_INTERVAL, pendingIntent);
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0及以上
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    getStartTime(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4及以上
            am.setExact(AlarmManager.RTC_WAKEUP, getStartTime(),
                    pendingIntent);
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    getStartTime(), TIME_INTERVAL, pendingIntent);
        }
        //System.out.println("第一次创建，当前时间 = " + System.currentTimeMillis());
    }

    @SuppressLint("NewApi")
    public void alarmManagerWorkOnReceiver() {
        //高版本重复设置闹钟达到低版本中setRepeating相同效果
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0及以上
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + TIME_INTERVAL, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4及以上
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + TIME_INTERVAL, pendingIntent);
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0及以上
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    getStartTime(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4及以上
            am.setExact(AlarmManager.RTC_WAKEUP, getStartTime(), pendingIntent);
        }
    }


    /**
     * 获取指定的开始执行任务的时间毫秒值
     *
     * @return 指定的开始执行任务的时间毫秒值
     */
    public long getStartTime(){
        Calendar mCalendar = Calendar.getInstance();

        //获取当前毫秒值
        long systemTime = System.currentTimeMillis();

        //设置日历的时间，主要是让日历的年月日和当前同步
        mCalendar.setTimeInMillis(systemTime);
        // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        //设置在几点提醒  设置的为0点
        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
        //设置在几分提醒  设置的为0分
        mCalendar.set(Calendar.MINUTE, 0);
        //下面这两个看字面意思也知道
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);

        //获取上面设置的1毫秒值
        long selectTime = mCalendar.getTimeInMillis();

        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if(systemTime > selectTime) {
            mCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        long time = mCalendar.getTimeInMillis();
//        Log.i("recevier","当前时间值 = " + System.currentTimeMillis());
//        Log.i("recevier","指定时间值 = " + time);

        return time;
    }

    private long interval = 10 * 60 * 1000;//触发间隔
//    private long interval = 5 * 1000;//触发间隔

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getNextTime(long interval_in_millisecond) {
        return System.currentTimeMillis() + interval_in_millisecond;
    }

    public void startIntervalTask() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0及以上
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    getNextTime(interval), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4及以上
            am.setExact(AlarmManager.RTC_WAKEUP, getNextTime(interval),
                    pendingIntent);
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    getNextTime(interval), interval, pendingIntent);
        }
    }

    public void intervalTaskOnReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0及以上
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    getNextTime(interval), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4及以上
            am.setExact(AlarmManager.RTC_WAKEUP,  getNextTime(interval), pendingIntent);
        }
    }



}
