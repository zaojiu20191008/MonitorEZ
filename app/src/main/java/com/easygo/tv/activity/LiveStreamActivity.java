package com.easygo.tv.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
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
import com.easygo.monitor.main.EZOpenApplication;
import com.easygo.monitor.model.EZOpenCameraInfo;
import com.easygo.monitor.presenter.DevicePresenter;
import com.easygo.monitor.view.DeviceView;
import com.easygo.monitor.view.avctivity.RootActivity;
import com.easygo.tv.Constant;
import com.easygo.tv.fragment.EZPlayerFragment;
import com.easygo.tv.message.CMQ;
import com.easygo.tv.message.Msg;
import com.easygo.tv.message.ShopMap;
import com.easygo.tv.message.bean.MsgBean;
import com.easygo.tv.module.Message.MessageContract;
import com.easygo.tv.module.Message.MessagePresenter;
import com.easygo.tv.module.login.LoginContract;
import com.easygo.tv.module.login.LoginPresenter;
import com.easygo.tv.mvp.model.LoginModel;
import com.easygo.tv.upload.CopyRecord;
import com.easygo.tv.util.AlarmManagerUtils;
import com.easygo.tv.util.NetUtils;
import com.easygo.tv.widget.CommandDialog;
import com.easygo.tv.widget.TipMessageBean;
import com.easygo.tv.widget.TipMessageDialog;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZAlarmInfo;
import com.videogo.openapi.bean.EZDeviceInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.OrderedRealmCollection;


public class LiveStreamActivity extends RootActivity implements MessageContract.IMessageView, LoginContract.ILoginView, DeviceView {

    public static final String TAG = "LiveStreamActivity";

    public static OrderedRealmCollection<EZOpenCameraInfo> data;
    public static List<EZDeviceInfo> list;

    private int mPlayingCount = 0;
    private final int mMaxVisibilityCount = 16;

    //记录屏幕分割参数 （ 1， 2， 3， 4）
    private int mLastSplitCount = 1;

    private ArrayList<EZPlayerFragment> lists;

    private int mScreenWidth;
    private int mScreenHeight;
    private GridLayout mGridLayout;
    private FrameLayout mRootLayout;

