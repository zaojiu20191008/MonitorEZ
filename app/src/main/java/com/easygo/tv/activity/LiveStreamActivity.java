package com.easygo.tv.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.easygo.monitor.R;
import com.easygo.monitor.common.EZOpenConstant;
import com.easygo.monitor.model.EZOpenCameraInfo;
import com.easygo.tv.fragment.EZPlayerFragment;
import com.easygo.tv.message.CMQ;

import java.util.ArrayList;

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


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

            hasPlaying[i] = data.get(i).getDeviceSerial();
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
                        } else if(msg.startsWith("startRecord")) {
                            final String[] split = msg.split("_");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startRecord(split[1]);
                                }
                            });
                        } else if(msg.startsWith("stopRecord")) {
                            final String[] split = msg.split("_");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stopRecord(split[1]);
                                }
                            });
                        }

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


    public String[] hasPlaying = new String[16];


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

        int length = hasPlaying.length;
        for (int i = 0; i < length; i++) {
            if(hasPlaying[i] != null) {
                continue;
            }
            index = i;
            break;
        }

        Log.i(TAG, "add: 添加位置序号 --> " + index);

        hasPlaying[index] = deviceSerial;


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.add(ids[index], EZPlayerFragment, deviceSerial).commit();
        transaction.replace(ids[index], EZPlayerFragment, deviceSerial).commit();

        mPlayingCount++;

    }

    public void remove(String deviceSerial) {

        EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);

        if(fragment == null) {
            Log.i(TAG, "remove: 找不到fragment");
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.release();
        transaction.remove(fragment).commit();

        int index = 0;

        int length = hasPlaying.length;
        for (int i = 0; i < length; i++) {
            if(deviceSerial.equals(hasPlaying[i])) {
                index = i;
                break;
            }
        }

        Log.i(TAG, "remove: 删除位置序号 --> " + index);

        hasPlaying[index] = null;

        mPlayingCount--;

    }

    public void startRecord(String deviceSerial) {
        EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);
        if (fragment == null) {
            Log.i(TAG, "remove: 找不到fragment");
            return;
        }

        fragment.startRecord();

    }

    public void stopRecord(String deviceSerial) {
        EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);
        if (fragment == null) {
            Log.i(TAG, "remove: 找不到fragment");
            return;
        }

        fragment.stopRecord();

    }



}
