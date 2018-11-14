package com.easygo.tv.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.Toast;

import com.easygo.monitor.BuildConfig;
import com.easygo.monitor.R;
import com.easygo.monitor.common.EZOpenConstant;
import com.easygo.monitor.model.EZOpenCameraInfo;
import com.easygo.tv.Constant;
import com.easygo.tv.fragment.EZPlayerFragment;
import com.easygo.tv.message.CMQ;
import com.easygo.tv.message.Msg;
import com.easygo.tv.message.bean.MsgBean;
import com.easygo.tv.upload.CopyRecord;
import com.easygo.tv.util.AlarmManagerUtils;
import com.easygo.tv.widget.CommandDialog;
import com.videogo.openapi.bean.EZDeviceInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollection;


public class LiveStreamActivity extends AppCompatActivity {

    public static final String TAG = "LiveStreamActivity";

    public static OrderedRealmCollection<EZOpenCameraInfo> data;
    public static List<EZDeviceInfo> list;

    public String[] serials = new String[] {
            "182365469",
            "182364991",
            "182365528"
    };

    public String[] serial = new String[] {
            "182364991",
            "182365528",
            "182364305",
            "182365547",

            "201104673",
            "201104597",
            "201104630",
            "201104726",

            "201105013",
            "201104994",
            "201105013",
            "201105092",

            "C16407792",
            "C16407994",
            "C16408133",
            "C16958301",
    };
    public String[] name = new String[] {
            "美的海岸花园海星居",
            "美的新海岸",
            "广大商业中心",
            "中山星汇云锦",

            "中山坦洲海伦印象",
            "南沙海景城",
            "海伦堡华景新城",
            "美的高尔夫一店",

            "美的君兰江山",
            "时代廊桥",
            "全球通大厦店",
            "星河湾半岛",

            "白云机场店",
            "海逸锦绣蓝湾",
            "美的御海东郡",
            "怡翠馨",
    };

    private int mPlayingCount = 0;
//    private final int mMaxVisibilityCount = 16;
    private final int mMaxVisibilityCount = 16;

    //记录屏幕分割参数 （ 1， 2， 3， 4）
    private int mLastSplitCount = 1;


    public int[] ids = new int[]{
//            R.id.fragment_1,
//            R.id.fragment_2,
//            R.id.fragment_3,
//            R.id.fragment_4,
//
//            R.id.fragment_5,
//            R.id.fragment_6,
//            R.id.fragment_7,
//            R.id.fragment_8,
//
//            R.id.fragment_9,
//            R.id.fragment_10,
//            R.id.fragment_11,
//            R.id.fragment_12,
//
//            R.id.fragment_13,
//            R.id.fragment_14,
//            R.id.fragment_15,
//            R.id.fragment_16,

    };
    private ArrayList<EZPlayerFragment> lists;

    private int mScreenWidth;
    private int mScreenHeight;
    private GridLayout mGridLayout;
    private FrameLayout mRootLayout;