    public static final int MSG_STOP_PLAY = 0x0000;
    public static final int MSG_HIDE_FOCUS_FRAME = 0x0001;
    public static final int MSG_GET_ALARM_INFO = 0x0002;
    public static final int MSG_REPLAY = 0x0003;

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
                case MSG_GET_ALARM_INFO:
                    removeMessages(MSG_GET_ALARM_INFO);
                    if(mPlayingCount != 0)
                        mMessagePresenter.getMessage();
                    break;
                case MSG_REPLAY:
                    Bundle data = msg.getData();
                    final MsgBean msgBean = (MsgBean) data.getSerializable(EZPlayerFragment.KEY_MSG_BEAN);
                    final boolean needRecord = data.getBoolean(EZPlayerFragment.KEY_NEED_START_RECORD_AFTER_PLAY);
                    stopPlay(msgBean.device_serial);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startPlay(msgBean, needRecord);
                        }
                    }, 3000);

                    break;
                    default:
                        break;
            }
        }
    };

    public static boolean test = true;

    private CommandDialog mCommandDialog;

    private MessagePresenter mMessagePresenter;
    private LoginPresenter mLoginPresenter;
    private DevicePresenter mDevicePresenter;
    private AlarmManagerUtils getTokenAlarmMgrUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.live_stream);

        initView();
        initScreenSize();
        startCopyTask();
        startGetTokenTask();
        registerNetworkReceiver();

        initPresenter();
    }

    private void initView() {
        mRootLayout = (FrameLayout) findViewById(R.id.frame_layout);
        mGridLayout = (GridLayout) findViewById(R.id.grid_layout);

        if(BuildConfig.BUILD_TYPE.equals("dev")) {
            findViewById(R.id.rl_keyboard).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.rl_keyboard).setVisibility(View.GONE);
        }
    }

    private void initScreenSize() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);

        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }

    private void startCopyTask() {
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

    private void startGetTokenTask() {
        Log.i(TAG, "startGetTokenTask: 开启定时任务");
        getTokenReceiver = new GetTokenReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("get_token");
        registerReceiver(getTokenReceiver, intentFilter);

        //开启定时任务 定时检查tv目录的文件 并传输

        Intent intent = new Intent();
        intent.setAction("get_token");
        getTokenAlarmMgrUtils = new AlarmManagerUtils(this);
        getTokenAlarmMgrUtils.createAlarmManager(intent);
        getTokenAlarmMgrUtils.setInterval(3 * 24 * 60 * 60 * 1000);
//        getTokenAlarmMgrUtils.setInterval(15 * 1000);
        getTokenAlarmMgrUtils.startIntervalTask();
    }

    public void registerNetworkReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        //注册广播接收
        registerReceiver(networkReceiver, filter);

    }

    public void initPresenter() {
        this.mMessagePresenter = new MessagePresenter(this);
        this.mLoginPresenter = new LoginPresenter();
        mLoginPresenter.attach(new LoginModel(), this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mLoginPresenter.detach();
        unregisterReceiver(receiver);
        unregisterReceiver(getTokenReceiver);
        unregisterReceiver(networkReceiver);

        mHandler.removeCallbacksAndMessages(null);

        if (commandDialog().isShowing()) {
            commandDialog().dismiss();
        }
        if (tipMessageDialog().isShowing()) {
            tipMessageDialog().dismiss();
        }
        hideTipDialog();

    }

    public CopyRecordReceiver receiver = new CopyRecordReceiver();
    public GetTokenReceiver getTokenReceiver = new GetTokenReceiver();
    public NetworkReceiver networkReceiver = new NetworkReceiver();

    @Override
    public void loginSucces(String token) {
        Log.i(TAG, "loginSucces: token -- " + token);
        EZOpenSDK.setAccessToken(token);

        if(mDevicePresenter == null)
            mDevicePresenter = new DevicePresenter(this);
        mDevicePresenter.loadDeviceList();
    }

    @Override
    public void loginFailed(String msg) {
        Toast.makeText(EZOpenApplication.mEZOpenApplication, "重新获取登录授权信息...", Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoginPresenter.login();
            }
        }, 1000);
    }

    @Override
    public void loadFinish() {

    }

    @Override
    public void loadFinish(List<EZDeviceInfo> list) {
        data = mDevicePresenter.getEZOpenCameraInfoList();
    }

    @Override
    public void refreshFinish() {

    }

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

    public class GetTokenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //设置token

            Log.i(TAG, "onReceive: get_token 定时任务执行中。。。");

            Toast.makeText(context, "获取登录授权信息中...", Toast.LENGTH_SHORT).show();
            mLoginPresenter.login();

            //高版本重复设置闹钟达到低版本中setRepeating相同效果
            getTokenAlarmMgrUtils.startIntervalTask();
        }
    }

    public Dialog tipDialog;
    public void showTipDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage(msg)
                .setCancelable(true);

        tipDialog = builder.create();
        tipDialog.show();
    }
    public void hideTipDialog() {
        if (tipDialog != null) {
            tipDialog.dismiss();
        }
    }

    public class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                boolean netWorkStateError = NetUtils.isNetWorkError(context);
                // 当网络发生变化，判断当前网络状态，并通过NetEvent回调当前网络状态

                if(netWorkStateError) {
                    showTipDialog("网络异常，请检查网络连接！");
                    Toast.makeText(LiveStreamActivity.this,
                            "网络异常，请检查网络连接！", Toast.LENGTH_LONG).show();
                } else {
                    hideTipDialog();
                    Toast.makeText(LiveStreamActivity.this,
                            "网络连接正常！", Toast.LENGTH_LONG).show();
                }
            }

        }
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

                    }
                });
                Log.i(TAG, "CMQ: 停止接收消息······················");
            }
        }).start();
    }

    private void parseMsg(final String msg) {

        Msg.parse(msg, new Msg.OnMsgListener() {
            @Override
            public void onInterrupt(final MsgBean msgBean, final String deviceSerial) {
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
                            if(deviceSerial.equals(ezOpenCameraInfo.getDeviceSerial())) {

                                if(ezOpenCameraInfo.getStatus() == 2) {
                                    msgBean.interrupt = true;
                                    break;
                                }
//                                msgBean.shop_name = ezOpenCameraInfo.getCameraName();
                                String shopName = ezOpenCameraInfo.getCameraName();
                                String lastChar = shopName.substring(shopName.length() - 1);
                                Pattern pattern = Pattern.compile("[0-9]*");
                                Matcher isNum = pattern.matcher(lastChar);
                                if(isNum.matches()){
                                    msgBean.shop_name = shopName.substring(0, shopName.length()-1);
                                } else {
                                    msgBean.shop_name = shopName;
                                }
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

                        if(mHandler.hasMessages(MSG_STOP_PLAY, msgBean.device_serial)) {
                            //取消延迟 停止播放消息
                            mHandler.removeMessages(MSG_STOP_PLAY, msgBean.device_serial);
                        }
//                        boolean needRecord = false;
//                        if(!TextUtils.isEmpty(msgBean.nick_name)) {
//                            if(Msg.isNeedRecord()) {//晚上10点到早上9点 需要录制
//                                needRecord = true;
//                            }
//                        }
//                        startPlay(msgBean, needRecord);
                        startPlay(msgBean, false);

                    }
                });
            }

            @Override
            public void onStopPlay(final MsgBean msgBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//                        stopPlay(msgBean.device_serial);
                        stopPlayDelayed(msgBean.device_serial);
                    }
                });

            }

            @Override
            public void onPaySuccess(final MsgBean msgBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showTip(msgBean);
                    }
                });
            }

            @Override
            public void onDubious(final MsgBean msgBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean needRecord = false;
                        if(Msg.isNeedRecord()) {//晚上10点到早上9点 需要录制
                            needRecord = true;
                        }
                        startPlay(msgBean, needRecord);
                    }
                });
            }

            @Override
            public void onError(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "parseMsg()# onError(): 错误信息 -- " + msg);
                        if(!"release".equals(BuildConfig.BUILD_TYPE))
                            Toast.makeText(LiveStreamActivity.this, "错误信息 -- " + msg, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onTest(final MsgBean msgBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        StringBuilder sb = new StringBuilder();
                        sb.append("播放数量：").append(mPlayingCount).append("\n");

                        int size = mPlaying.size();

                        sb.append("序列号：").append("\n");
                        for (int i = 0; i < size; i++) {

                            String serial = mPlaying.get(i);
                            sb.append(serial).append(",");
                        }
                        sb.append("\n");

                        sb.append("移动侦测是否开启：").append(isGettingAlarmInfo).append("\n");
                        sb.append("是否接收消息中：").append(CMQ.getInstance().isRepeatAccept()).append("\n");

                        int fragmentSize = mPlayingFragment.size();
                        sb.append("fragment数量：").append(fragmentSize).append("\n");
                        for (int i = 0; i < fragmentSize; i++) {
                            EZPlayerFragment fragment = mPlayingFragment.get(i);
                            sb.append(fragment.getCameraName()).append("-").append(fragment.getStateDescribe()).append(",");
                        }

                        showTipDialog(sb.toString());

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


    public void startPlay(MsgBean msgBean, boolean needRecord) {
        if(msgBean.interrupt) {
            Log.i(TAG, "startPlay: 设备不在线 --> " + msgBean.shop_name);
            return;
        }

        String deviceSerial = msgBean.device_serial;
        int shop_id = msgBean.shop_id;
        String name = msgBean.shop_name;
        if(TextUtils.isEmpty(deviceSerial)) {
            Toast.makeText(LiveStreamActivity.this, "参数异常： 序列号为空！", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "add: 参数异常： 序列号为空！");
            return;
        }

        boolean isDubious = Msg.isDubious(msgBean);
        if(isDubious) {
            //可疑人员
            showTip(msgBean);
        }

        EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);

        if(fragment != null) {
            Log.i(TAG, "add: 已存在 " + deviceSerial);

            if(needRecord) {
                startRecord(msgBean);
            }

            if(isDubious) {
                fragment.highlight();
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
        ezPlayerFragment.setData(msgBean);
        if(isDubious) {
            ezPlayerFragment.highlight();
        }

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


        //如果没有开始获取告警信息，则开始
        if(!isGettingAlarmInfo) {
            mHandler.sendEmptyMessageDelayed(MSG_GET_ALARM_INFO, mStartGetAlarmInfoDelay);
            isGettingAlarmInfo = true;
        }

    }

    @NonNull
    private void setGridLayoutSplit(int splitCount) {
        mGridLayout.setColumnCount(splitCount);
        mGridLayout.setRowCount(splitCount);
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

        if(fragment != null) {
            fragment.release();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(fragment).commit();

        }

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

        if(mPlayingCount == 0) {
            isGettingAlarmInfo = false;
            mHandler.removeMessages(MSG_GET_ALARM_INFO);
        }

        //移除支付成功消息 和 可疑人员消息
        if(mTipMessageDialog != null) {
            if (tipMessageDialog().containMsg(deviceSerial)) {
                //开始 移除消息
                tipMessageDialog().removeMsg(deviceSerial);
            }
        }

    }


    public void startRecord(MsgBean msgBean) {

        Log.i(TAG, "startRecord: ");
        String deviceSerial = msgBean.device_serial;

        EZPlayerFragment fragment = (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);
        if (fragment == null) {
            Log.i(TAG, "startRecord: 找不到fragment");

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
                lp.width = mScreenWidth / splitCount;
                lp.height = mScreenHeight / splitCount;
            }
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
        if(mPlayingCount != 0)
            move(DIRECTION_UP);
    }

    public void left(View view) {
        if(mPlayingCount != 0)
            move(DIRECTION_LEFT);
    }

    public void ok(View view) {
        if(mPlayingCount == 0) {
            return;
        }

        if(isFullScreen()) {//全屏显示时
            cancelFullScreen();
            mHandler.removeMessages(MSG_HIDE_FOCUS_FRAME);
//            return true;
        }
        else if(mFocusIndex != -1 && !commandDialog().isShowing()) {
            commandDialog().show();
            mHandler.removeMessages(MSG_HIDE_FOCUS_FRAME);
//            return true;
        }
    }

    public void right(View view) {
        if(mPlayingCount != 0)
            move(DIRECTION_RIGHT);
    }

    public void down(View view) {
        if(mPlayingCount != 0)
            move(DIRECTION_DOWN);
    }

    public void move(int direction) {
        if(isFullScreen()) {
            Log.i(TAG, "move: 当前为全屏， 直接返回");
            return ;
        }

        int nextFocusIndex = getNextFocusIndex(mFocusIndex, direction);

        if(nextFocusIndex != mFocusIndex) {
            mFocusIndex = nextFocusIndex;
            mPlayingLayout.get(mFocusIndex).requestFocus();
        }
    }


    public View mFocusFrame;
    public View getFocusFrame() {
        if(mFocusFrame == null) {
            mFocusFrame = new View(this);
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
        if(mFocusIndex == -1) {
            Log.i(TAG, "hideFocusFrame: 没有选中任何一项");
            return;
        }
        if(mPlayingCount == 0) {
            Log.i(TAG, "hideFocusFrame: 播放数量为0");
            return;
        }
        mPlayingLayout.get(mFocusIndex).clearFocus();
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
                    if (Constant.CMD.REMOVE.equals(cmdText)) {//移除
                        stopPlayDelayed(mPlaying.get(mFocusIndex));
                    } else if (Constant.CMD.ZOOM_IN.equals(cmdText)) {//全屏

                        if(mPlayingCount == 1) {
                            Toast.makeText(LiveStreamActivity.this, "正在播放的视频数量为 1，不需要全屏显示", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "onCmdSelected: 正在播放的视频数量为 1，不需要全屏显示");
                            return;
                        }
                        showFullScreenVideo();
                    } else if (Constant.CMD.REPLAY.equals(cmdText)) {//重载
                        replay(mPlaying.get(mFocusIndex));
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
                    if(isFullScreen()) {//全屏显示时
                        cancelFullScreen();
                        mHandler.removeMessages(MSG_HIDE_FOCUS_FRAME);
                        return true;
                    }
                    else if(mFocusIndex != -1 && !commandDialog().isShowing()) {
                        commandDialog().show();
                        mHandler.removeMessages(MSG_HIDE_FOCUS_FRAME);
                        return true;
                    }
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if(isFullScreen()) {//全屏显示时
            cancelFullScreen();
            mHandler.removeMessages(MSG_HIDE_FOCUS_FRAME);
            return;
        }
        super.onBackPressed();
    }

    private final String mPrefixTag = "full_screen_";
    private final int mFullScreenLayoutId = 0x1111;
    private int mFullScreenIndex = -1;
    public boolean isFullScreen() {
        return mFullScreenIndex != -1;
    }
    public boolean cancelFullScreen() {
        if(!isFullScreen()) {
            Log.i(TAG, "cancelFullScreen: 没有全屏显示的视频");
            return false;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        EZPlayerFragment fragment = ((EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(mPrefixTag + mPlaying.get(mFullScreenIndex)));
        transaction.remove(fragment).commit();

        mRootLayout.removeViewAt(mRootLayout.getChildCount()-1);
        mFullScreenIndex = -1;
        return true;
    }

    public void showFullScreenVideo() {
        if(mFocusIndex == -1) {
            Log.i(TAG, "showFullScreenVideo: 没有选中任何项进行全屏操作");
            return;
        }

        String deviceSerial = mPlaying.get(mFocusIndex);
        String cameraName = mPlayingFragment.get(mFocusIndex).getCameraName();


        Bundle bundle = new Bundle();
        bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, deviceSerial);
        bundle.putString(EZOpenConstant.EXTRA_CAMERA_NAME, cameraName);
        bundle.putInt(EZOpenConstant.EXTRA_CAMERA_NO, 1);
        bundle.putBoolean(EZOpenConstant.EXTRA_START_RECORD_AFTER_PLAY, false);
        bundle.putInt(EZOpenConstant.EXTRA_INIT_QUALITY, 3);
        final EZPlayerFragment ezPlayerFragment = createFragment(bundle);
        ezPlayerFragment.setSize(mScreenWidth, 0);
        ezPlayerFragment.setZOrderOnTop(true);

        String tag = mPrefixTag + deviceSerial;

        FrameLayout fragmentParent = new FrameLayout(LiveStreamActivity.this);
        fragmentParent.setBackgroundColor(Color.BLACK);
        fragmentParent.setId(mFullScreenLayoutId);
        fragmentParent.setFocusable(true);
        fragmentParent.setFocusableInTouchMode(true);
        fragmentParent.setTag(tag);
        fragmentParent.setOnFocusChangeListener(getOnFocusChangeListener());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2);
        lp.width = mScreenWidth;
        lp.height = mScreenHeight;
        fragmentParent.setLayoutParams(lp);

        mRootLayout.addView(fragmentParent);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(mFullScreenLayoutId, ezPlayerFragment, tag).commit();

        mFullScreenIndex = mFocusIndex;
    }


    private boolean isGettingAlarmInfo = false;//是否正在获取监测告警（移动）
    private final long mStartGetAlarmInfoDelay = 60*1000;//开始获取告警信息延迟
    private final long mGetAlarmInfoInterval = 60*1000;//获取告警信息间隔
//    private final long mStartGetAlarmInfoDelay = 20*1000;//开始获取告警信息延迟
//    private final long mGetAlarmInfoInterval = 15*1000;//获取告警信息间隔

    @Override
    public void messageSuccess(List<EZAlarmInfo> list) {
        if(mPlayingCount == 0 || list == null || list.size() == 0) {
            Log.i(TAG, "messageSuccess: 正在播放视频数量为" +"(" + mPlayingCount + ")，或者过去一分钟没有告警信息");
            if(mPlayingCount != 0) {
                mHandler.sendEmptyMessageDelayed(MSG_GET_ALARM_INFO, mGetAlarmInfoInterval);
            } else {
                //没有侦测到移动 则移除所有播放的视频
                for (int i = 0; i < mPlayingCount; i++) {
                    stopPlay(mPlaying.get(i));
                }
            }

            return;
        }
        Log.i(TAG, "messageSuccess: 监测过去一分钟内是否移动过...");

        for (int i = 0; i < mPlayingCount; i++) {
            EZPlayerFragment fragment = mPlayingFragment.get(i);
            if(!fragment.hasAlreadyPlay(Constant.Alarm.GET_ALARM_INFO_AFTER_TIME)){
                Log.i(TAG, "messageSuccess: 还没播放超过一分钟 --> " + fragment.getCameraName());
                continue;
            }

            String deviceSerial = mPlaying.get(i);

            //找到该门店内所有摄像头 存入needCheck中
            ArrayList<String> needCheck = new ArrayList<>(5);
            needCheck.add(deviceSerial);
            if (ShopMap.sCamera.containsKey(deviceSerial)) {
                String[] cameras = ShopMap.sCamera.get(deviceSerial);
                Collections.addAll(needCheck, cameras);
            }
            int checkCount = needCheck.size();

            boolean needStopPlay = true;
            Log.i(TAG, "messageSuccess: 判断是否侦测到移动 --> " + fragment.getCameraName());
            int size = list.size();
            for (int j = 0; j < size; j++) {
                EZAlarmInfo ezAlarmInfo = list.get(j);
                Log.i(TAG, "messageSuccess: 告警设备名 --> " + ezAlarmInfo.getDeviceName());

                for (int k = 0; k < checkCount; k++) {
                    String serial = needCheck.get(k);
                    if(serial.equals(ezAlarmInfo.getDeviceSerial())) {
                        if (ezAlarmInfo.getAlarmType() != EZOpenConstant.AlarmType.UNKNOWN.getId()) {
                            //监测到报警信息
                            needStopPlay = false;

                            Log.i(TAG, "messageSuccess: 侦测到移动 " + ezAlarmInfo.getDeviceName()
                                    + " -- " + ezAlarmInfo.getAlarmType());
                            break;
                        } else {
                            Log.i(TAG, "messageSuccess: 不知名的告警信息 ");
                        }
                    }
                }


            }
            if(needStopPlay) {
                Log.i(TAG, "messageSuccess: 移除播放 - " + fragment.getCameraName());
                stopPlay(deviceSerial);
            }
        }

        if(mPlayingCount != 0) {
            mHandler.sendEmptyMessageDelayed(MSG_GET_ALARM_INFO, mGetAlarmInfoInterval);
        }

    }

    @Override
    public void onError() {
        Toast.makeText(this, "重新获取移动侦测消息中...", Toast.LENGTH_SHORT).show();
        if(mPlayingCount != 0) {
            mHandler.sendEmptyMessageDelayed(MSG_GET_ALARM_INFO, 1000);
        }
    }

    private TipMessageDialog mTipMessageDialog;
    private TipMessageDialog tipMessageDialog() {
        if(mTipMessageDialog == null) {
            mTipMessageDialog = new TipMessageDialog(this, R.style.TipMessageDialog);

            Window window = mTipMessageDialog.getWindow();
            WindowManager.LayoutParams p = window.getAttributes(); // 获取对话框当前的参数值
            p.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
            window.setAttributes(p);
        }
        mTipMessageDialog.resetFocus();
        return mTipMessageDialog;
    }


    public void showTip(MsgBean msgBean) {

        if(commandDialog().isShowing()) {
            commandDialog().dismiss();
        }
        int type = 0;
        TipMessageBean tipMessageBean = new TipMessageBean();
        tipMessageBean.deviceSerial = msgBean.device_serial;
        if(Msg.ACTION_DUBIOUS.equals(msgBean.action)) {
            //可疑人员
            tipMessageBean.type = TipMessageBean.TYPE_BLACK_LIST;
            tipMessageBean.nick_name = msgBean.nick_name;
        } else {
            tipMessageBean.type = TipMessageBean.TYPE_PAY_SUCCESS;
            tipMessageBean.shop = msgBean.shop_name;
            tipMessageBean.paySuccessCount = msgBean.pay_success_count;
        }

        TipMessageDialog tipMessageDialog = tipMessageDialog();
        tipMessageDialog.addData(tipMessageBean);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tipMessageDialog().resetFocus();
            }
        }, 500);

        tipMessageDialog.show();
    }

    public void replay(String deviceSerial) {

        EZPlayerFragment fragment =  (EZPlayerFragment) getSupportFragmentManager().findFragmentByTag(deviceSerial);

        Bundle bundle = null;
        MsgBean msgBean = null;
        if(fragment != null) {
            fragment.release();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(fragment).commit();
            msgBean = fragment.getData();
            bundle = new Bundle(fragment.getArguments());
        }

        if(msgBean == null || bundle == null) {
            Log.i(TAG, "replay: 数据异常 msgBean - " + msgBean + ", bundle - " + bundle);
            Toast.makeText(this, "数据异常", Toast.LENGTH_SHORT).show();
            return;
        }

        final EZPlayerFragment ezPlayerFragment = createFragment(bundle);
        ezPlayerFragment.setData(msgBean);

        if(bundle.getBoolean(EZPlayerFragment.KEY_NEED_START_RECORD_AFTER_PLAY, false)) {
            String tvRecordFilePath = Msg.getTVRecordFilePath(msgBean);
            Log.i(TAG, "replay: tvRecordFilePath --> " + tvRecordFilePath);
            ezPlayerFragment.setRecordPath(tvRecordFilePath);
        }

        Point size = computeSurfaceSize(mPlayingCount);
        ezPlayerFragment.setSize(size.x, 0);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(msgBean.shop_id, ezPlayerFragment, deviceSerial).commit();
    }


}
