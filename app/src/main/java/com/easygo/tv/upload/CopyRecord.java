package com.easygo.tv.upload;

import android.util.Log;

import com.easygo.monitor.utils.DataManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CopyRecord {

    public static final String TAG = CopyRecord.class.getSimpleName();

    public List<String> recordingPath = new ArrayList<>();
    public List<String> needCopyRecordingPath = new ArrayList<>();

    private String recordDir = DataManager.getInstance().getRecodeFilePath();//录像目录  Records/

    private static ExecutorService cachedThreadPool;

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




}