    public static final int MSG_STOP_PLAY = 0x0000;
    public static final int MSG_HIDE_FOCUS_FRAME = 0x0001;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Object obj = msg.obj;
            switch (what) {
                case MSG_STOP_PLAY:
                    String deviceSerial = (String) obj;
                    stopPlay(deviceSerial);
                    break;
                case MSG_HIDE_FOCUS_FRAME:
                    hideFocusFrame();
                    break;

                    default:
                        break;
            }
        }
    };
    private CommandDialog mCommandDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.live_stream);

        ezPlay();


        mRootLayout = (FrameLayout) findViewById(R.id.frame_layout);
        mGridLayout = (GridLayout) findViewById(R.id.grid_layout);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);

        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;


        Log.i(TAG, "copyRecord: 开启定时任务");
        receiver = new CopyRecordReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("copy_record");
        registerReceiver(receiver, intentFilter);

        //开启定时任务 定时检查tv目录的文件 并传输

        Intent intent = new Intent();
        intent.setAction("copy_record");
        AlarmManagerUtils.getInstance(this).createAlarmManager(intent);
        AlarmManagerUtils.getInstance(this).startIntervalTask();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

        mHandler.removeCallbacksAndMessages(null);

    }

    public CopyRecordReceiver receiver = new CopyRecordReceiver();

    public class CopyRecordReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //检查TV目录中是否有文件需要传输

            Log.i(TAG, "onReceive: copyRecord 定时任务执行中。。。");

            copyRecordInTvDirectory();

            //高版本重复设置闹钟达到低版本中setRepeating相同效果
            AlarmManagerUtils.getInstance(context).startIntervalTask();
        }
    }

    /**
     * 检查TV目录是否有视频 进行拷贝
     */
    public void copyRecordInTvDirectory() {
        String tvFilePath = Msg.getTvFilePath();
        File tvDirectory = new File(tvFilePath);
        if(!tvDirectory.exists()) {
            Log.i(TAG, "copyRecordInTvDirectory: TV目录不存在！");
        } else {
            File[] files = tvDirectory.listFiles();

            if(files.length == 0) {
                Log.i(TAG, "copyRecordInTvDirectory: TV目录中 不存在任何文件");
            } else {
                //有文件时
                ArrayList<File> delete = new ArrayList<>();
                Log.i(TAG, "copyRecordInTvDirectory: TV目录中 存在文件, 开始检测并拷贝");
                for (File file : files) {
                    if (file.getName().endsWith(".mp4")) {
                        //是视频文件
                        String recordPath = file.getAbsolutePath();
                        if(!CopyRecord.getInstance().isDuringRecordOrCopy(recordPath)) {
                            //该路径文件没有正在录制 或者 正在拷贝
                            Log.i(TAG, "copyRecordInTvDirectory: recordPath --> " + recordPath);
//                            CopyRecord.getInstance().copy(getApplicationContext(), "test", recordPath);
                            CopyRecord.getInstance().copy(getApplicationContext(), recordPath);
                        } else {
                            Log.i(TAG, "copyRecordInTvDirectory: 正在录制或者拷贝文件 recordPath --> " + recordPath);
                        }
                    } else {
                        //不是视频文件 直接删除
                        delete.add(file);
                    }
                }

                //删除不相关文件
                int size = delete.size();
                for (int i = 0; i < size; i++) {
                    File file = delete.get(i);
                    Log.i(TAG, "copyRecordInTvDirectory: 删除文件 --> " + file.getName());
                    file.delete();
                }
            }
        }
    }




    private void ezPlay() {

        if(data == null || data.size() == 0)
            return;

        Log.i(TAG, "ezPlay: data.size() - " + data.size());



        lists = new ArrayList<>();
//        int count = ids.length;
        int count = 0;
        mPlayingCount = count;

        for (int i = 0; i < count; i++) {
            EZPlayerFragment EZPlayerFragment = new EZPlayerFragment();


            Bundle bundle = new Bundle();
//            bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, serials[i]);
//            bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, data.get(i).getDeviceSerial());
//            bundle.putString(EZOpenConstant.EXTRA_CAMERA_NAME, data.get(i).getCameraName());
            bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, serial[i]);
            bundle.putString(EZOpenConstant.EXTRA_CAMERA_NAME, name[i]);
            bundle.putInt(EZOpenConstant.EXTRA_CAMERA_NO, 1);
            EZPlayerFragment.setArguments(bundle);

            lists.add(EZPlayerFragment);

//            mPlaying.add(i, data.get(i).getDeviceSerial());
            mPlaying.add(i, serial[i]);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < count; i++) {
//            transaction.add(ids[i], lists.get(i), data.get(i).getDeviceSerial());
            transaction.add(ids[i], lists.get(i), serial[i]);
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
                CMQ.getInstance().reset();
                CMQ.getInstance().accept(new CMQ.OnMessageListener() {
                    @Override
                    public void onAccept(final String msg) {
                        if("stop".equals(msg)) {
                            CMQ.getInstance().stop();
                        }
                        Log.i(TAG, "onAccept: msg --> " + msg);

                        //解析msg
                        parseMsg(msg);

//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(LiveStreamActivity.this,  msg, Toast.LENGTH_SHORT).show();
//                            }
//                        });
                    }
                });
                Log.i(TAG, "CMQ: 停止接收消息······················");
            }
        }).start();
    }

    private void parseMsg(final String msg) {

        Msg.parse(msg, new Msg.OnMsgListener() {
            @Override
            public void onParseBefore(final MsgBean msgBean, final String deviceSerial) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long start = System.currentTimeMillis();
                        int size = data.size();
                        for (int i = 0; i < size; i++) {
                            EZOpenCameraInfo ezOpenCameraInfo = data.get(i);
//                            if(ezOpenCameraInfo.getCameraName().startsWith(deviceSerial)) {
//                                msgBean.device_serial = ezOpenCameraInfo.getDeviceSerial();
//                                Log.i(TAG, "run: deviceSerial --> " + msgBean.device_serial);
//                                break;
//                            }
                            if(deviceSerial.startsWith(ezOpenCameraInfo.getDeviceSerial())) {
                                msgBean.shop_name = ezOpenCameraInfo.getCameraName();
                                Log.i(TAG, "onParseBefore: shop_name --> " + msgBean.shop_name);
                                break;
                            }
                        }

//                        int size = list.size();
//                        for (int i = 0; i < size; i++) {
//                            EZDeviceInfo ezDeviceInfo = list.get(i);
//                            if(ezDeviceInfo.getDeviceName().startsWith(deviceSerial)) {
//                                msgBean.device_serial = ezDeviceInfo.getDeviceSerial();
//                                Log.i(TAG, "run: deviceSerial --> " + msgBean.device_serial);
//                                break;
//                            }
//                        }
                        Log.i("deviceSerial", "run: 遍历搜索 序列号 耗时 --> " + (System.currentTimeMillis()-start));
                    }
                });

            }

            @Override
            public void onStartRecord(final MsgBean msgBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startRecord(msgBean);
                    }
                });
            }

            @Override
            public void onStopRecord(final MsgBean msgBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopRecord(msgBean);
                    }
                });
            }

            @Override
            public void onStartPlay(final MsgBean msgBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        add(msgBean, false);

                        if(mHandler.hasMessages(MSG_STOP_PLAY, msgBean.device_serial)) {
                            //取消延迟 停止播放消息
                            mHandler.removeMessages(MSG_STOP_PLAY, msgBean.device_serial);
                        }
                        startPlay(msgBean, false);
                    }
                });
            }

            @Override
            public void onStopPlay(final MsgBean msgBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        remove(msgBean.device_serial);

//                        stopPlay(msgBean.device_serial);
                        stopPlayDelayed(msgBean.device_serial);
                    }
                });

            }

            @Override
            public void onError(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "parseMsg()# onError(): 错误信息 -- " + msg);
                        Toast.makeText(LiveStreamActivity.this, "错误信息 -- " + msg, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onTest(final MsgBean msgBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String device_serial = msgBean.device_serial;

                        EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(device_serial);
                        if(fragment == null) {
                            Log.i(TAG, "onTest: 找不到fragment - " + device_serial);
                            return;
                        }

                        int width = msgBean.width;
                        int height = msgBean.height;
                        int video_level = msgBean.video_level;

                        fragment.changeQuality(video_level);

//                        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.fragment_1);
//                        GridLayout.LayoutParams lp = (GridLayout.LayoutParams) frameLayout.getLayoutParams();
//                        lp.width = width;
//                        lp.height = height;
//                        frameLayout.setLayoutParams(lp);
//                        fragment.setSurfaceSize(width, height);
                    }
                });

            }
        });
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


    private EZPlayerFragment createFragment(String deviceSerial, int cameraNo, String cameraName, boolean needRecord) {
        EZPlayerFragment EZPlayerFragment = new EZPlayerFragment();

        Bundle bundle = new Bundle();
        bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, deviceSerial);
        bundle.putString(EZOpenConstant.EXTRA_CAMERA_NAME, cameraName);
        bundle.putInt(EZOpenConstant.EXTRA_CAMERA_NO, cameraNo);
        bundle.putBoolean(EZOpenConstant.EXTRA_START_RECORD_AFTER_PLAY, needRecord);
        EZPlayerFragment.setArguments(bundle);

        return EZPlayerFragment;
    }

    private EZPlayerFragment createFragment(Bundle bundle) {
        EZPlayerFragment EZPlayerFragment = new EZPlayerFragment();
        EZPlayerFragment.setArguments(bundle);

        return EZPlayerFragment;
    }

    //记录正在播放的摄像头序列号
    public ArrayList<String> mPlaying = new ArrayList<>();
    //记录正在播放的layout
    public ArrayList<FrameLayout> mPlayingLayout = new ArrayList<>();
    //记录正在播放的fragment
    public ArrayList<EZPlayerFragment> mPlayingFragment = new ArrayList<>();

    public void add(MsgBean msgBean, boolean needRecord) {
        String deviceSerial = msgBean.device_serial;
        String name = msgBean.shop_name;
        if(TextUtils.isEmpty(deviceSerial)) {
            Toast.makeText(LiveStreamActivity.this, "参数异常： 序列号为空！", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "add: 参数异常： 序列号为空！");
            return;
        }

        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(deviceSerial);
        if(fragmentByTag != null) {
            Log.i(TAG, "add: 已存在 " + deviceSerial);
            return;
        }

        Log.i(TAG, "add: 添加 " + deviceSerial);

        int index = findNeedAddIndex();

        //屏幕已满时
        if(index >= mMaxVisibilityCount) {
            if (Msg.isRecordAction(msgBean)) {//如果action是播放相关，则直接返回
                return;
            }

            needRecord = true;
        }

        Log.i(TAG, "add: 需要在播放后开始录制视频 -->" + needRecord);
        final EZPlayerFragment ezPlayerFragment = createFragment(deviceSerial, 1, name, needRecord);

        if(needRecord) {
            String tvRecordFilePath = Msg.getTVRecordFilePath(msgBean);
            Log.i(TAG, "add: tvRecordFilePath --> " + tvRecordFilePath);
            ezPlayerFragment.setRecordPath(tvRecordFilePath);
        }


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


        }

        Log.i(TAG, "add: 添加位置序号 --> " + index);

        mPlaying.add(index, deviceSerial);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(containerId, ezPlayerFragment, deviceSerial).commit();

        mPlayingCount++;

    }

    public void startPlay(MsgBean msgBean, boolean needRecord) {
        String deviceSerial = msgBean.device_serial;
        int shop_id = msgBean.shop_id;
        String name = msgBean.shop_name;
        if(TextUtils.isEmpty(deviceSerial)) {
            Toast.makeText(LiveStreamActivity.this, "参数异常： 序列号为空！", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "add: 参数异常： 序列号为空！");
            return;
        }

        if(mPlaying.contains(deviceSerial)) {
            Log.i(TAG, "add: 已存在 " + deviceSerial);

            if(needRecord) {
                startRecord(msgBean);
            }

            return;
        }

        Log.i(TAG, "startPlay: 添加 " + deviceSerial);


        //屏幕已满时
        if(mPlayingCount+1 > mMaxVisibilityCount) {
            if(Msg.isRecordAction(msgBean)) {
                needRecord = true;
            }
        }

        int quality = getQualityByPlayingCount(mPlayingCount + 1);
        Log.i(TAG, "startPlay: 需要设置的分辨率 -->" + quality);

        Log.i(TAG, "startPlay: 需要在播放后开始录制视频 -->" + needRecord);
        Bundle bundle = new Bundle();
        bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, deviceSerial);
        bundle.putString(EZOpenConstant.EXTRA_CAMERA_NAME, name);
        bundle.putInt(EZOpenConstant.EXTRA_CAMERA_NO, 1);
        bundle.putBoolean(EZOpenConstant.EXTRA_START_RECORD_AFTER_PLAY, needRecord);
        bundle.putInt(EZOpenConstant.EXTRA_INIT_QUALITY, quality);
        final EZPlayerFragment ezPlayerFragment = createFragment(bundle);

        if(needRecord) {
            String tvRecordFilePath = Msg.getTVRecordFilePath(msgBean);
            Log.i(TAG, "add: tvRecordFilePath --> " + tvRecordFilePath);
            ezPlayerFragment.setRecordPath(tvRecordFilePath);
        }

        //获取播放窗口大小
        int splitCount = getSplitCount(mPlayingCount + 1);

        if(mLastSplitCount != splitCount) {
            setGridLayoutSplit(splitCount);
            mLastSplitCount = splitCount;
        }


        Point size = computeSurfaceSize(mPlayingCount + 1);
        setSurfaceSize(splitCount, size.x, 0);



        ezPlayerFragment.setSize(size.x, 0);


        FrameLayout fragmentParent = new FrameLayout(LiveStreamActivity.this);
