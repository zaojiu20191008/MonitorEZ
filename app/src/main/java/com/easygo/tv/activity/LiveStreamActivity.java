package com.easygo.tv.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.Toast;

import com.easygo.monitor.R;
import com.easygo.monitor.common.EZOpenConstant;
import com.easygo.monitor.model.EZOpenCameraInfo;
import com.easygo.tv.fragment.EZPlayerFragment;
import com.easygo.tv.message.CMQ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.realm.OrderedRealmCollection;


public class LiveStreamActivity extends AppCompatActivity {

    public static final String TAG = "LiveStreamActivity";

    public static OrderedRealmCollection<EZOpenCameraInfo> data;

    public String[] serials = new String[] {
            "182365469",
            "182364991",
            "182365528"
    };

    private int mPlayingCount = 3;


    public int[] ids = new int[]{
            R.id.fragment_1,
            R.id.fragment_2,
            R.id.fragment_3,
            R.id.fragment_4,

            R.id.fragment_5,
            R.id.fragment_6,
            R.id.fragment_7,
            R.id.fragment_8,

            R.id.fragment_9,
            R.id.fragment_10,
            R.id.fragment_11,
            R.id.fragment_12,

            R.id.fragment_13,
            R.id.fragment_14,
            R.id.fragment_15,
            R.id.fragment_16,

    };
    private ArrayList<EZPlayerFragment> lists;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.live_stream);

        ezPlay();

    }


    private void ezPlay() {

        if(data == null || data.size() == 0)
            return;

        Log.i(TAG, "ezPlay: data.size() - " + data.size());



        lists = new ArrayList<>();
//        int count = ids.length;
        int count = 3;

        for (int i = 0; i < count; i++) {
            EZPlayerFragment EZPlayerFragment = new EZPlayerFragment();


            Bundle bundle = new Bundle();
//            bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, serials[i]);
            bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, data.get(i).getDeviceSerial());
            bundle.putString(EZOpenConstant.EXTRA_CAMERA_NAME, data.get(i).getCameraName());
            bundle.putInt(EZOpenConstant.EXTRA_CAMERA_NO, 1);
            EZPlayerFragment.setArguments(bundle);

            lists.add(EZPlayerFragment);

            mPlaying.put(i, data.get(i).getDeviceSerial());
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < count; i++) {
//            transaction.add(ids[i], lists.get(i), String.valueOf(i));
            transaction.add(ids[i], lists.get(i), data.get(i).getDeviceSerial());
        }
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void startAcceptMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CMQ.getInstance().accept(new CMQ.OnMessageListener() {
                    @Override
                    public void onAccept(final String msg) {
                        if("stop".equals(msg)) {
                            CMQ.getInstance().stop();
                        }

                        if(msg.startsWith("remove")) {
                            String[] split = msg.split("_");
                            remove(split[1]);
                        } else if(msg.startsWith("add")) {
                            final String[] split = msg.split("_");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    add(split[1]);
                                }
                            });
                        }


//                        if("remove_182365469".equals(msg)) {
//                            remove("182365469");
//                        } else if("add_182365469".equals(msg)) {
//                            add("182365469");
//                        }


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LiveStreamActivity.this,  msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.i(TAG, "onAccept: msg --> " + msg);
                    }
                });
                Log.i(TAG, "run: 停止······················");
            }
        }).start();
//        CMQ.getInstance().accept(new CMQ.OnMessageListener() {
//            @Override
//            public void onAccept(String msg) {
//                //接收后台返回的消息
//
//
//                //1.获取摄像头 序列号
//                //2.获取消息类型  （开门直播、录制）
//                //3.根据类型 执行不同方法
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAcceptMessage();
    }

    @Override
    protected void onPause() {
        super.onPause();

        CMQ.getInstance().stop();
    }


//    public ArrayList<Integer> hasPlayingIndexs = new ArrayList<>();
    public int[] hasPlayingIndexs = new int[16];

    public HashMap<Integer, String> mPlaying = new HashMap<>();
//    public SparseArray<String> mPlaying = new SparseArray<>();

    public void add(String deviceSerial) {
        if(TextUtils.isEmpty(deviceSerial)) {
            Toast.makeText(LiveStreamActivity.this, "参数异常： 序列号为空！", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "add: 参数异常： 序列号为空！");
            return;
        }
        Log.i(TAG, "add: 添加 " + deviceSerial);
        EZPlayerFragment EZPlayerFragment = new EZPlayerFragment();

        Bundle bundle = new Bundle();
//            bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, serials[i]);
        bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, deviceSerial);
        bundle.putString(EZOpenConstant.EXTRA_CAMERA_NAME, "门店名称");
        bundle.putInt(EZOpenConstant.EXTRA_CAMERA_NO, 1);
        EZPlayerFragment.setArguments(bundle);

        int index = 0;

        if(mPlaying.size() != 0) {

            Iterator<Map.Entry<Integer, String>> iterator = mPlaying.entrySet().iterator();
            while (iterator.hasNext()) {
                if(iterator.next().getValue() == null) {
                    index = iterator.next().getKey();

                    break;

                }
            }
        }

        Log.i(TAG, "add: 添加位置序号 --> " + index);

        mPlaying.put(index, deviceSerial);

//        for (int i : mPlaying) {
//            if(i == 1)
//                continue;
//
//            //该位置没有视频直播
//            index = i;
//            hasPlayingIndexs[i] = 1;
//            break;
//
//        }


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(ids[index], EZPlayerFragment, deviceSerial).commit();
//        transaction.add(ids[mPlayingCount], EZPlayerFragment, deviceSerial).commit();

        mPlayingCount++;

    }

    public void remove(String deviceSerial) {
        //根据序列号找到 Fragment

        //调用方法 停止录制和播放，释放资源


        EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);
//        fragment.stopRecord();

        if(fragment == null) {
            Log.i(TAG, "remove: 找不到fragment");
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.release();
        transaction.remove(fragment).commit();

        int index = 0;

        if(mPlaying.size() == 0) {
            Log.i(TAG, "remove: 没有正在播放的视频 直接返回");
            return;
        }
        Iterator<Map.Entry<Integer, String>> iterator = mPlaying.entrySet().iterator();
        while (iterator.hasNext()) {
            if(deviceSerial.equals(iterator.next().getValue())) {
                index = iterator.next().getKey();

                break;
            }
        }

        mPlaying.put(index, null);

        mPlayingCount--;

        GridLayout gridLayout = (GridLayout) findViewById(R.id.grid_layout);
        int childCount = gridLayout.getChildCount();
        Log.i(TAG, "remove: childCount --> " + childCount);

        Log.i(TAG, "remove: fragment1 --> " + ((FrameLayout) findViewById(R.id.fragment_1)).getChildCount());
        Log.i(TAG, "remove: fragment2 --> " + ((FrameLayout) findViewById(R.id.fragment_2)).getChildCount());
        Log.i(TAG, "remove: fragment3 --> " + ((FrameLayout) findViewById(R.id.fragment_3)).getChildCount());
        Log.i(TAG, "remove: fragment4 --> " + ((FrameLayout) findViewById(R.id.fragment_4)).getChildCount());

    }

    public void startRecord(String deviceSerial) {
        EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);
        if (fragment == null) {
            Log.i(TAG, "remove: 找不到fragment");
            return;
        }

        fragment.startRecord();

    }



}
