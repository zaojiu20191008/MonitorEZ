package com.easygo.tv.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.easygo.monitor.R;
import com.easygo.monitor.common.EZOpenConstant;
import com.easygo.monitor.model.EZOpenCameraInfo;
import com.easygo.tv.fragment.EZPlayerFragment;
import com.easygo.tv.message.CMQ;
import com.easygo.tv.upload.CopyRecord;

import java.lang.reflect.Array;
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

    private int mPlayingCount = 2;
//    private final int mMaxVisibilityCount = 16;
    private final int mMaxVisibilityCount = 2;


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
        int count = 2;

        for (int i = 0; i < count; i++) {
            EZPlayerFragment EZPlayerFragment = new EZPlayerFragment();


            Bundle bundle = new Bundle();
//            bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, serials[i]);
            bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, data.get(i).getDeviceSerial());
            bundle.putString(EZOpenConstant.EXTRA_CAMERA_NAME, data.get(i).getCameraName());
            bundle.putInt(EZOpenConstant.EXTRA_CAMERA_NO, 1);
            EZPlayerFragment.setArguments(bundle);

            lists.add(EZPlayerFragment);

            mPlaying.add(i, data.get(i).getDeviceSerial());
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


    private EZPlayerFragment createFragment(String deviceSerial, int cameraNo, String cameraName) {
        EZPlayerFragment EZPlayerFragment = new EZPlayerFragment();

        Bundle bundle = new Bundle();
        bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, deviceSerial);
        bundle.putString(EZOpenConstant.EXTRA_CAMERA_NAME, cameraName);
        bundle.putInt(EZOpenConstant.EXTRA_CAMERA_NO, cameraNo);
        EZPlayerFragment.setArguments(bundle);

        return EZPlayerFragment;
    }

    public ArrayList<String> mPlaying = new ArrayList<>();


    public void add(String deviceSerial) {
        if(TextUtils.isEmpty(deviceSerial)) {
            Toast.makeText(LiveStreamActivity.this, "参数异常： 序列号为空！", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "add: 参数异常： 序列号为空！");
            return;
        }
        Log.i(TAG, "add: 添加 " + deviceSerial);

        int index = findNeedAddIndex();

        final EZPlayerFragment ezPlayerFragment = createFragment(deviceSerial, 1, "门店名称");

        int containerId;

        if(index < mMaxVisibilityCount) {
            //屏幕上16个未满
            Log.i(TAG, "add: 屏幕未满16个");
            containerId = ids[index];

        }
        else {
            //屏幕上16个已满
            Log.i(TAG, "add: 屏幕已满16个");
            FrameLayout parent = (FrameLayout) findViewById(R.id.frame_layout);

            FrameLayout fragmentParent = new FrameLayout(LiveStreamActivity.this);
            fragmentParent.setBackgroundColor(Color.parseColor("#88000000"));
            fragmentParent.setId(index);
            fragmentParent.setTag(deviceSerial);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2);
            lp.width = getResources().getDimensionPixelSize(R.dimen.tv_player_width);
            lp.height = getResources().getDimensionPixelSize(R.dimen.tv_player_height);
            fragmentParent.setLayoutParams(lp);

            parent.addView(fragmentParent);

            fragmentParent.setVisibility(View.INVISIBLE);

            containerId = index;


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LiveStreamActivity.this, "开始录制···········", Toast.LENGTH_SHORT).show();
                    ezPlayerFragment.startRecord();
                }
            }, 3000);
        }

        Log.i(TAG, "add: 添加位置序号 --> " + index);

        mPlaying.add(index, deviceSerial);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(containerId, ezPlayerFragment, deviceSerial).commit();

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

        int index = findRemoveIndex(deviceSerial);

        if(index >= mMaxVisibilityCount) {
            //移除的是 屏幕外的视频
            FrameLayout parent = (FrameLayout) findViewById(R.id.frame_layout);
            int childCount = parent.getChildCount();
            View needRemove = null;
            for (int i = 0; i < childCount; i++) {
                View view = parent.getChildAt(i);
                String tag = (String) view.getTag();
                if(deviceSerial.equals(tag)) {
                    needRemove = view;
                    Log.i(TAG, "frame_layout中 remove: i --> " + i);
                    break;
                }
            }

            if(needRemove != null) {
                parent.removeView(needRemove);
            }


        }

        Log.i(TAG, "remove: 删除位置序号 --> " + index);

        mPlaying.add(index, null);

        mPlayingCount--;

    }

    private int findNeedAddIndex() {
        int size = mPlaying.size();
        for (int i = 0; i < size; i++) {
            if(mPlaying.get(i) != null) {
                continue;
            }
            return i;
        }
        //遍历完都有画面，找下一个位置
        return size;
    }

    private int findRemoveIndex(String deviceSerial) {
        int size = mPlaying.size();
        for (int i = 0; i < size; i++) {
            if(deviceSerial.equals(mPlaying.get(i))) {
                return i;
            }
        }
        return 0;
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

        String recordPath = fragment.getRecordPath();

        //将需要拷贝的文件路径，存到本地
        CopyRecord.getInstance().saveRecordPath(getApplicationContext(), recordPath);

        //开启线程  拷贝文件
        CopyRecord.getInstance().copy(getApplicationContext(), recordPath);

    }

    public void addInvisibility(String deviceSerial) {
        if(mPlayingCount >= mMaxVisibilityCount) {
            FrameLayout parent = (FrameLayout) findViewById(R.id.frame_layout);

            FrameLayout fragmentParent = new FrameLayout(LiveStreamActivity.this);
            fragmentParent.setId(findNeedAddIndex());
            fragmentParent.setTag(deviceSerial);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) fragmentParent.getLayoutParams();
            lp.width = getResources().getDimensionPixelSize(R.dimen.tv_player_width);
            lp.height = getResources().getDimensionPixelSize(R.dimen.tv_player_height);

            parent.addView(fragmentParent);



            EZPlayerFragment EZPlayerFragment = new EZPlayerFragment();

            Bundle bundle = new Bundle();
            bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, deviceSerial);
            bundle.putString(EZOpenConstant.EXTRA_CAMERA_NAME, "门店名称");
            bundle.putInt(EZOpenConstant.EXTRA_CAMERA_NO, 1);
            EZPlayerFragment.setArguments(bundle);


            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.replace(ids[index], EZPlayerFragment, deviceSerial).commit();

            mPlayingCount++;

        }
    }




}