//        fragmentParent.setBackgroundColor(Color.parseColor("#88000000"));
        fragmentParent.setBackgroundColor(Color.BLACK);
        fragmentParent.setId(shop_id);
        fragmentParent.setFocusable(true);
        fragmentParent.setFocusableInTouchMode(true);
        fragmentParent.setTag(deviceSerial);
        fragmentParent.setOnFocusChangeListener(getOnFocusChangeListener());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2);
        lp.width = size.x;
        lp.height = size.y;
        fragmentParent.setLayoutParams(lp);

        mGridLayout.addView(fragmentParent);

        Log.i(TAG, "add: 添加位置序号 --> " + mPlayingCount);
        mPlaying.add(deviceSerial);
        mPlayingLayout.add(fragmentParent);
        mPlayingFragment.add(ezPlayerFragment);

        if(mPlayingCount + 1 > mMaxVisibilityCount)
            fragmentParent.setVisibility(View.INVISIBLE);

        int containerId;
        containerId = shop_id;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(containerId, ezPlayerFragment, deviceSerial).commit();

        mPlayingCount++;


        Log.i("focus", "焦点  ------ 播放数量： " + mPlayingCount);
        int childCount = mGridLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = mGridLayout.getChildAt(i);
            if(childAt instanceof FrameLayout) {

                FrameLayout frameLayout = (FrameLayout) childAt;

                int nextFocusLeftId = getNextFocusIndex(i, DIRECTION_LEFT);
                int nextFocusUpId = getNextFocusIndex(i, DIRECTION_UP);
                int nextFocusRightId = getNextFocusIndex(i, DIRECTION_RIGHT);
                int nextFocusDownId = getNextFocusIndex(i, DIRECTION_DOWN);


                Log.i("focus", "FrameLayout id --> " + frameLayout.getId());
                Log.i("focus", "nextFocusUpId: " + nextFocusUpId);
                Log.i("focus", "nextFocusDownId: " + nextFocusDownId);
                Log.i("focus", "nextFocusLeftId: " + nextFocusLeftId);
                Log.i("focus", "nextFocusRightId: " + nextFocusRightId);

            }
        }


    }

    @NonNull
    private void setGridLayoutSplit(int splitCount) {
        mGridLayout.setColumnCount(splitCount);
        mGridLayout.setRowCount(splitCount);
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

        mPlaying.add(index, null);//序列号置空
//        mPlaying.remove(index);//移除

        mPlayingCount--;

    }

    private final long stop_play_delay_time = BuildConfig.STOP_PLAY_DELAY_TIME;
    public void stopPlayDelayed(final String deviceSerial) {
        Message msg = Message.obtain();
        msg.what = MSG_STOP_PLAY;
        msg.obj = deviceSerial;
        mHandler.sendMessageDelayed(msg, stop_play_delay_time);
    }

    public void stopPlay(String deviceSerial) {

        EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);

        if(fragment == null) {
            Log.i(TAG, "remove: 找不到fragment");
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.release();
        transaction.remove(fragment).commit();

        int index = -1;

        int size = mPlaying.size();
        for (int i = 0; i < size; i++) {
            if(mPlaying.get(i).equals(deviceSerial)) {
                index = i;
                break;
            }
        }

        if(index == -1) {
            return;
        }


        Log.i(TAG, "remove: 删除位置序号 --> " + index);
        FrameLayout layout = mPlayingLayout.get(index);

        if(index < mMaxVisibilityCount) {
            if (mPlayingCount > mMaxVisibilityCount) {
                //满屏时 前移的一项设置为 可见
                mGridLayout.getChildAt(mMaxVisibilityCount).setVisibility(View.VISIBLE);
            }
        }

        mGridLayout.removeView(layout);


        mPlaying.remove(index);//移除
        mPlayingLayout.remove(index);//移除
        mPlayingFragment.remove(index);//移除

        mPlayingCount--;


        //获取播放窗口大小
        int splitCount = getSplitCount(mPlayingCount);

        Point surfaceSize = computeSurfaceSize(mPlayingCount);
        setSurfaceSize(splitCount, surfaceSize.x, 0);


        if(mLastSplitCount != splitCount) {
            setGridLayoutSplit(splitCount);
            mLastSplitCount = splitCount;
        }

        //动态设置分辨率
        int setVideoLevel = 2;//高清
        if(mPlayingCount == 4) {
            int size1 = mPlayingFragment.size();
            for (int i = 0; i < size1; i++) {
                EZPlayerFragment ezPlayerFragment = mPlayingFragment.get(i);
                ezPlayerFragment.changeQuality(setVideoLevel);
            }
        }

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


    public void startRecord(MsgBean msgBean) {

        Log.i(TAG, "startRecord: ");
        String deviceSerial = msgBean.device_serial;

        EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);
        if (fragment == null) {
            Log.i(TAG, "startRecord: 找不到fragment");

//            add(msgBean, true);
            startPlay(msgBean, true);

            return;
        }

        String tvRecordFilePath = Msg.getTVRecordFilePath(msgBean);
        Log.i(TAG, "startRecord: tvRecordFilePath --> " + tvRecordFilePath);

        fragment.setRecordPath(tvRecordFilePath);

        fragment.startRecord();
    }


    public void stopRecord(MsgBean msgBean) {

        String deviceSerial = msgBean.device_serial;

        EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);
        if (fragment == null) {
            Log.i(TAG, "stopRecord: 找不到fragment");
            return;
        }

        fragment.stopRecord();

        String recordPath = fragment.getRecordPath();
        Log.i(TAG, "stopRecord: 拷贝文件路径 --> " + recordPath);

        String recordType = Msg.getRecordType(msgBean);
        Log.i(TAG, "stopRecord: 拷贝文件类型 --> " + recordType);

        //将需要拷贝的文件路径，存到本地
        CopyRecord.getInstance().saveRecordPath(getApplicationContext(), recordPath, recordType);
        //记录正在拷贝的视频路径
        CopyRecord.getInstance().addCopyingPath(recordPath);

        //开启线程  拷贝文件
