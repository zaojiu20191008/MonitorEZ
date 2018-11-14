package com.easygo.tv.upload;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.easygo.monitor.utils.DataManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CopyRecord {

    public static final String TAG = CopyRecord.class.getSimpleName();

    public List<String> recordingPath = new ArrayList<>();
    public List<String> copyingPath = new ArrayList<>();
    public List<String> needCopyRecordingPath = new ArrayList<>();

    private String recordDir = DataManager.getInstance().getRecodeFilePath();//录像目录  Records/

    private static ExecutorService cachedThreadPool;

    private SharedPreferences sp;

    private CopyRecord() {
        if(cachedThreadPool == null) {
            cachedThreadPool = Executors.newCachedThreadPool();
        }
    }


    public static CopyRecord getInstance() {

        return Holder.instance;
    }

    private static class Holder {
        private static final CopyRecord instance = new CopyRecord();
    }


    public void copy() {

        //1、获取当前正在录取的视频文件字符串
        File recordFile = new File(recordDir);

        String[] list = recordFile.list();

        //判断是否有文件
        if (list.length == 0) {

            Log.i(TAG, "copy: " + recordFile.getAbsolutePath() + " --> 目录下 没有文件！ 不需要拷贝");
            return;
        }


        Collections.addAll(needCopyRecordingPath, list);

        //2、获取视频目录下 已经录好的视频文件 集合(即需要拷贝的录像视频文件)
        needCopyRecordingPath.removeAll(recordingPath);

        //3、连接目标机器 进行文件传输


    }

    public static void testCopy() {


        String recordDir = DataManager.getInstance().getRecodeFilePath();
        FileTransferClient runnable = new FileTransferClient(recordDir + "20181023/175808388.mp4");

        new Thread(runnable).start();

    }

    public void testCopyInDirectory() {

        String recodeFilePath = DataManager.getInstance().getRecodeFilePath() + "20181026/";
        File file = new File(recodeFilePath);

        if(!file.exists())
            return;


        String[] list = file.list();


        for (String path : list) {
            Log.i(TAG, "testCopyInDirectory: path --> " + path);
            FileTransferClient runnable = new FileTransferClient(recodeFilePath + path);
            cachedThreadPool.execute(runnable);
        }

    }

    public void copy(final Context context, String typeDirectory, final String recordPath) {
        FileTransferClient runnable = new FileTransferClient(typeDirectory, recordPath, new FileTransferClient.TransferListener() {
            @Override
            public void onTransferSuccess() {
                Log.i(TAG, "onTransferSuccess: 清除 拷贝标记 --> " + recordPath);
                getSp(context).edit().remove(recordPath).apply();

                //移除记录：正在拷贝的视频路径
                CopyRecord.getInstance().removeCopyingPath(recordPath);
            }
        });
        cachedThreadPool.execute(runnable);
    }

    public void copy(final Context context, final String recordPath) {
        String type = getSp(context).getString(recordPath, "error");
        FileTransferClient runnable = new FileTransferClient(type, recordPath, new FileTransferClient.TransferListener() {
            @Override
            public void onTransferSuccess() {
                Log.i(TAG, "onTransferSuccess: 清除 拷贝标记 --> " + recordPath);
                getSp(context).edit().remove(recordPath).apply();

                //移除记录：正在拷贝的视频路径
                CopyRecord.getInstance().removeCopyingPath(recordPath);
            }
        });
        cachedThreadPool.execute(runnable);
    }

    public SharedPreferences getSp(Context context) {
        if(sp == null) {
            sp = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sp;
    }

    public void saveRecordPath(Context context, String recordPath) {
        Log.i(TAG, "saveRecordPath: 设置拷贝标记 --> " + recordPath);
        // true 表示需要进行拷贝
        getSp(context).edit()
                .putBoolean(recordPath, true)
                .apply();
    }

    public void saveRecordPath(Context context, String recordPath, String type) {
        Log.i(TAG, "saveRecordPath: 设置拷贝标记 --> " + recordPath);
        // true 表示需要进行拷贝
        getSp(context).edit()
                .putString(recordPath, type)
                .commit();
    }

    public void addRecordingPath (String recordPath) {
        recordingPath.add(recordPath);
    }
    public void removeRecordingPath(String recordPath) {
        recordingPath.remove(recordPath);
    }
    public void addCopyingPath(String recordPath) {
        copyingPath.add(recordPath);
    }
    public void removeCopyingPath(String recordPath) {
        copyingPath.remove(recordPath);
    }

    //判断 该路径文件是否正在录制或者拷贝中
    public boolean isDuringRecordOrCopy(String path) {
        return recordingPath.contains(path) || copyingPath.contains(path);
    }



}