//        CopyRecord.getInstance().copy(getApplicationContext(), "test", recordPath);
        CopyRecord.getInstance().copy(getApplicationContext(), recordPath);

    }


    public int getSplitCount(int count) {
        int split;
        if(count == 1) {
            split = 1;
        } else if(count <= 4) {
            split = 2;
        } else if(count <= 9) {
            split = 3;
        } else {
            split = 4;
        }
        return split;
    }
    /**
     * 根据即将播放的数量， 计算视频窗口的大小
     * @param count
     * @return
     */
    public Point computeSurfaceSize(int count) {
        Point size = new Point();
        int split = getSplitCount(count);

        size.set(mScreenWidth / split, mScreenHeight / split);
        return size;
    }

    public void setSurfaceSize(int width, int height) {

        ViewGroup.LayoutParams lp;
        FrameLayout layout;

        int size = mPlayingLayout.size();
        for (int i = 0; i < size; i++) {
            //布局大小
            layout = mPlayingLayout.get(i);
            lp = layout.getLayoutParams();

            if (lp == null) {
                lp = new ViewGroup.LayoutParams(width, height);
            } else {
                lp.width = width;
                lp.height = height;
            }
            if (width == 0) {
                lp.width = (int) (height * 1.1778);
            }
            if (height == 0) {
                lp.height = (int) (width * 0.562);
            }
            layout.setLayoutParams(lp);

            //视频窗口
            String deviceSerial = mPlaying.get(i);
            EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);
            if (fragment == null) {
                Log.i(TAG, "stopRecord: 找不到fragment");
                continue;
            }

            fragment.setSurfaceSize(width, height);
        }

    }

    public void setSurfaceSize(int splitCount, int width, int height) {

        GridLayout.LayoutParams lp;
        FrameLayout layout;

        int size = mPlayingLayout.size();
        for (int i = 0; i < size; i++) {
            //布局大小
            layout = mPlayingLayout.get(i);

            ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();
            if(layoutParams == null || layoutParams instanceof FrameLayout.LayoutParams) {
                lp = new GridLayout.LayoutParams();
            } else {
                lp = (GridLayout.LayoutParams) layout.getLayoutParams();

            }

            if (lp == null) {
                lp = new GridLayout.LayoutParams();
            } else {
//                lp.width = width;
//                lp.height = height;
                lp.width = mScreenWidth / splitCount;
                lp.height = mScreenHeight / splitCount;
            }
//            if (width == 0) {
//                lp.width = (int) (height * 1.1778);
//            }
//            if (height == 0) {
//                lp.height = (int) (width * 0.562);
//            }
            int colIndex = i % splitCount;
            int rowIndex = i / splitCount;
            lp.columnSpec = GridLayout.spec(colIndex,1,GridLayout.FILL,1f);
            lp.rowSpec = GridLayout.spec(rowIndex);
            layout.setLayoutParams(lp);

            //视频窗口
            String deviceSerial = mPlaying.get(i);
            EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);
            if (fragment == null) {
                Log.i(TAG, "stopRecord: 找不到fragment");
                continue;
            }

            fragment.setSurfaceSize(width, height);
        }

        hideFocusFrame();
    }

    public int getQualityByPlayingCount(int playingCount) {
        int quality = 1;

        if(playingCount <= 4) {
            quality = 2;
        }

        return quality;
    }


    private int mFocusIndex = -1;
    private final int DIRECTION_LEFT = 0;
    private final int DIRECTION_UP = 1;
    private final int DIRECTION_RIGHT = 2;
    private final int DIRECTION_DOWN = 3;

    public int getNextFocusIndex(int direction) {
        return getNextFocusIndex(mFocusIndex, direction);
    }

    public int getNextFocusIndex(int curFocusIndex, int direction) {

        if(curFocusIndex == -1) {
            //说明 没有记录焦点
            curFocusIndex = 0;
            return curFocusIndex;
        }

        //当前等分数
        int splitCount = getSplitCount(mPlayingCount);

        int resultIndex = curFocusIndex;
        //找不到时
//        int findNot = -1;
        int findNot = curFocusIndex;//找不到时 返回自身
        switch(direction) {
            case DIRECTION_LEFT:
                resultIndex = curFocusIndex - 1 < 0? findNot: curFocusIndex - 1;
                break;
            case DIRECTION_UP:
                resultIndex = curFocusIndex - splitCount < 0? findNot: curFocusIndex - splitCount;
                break;
            case DIRECTION_RIGHT:
                int childCount1 = mGridLayout.getChildCount();
                resultIndex = curFocusIndex + 1 >= childCount1? findNot: curFocusIndex + 1;
                break;
            case DIRECTION_DOWN:
                int childCount2 = mGridLayout.getChildCount();
                resultIndex = curFocusIndex + splitCount >= childCount2? findNot: curFocusIndex + splitCount;
                break;
        }
        return resultIndex;
    }

    public void up(View view) {
        move(DIRECTION_UP);
    }

    public void left(View view) {
        move(DIRECTION_LEFT);
    }

    public void ok(View view) {
        String content;
        if (mFocusIndex == -1) {
            content = "null";
        } else {
            content = "选中 --> " + mPlayingFragment.get(mFocusIndex).getCameraName();
        }

//        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();

        commandDialog().show();
        mHandler.removeMessages(MSG_HIDE_FOCUS_FRAME);
    }

    public void right(View view) {
        move(DIRECTION_RIGHT);
    }

    public void down(View view) {
        move(DIRECTION_DOWN);
    }

    public int move(int direction) {
        int nextFocusIndex = getNextFocusIndex(mFocusIndex, direction);
        View currentFocus = mGridLayout.getFocusedChild();
        Log.i("focus", "move: currentFocus --> " + (currentFocus != null? currentFocus.getId(): "null"));

//        String cameraName = nextFocusIndex == -1 ?
//                "null"
//                : mPlayingFragment.get(nextFocusIndex).getCameraName();
//        Log.i("focus", "move: " + cameraName);
//        Toast.makeText(this, "移动到： " + cameraName, Toast.LENGTH_SHORT).show();

        if(nextFocusIndex != mFocusIndex) {
            mFocusIndex = nextFocusIndex;
            mPlayingLayout.get(mFocusIndex).requestFocus();
        }
        return nextFocusIndex;
    }


    public View mFocusFrame;
    public View getFocusFrame() {
        if(mFocusFrame == null) {
            mFocusFrame = new View(this);
//            mFocusFrame.setBackgroundResource(R.drawable.selector_focus);
            mFocusFrame.setBackgroundResource(R.drawable.bg_focus_frame);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2);
            mFocusFrame.setLayoutParams(lp);
            mRootLayout.addView(mFocusFrame);
        }
        return mFocusFrame;
    }

    public void adjustFocusFrameSize(View v) {
        FrameLayout frameLayout = (FrameLayout) v;
        int[] outLocation = new int[2];
        frameLayout.getLocationOnScreen(outLocation);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) getFocusFrame().getLayoutParams();
        lp.leftMargin = outLocation[0];
        lp.topMargin = outLocation[1];
        lp.width = frameLayout.getWidth();
        lp.height = frameLayout.getHeight();
        getFocusFrame().setLayoutParams(lp);
    }
    public void hideFocusFrame() {
        mFocusIndex = -1;
        getFocusFrame().setVisibility(View.GONE);
    }
    public void showFocusFrame(View v){
        adjustFocusFrameSize(v);
        getFocusFrame().setVisibility(View.VISIBLE);
    }


    public class OnFocusChangeListener implements View.OnFocusChangeListener{

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus) {
                Log.i("focus", "onFocusChange: " + v.getId());

                showFocusFrame(v);
                //一定时间后取消 焦点选中框
                mHandler.removeMessages(MSG_HIDE_FOCUS_FRAME);
                mHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS_FRAME, 5000);

            } else {

            }
        }
    }

    public OnFocusChangeListener getOnFocusChangeListener() {
        if (mOnFocusChangeListener == null) {
            mOnFocusChangeListener = new OnFocusChangeListener();
        }
        return mOnFocusChangeListener;
    }

    public OnFocusChangeListener mOnFocusChangeListener;


    public Dialog commandDialog() {
        if(mCommandDialog == null) {
            mCommandDialog = new CommandDialog(this, R.style.FocusDialog);
            mCommandDialog.setOnCmdListener(new CommandDialog.OnCmdListener() {
                @Override
                public void onCmdSelected(String cmdText) {
                    mCommandDialog.dismiss();

                    if (mFocusIndex == -1) {
                        Log.i(TAG, "onCmdSelected: 没有选中任何一项");
                        return;
                    }
                    if (Constant.CMD.REMOVE.equals(cmdText)) {
                        stopPlayDelayed(mPlaying.get(mFocusIndex));
                    } else if (Constant.CMD.ZOOM_IN.equals(cmdText)) {
                        Toast.makeText(LiveStreamActivity.this, "放大功能， 开发中。。。", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            mCommandDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mHandler.removeMessages(MSG_HIDE_FOCUS_FRAME);
                    hideFocusFrame();
                }
            });

            Window window = mCommandDialog.getWindow();
            WindowManager.LayoutParams p = window.getAttributes(); // 获取对话框当前的参数值
            p.gravity = Gravity.CENTER;
            window.setAttributes(p);
        }
        mCommandDialog.resetFocus();
        return mCommandDialog;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mPlayingCount != 0) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:

                    move(DIRECTION_LEFT);
                    return true;
                case KeyEvent.KEYCODE_DPAD_UP:

                    move(DIRECTION_UP);
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:

                    move(DIRECTION_RIGHT);
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:

                    move(DIRECTION_DOWN);
                    return true;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:

                    if(mFocusIndex != -1 && !commandDialog().isShowing()) {
                        commandDialog().show();
                        mHandler.removeMessages(MSG_HIDE_FOCUS_FRAME);
                        return true;
                    }
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
