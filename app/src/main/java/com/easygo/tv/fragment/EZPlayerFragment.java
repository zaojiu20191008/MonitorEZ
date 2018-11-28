package com.easygo.tv.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easygo.monitor.R;
import com.easygo.monitor.common.EZOpenConstant;
import com.easygo.monitor.model.EZOpenVideoQualityInfo;
import com.easygo.monitor.presenter.PlayPresenter;
import com.easygo.monitor.utils.DataManager;
import com.easygo.monitor.utils.DateUtil;
import com.easygo.monitor.utils.EZLog;
import com.easygo.monitor.utils.EZOpenUtils;
import com.easygo.monitor.utils.ToastUtls;
import com.easygo.monitor.view.PlayView;
import com.easygo.monitor.view.widget.CommomAlertDialog;
import com.easygo.monitor.view.widget.EZUIPlayerView;
import com.easygo.monitor.view.widget.LoadProgressDialog;
import com.easygo.tv.activity.LiveStreamActivity;
import com.easygo.tv.message.bean.MsgBean;
import com.easygo.tv.upload.CopyRecord;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.OnEZPlayerCallBack;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.RealmList;

public class EZPlayerFragment extends Fragment implements SurfaceHolder.Callback, PlayView {

    private static final String TAG = "EZPlayerFragment";

    private View mMainView;

    public final static int STATUS_INIT = 1;
    public final static int STATUS_START = 2;
    public final static int STATUS_PLAY = 3;
    public final static int STATUS_STOP = 4;

    private long startPlayTime = -1;

    /**
     * 播放器状态
     */
    public int mStatus = STATUS_INIT;
    private boolean isSoundOpen = true;


    private PlayPresenter mPlayPresenter;

    private TextView mNameTextView;
    private int mTextColor = Color.WHITE;
    private LinearLayout mRecordLayout;
    private TextView mRecordTimeTextView;


    private EZUIPlayerView mEZUIPlayerView;
    private EZPlayer mEZPlayer;

    private String mDeviceSerial;
    private int mCameraNo;
    private String mCameraName;
    private boolean mNeedStartRecordAfterPlay = false;

    private String mVerifyCode;

    /**
     * resume时是否恢复播放
     */
    private AtomicBoolean isResumePlay = new AtomicBoolean(true);

    /**
     * surface是否创建好
     */
    private AtomicBoolean isInitSurface = new AtomicBoolean(false);



    private static final int MSG_REFRESH_PLAY_UI = 1000;
    private static final int MSG_WAITING_SET_QUALITY = 6000;
    private static final int MSG_HIDE_TOPBAR = 1001;
    private static final int MSG_SHOW_TOPBAR = 1002;
    private static final int SHOW_TOP_BAR_TIME = 5000;

//    public EZOpenHandler mHandler = new EZOpenHandler(getActivity());
    public EZOpenHandler mHandler = new EZOpenHandler();

    private MsgBean msgBean;//原始消息数据
    public void setData(MsgBean msgBean) {
        this.msgBean = msgBean;
    }

    public void highlight() {
        this.mTextColor = Color.BLACK;
        if(mNameTextView != null) {
            mNameTextView.setTextColor(mTextColor);
        }
    }

    private class EZOpenHandler extends Handler {
        WeakReference<Activity> mActivity;

//        EZOpenHandler(Activity mActivity) {
//            this.mActivity = new WeakReference<Activity>(mActivity);
//        }

        @Override
        public void handleMessage(Message msg) {
//            LiveStreamActivity ower = (LiveStreamActivity) mActivity.get();
//            if (ower == null || ower.isFinishing()) {
//                return;
//            }
            switch (msg.what) {
                case MSG_REFRESH_PLAY_UI:
//                    EZLog.d(TAG, "MSG_REFRESH_PLAY_UI");
                    removeMessages(MSG_REFRESH_PLAY_UI);
//                    ower.updateRateFlow();
                    updateRecordTime();
                    checkRecordTime();
                    sendEmptyMessageDelayed(MSG_REFRESH_PLAY_UI, 1000);
                    break;
//                case MSG_HIDE_TOPBAR:
//                    ower.showOverlayBar(false);
//                    break;
//                case MSG_SHOW_TOPBAR:
//                    ower.showOverlayBar(true);
//                    break;
                case MSG_WAITING_SET_QUALITY:
                    if(mIsSettingQuality) {
                        EZLog.debugLog(TAG, "handleMessage() 等待清晰度设置完成 " + mCameraName);
                        mHandler.sendEmptyMessageDelayed(MSG_WAITING_SET_QUALITY, 500);
                    } else {

                        EZLog.debugLog(TAG, "开始播放： " + mCameraName);
                        startRealPlay();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.live_stream_layout, container, false);

        return mMainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNameTextView = (TextView) mMainView.findViewById(R.id.name);
        mRecordLayout = (LinearLayout) mMainView.findViewById(R.id.record_layout);
        mRecordTimeTextView = (TextView) mMainView.findViewById(R.id.record_text);



        mEZUIPlayerView = (EZUIPlayerView) mMainView.findViewById(R.id.play_view);
        mEZUIPlayerView.setSurfaceHolderCallback(this);

        mPlayPresenter = new PlayPresenter(this);

        Bundle bundle = getArguments();
        mDeviceSerial = bundle.getString(EZOpenConstant.EXTRA_DEVICE_SERIAL);
        mCameraNo = bundle.getInt(EZOpenConstant.EXTRA_CAMERA_NO, -1);
        mCameraName = bundle.getString(EZOpenConstant.EXTRA_CAMERA_NAME);
        mNeedStartRecordAfterPlay = bundle.getBoolean(EZOpenConstant.EXTRA_START_RECORD_AFTER_PLAY);
        mInitVideoLevel = bundle.getInt(EZOpenConstant.EXTRA_INIT_QUALITY);

        //设置名字
        mNameTextView.setTextColor(mTextColor);
        mNameTextView.setText(mCameraName);

        mEZPlayer = EZPlayer.createPlayer(mDeviceSerial, mCameraNo);

        mPlayPresenter.prepareInfo(mDeviceSerial, mCameraNo);

        mEZPlayer.setOnEZPlayerCallBack(new OnEZPlayerCallBack() {
            @Override
            public void onPlaySuccess() {
                EZLog.d(TAG, "onPlaySuccess");
                if (mStatus != STATUS_STOP) {
                    handlePlaySuccess();
                }
            }

            @Override
            public void onPlayFailed(BaseException e) {
                EZLog.d(TAG, "onPlayFailed");
                handlePlayFail(e);
            }

            @Override
            public void onVideoSizeChange(int i, int i1) {
                EZLog.d(TAG, "onVideoSizeChange");
                int mVideoWidth = i;
                int mVideoHeight = i1;
                EZLog.d(TAG, "video width = " + mVideoWidth + "   height = " + mVideoHeight);
                if (mStatus != STATUS_STOP) {
                    mEZUIPlayerView.setVideoSizeChange(mVideoWidth, mVideoHeight);
                }
            }

            @Override
            public void onCompletion() {
                EZLog.d(TAG, "onCompletion");
            }
        });

        if(mWidth == 0 && mHeight == 0) {
            setSurfaceSize();
        } else {
            setSurfaceSize(mWidth, mHeight);
        }

        if(isZOrderOnTop) {
            setSurfaceSizeOnTop();
        }
    }

    /**
     * 处理播放成功的情况
     */
    private void handlePlaySuccess() {
        mStatus = STATUS_PLAY;
        if(startPlayTime == -1) {
            startPlayTime = System.currentTimeMillis();
        }
        refreshPlayStutsUI();
        // 声音处理
        setRealPlaySound();
//        if (mPlayPresenter.getOpenDeviceInfo() != null && mPlayPresenter.getOpenDeviceInfo().supportTalkMode() != 0) {
//            mPlayUI.mTalkImg.setEnabled(true);
//        } else {
//            mPlayUI.mTalkImg.setEnabled(false);
//        }
        mHandler.sendEmptyMessage(MSG_REFRESH_PLAY_UI);
        playFailRetryCount = 0;

        //todo test
        if(mNeedStartRecordAfterPlay) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startRecord();
                }
            }, 2000);
        }
    }

    private int playFailRetryCount = 0;
    public static final String KEY_MSG_BEAN = "key_msg_bean";
    public static final String KEY_DEVICE_SERIAL = "key_device_serial";
    public static final String KEY_NEED_START_RECORD_AFTER_PLAY = "key_need_start_record_after_play";
    /**
     * 处理播放失败的情况
     */
    private void handlePlayFail(BaseException e) {
        if(playFailRetryCount == 2) {
            if (mStatus != STATUS_STOP) {
                mStatus = STATUS_STOP;
                mEZUIPlayerView.dismissomLoading();
                stopRealPlay();

                updateRealPlayFailUI(e.getErrorCode());

            }

            Message msg = Message.obtain();
            Bundle data = new Bundle();
            data.putSerializable(KEY_MSG_BEAN, msgBean);
            data.putBoolean(KEY_NEED_START_RECORD_AFTER_PLAY, mNeedStartRecordAfterPlay);
            msg.what = LiveStreamActivity.MSG_REPLAY;
            msg.setData(data);
            ((LiveStreamActivity) getActivity()).mHandler.sendMessage(msg);
            return;
        }
        if (mStatus != STATUS_STOP) {
            mStatus = STATUS_STOP;
            mEZUIPlayerView.dismissomLoading();
            stopRealPlay();

            updateRealPlayFailUI(e.getErrorCode());
            showToast(mCameraName + ": 重试中...");
            startRealPlay();
            playFailRetryCount++;
        }
    }

    private void updateRealPlayFailUI(int errorCode) {
        String txt = null;
        Log.i(TAG, "updateRealPlayFailUI: errorCode:" + errorCode);
        // 判断返回的错误码
        switch (errorCode) {
            case ErrorCode.ERROR_TRANSF_ACCESSTOKEN_ERROR:
//                EZOpenUtils.gotoLogin();
                return;
            case ErrorCode.ERROR_CAS_MSG_PU_NO_RESOURCE:
                txt = getString(R.string.remoteplayback_over_link);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_OFFLINE:
                txt = getString(R.string.realplay_fail_device_not_exist);
                break;
            case ErrorCode.ERROR_INNER_STREAM_TIMEOUT:
                txt = getString(R.string.realplay_fail_connect_device);
                break;
            case ErrorCode.ERROR_WEB_CODE_ERROR:
                break;
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_OP_ERROR:
                break;
            case ErrorCode.ERROR_TRANSF_TERMINAL_BINDING:
                txt = "请在萤石客户端关闭终端绑定";
                break;
            // 收到这两个错误码，可以弹出对话框，让用户输入密码后，重新取流预览
            case ErrorCode.ERROR_INNER_VERIFYCODE_NEED:
            case ErrorCode.ERROR_INNER_VERIFYCODE_ERROR: {
//                CommomAlertDialog.VerifyCodeInputDialog(PlayActivity.this, new CommomAlertDialog.VerifyCodeInputListener() {
//                    @Override
//                    public void onInputVerifyCode(String verifyCode) {
//                        if (!TextUtils.isEmpty(verifyCode)) {
//                            mVerifyCode = verifyCode;
//                            mPlayPresenter.setDeviceEncrypt(mDeviceSerial, mVerifyCode);
//                            realStartPlay(mVerifyCode);
//                        }
//                    }
//                }).show();
            }
            break;
            case ErrorCode.ERROR_EXTRA_SQUARE_NO_SHARING:
            default:
                txt = getErrorTip(R.string.realplay_play_fail, errorCode);
                break;
        }

        if(errorTxts == null)
            errorTxts = new ArrayList<>();
        errorTxts.add(txt);

        if (!TextUtils.isEmpty(txt)) {
            setRealPlayFailUI(txt);
        } else {
            setRealPlayStopUI();
        }
    }

    private void setRealPlayFailUI(String txt) {
        mEZUIPlayerView.showTipText(txt);
    }

    private void setRealPlayStopUI() {

    }
    public int getErrorId(int errorCode) {
        int errorId = this.getResources().getIdentifier("error_code_" + errorCode, "string", getContext().getPackageName());
        return errorId;
    }
    public String getErrorTip(int id, int errCode) {
        StringBuffer errorTip = new StringBuffer();
        if (errCode != 0) {
            int errorId = getErrorId(errCode);
            if (errorId != 0) {
                errorTip.append(getString(errorId));
            } else {
                errorTip.append(getString(id)).append(" (").append(errCode).append(")");
            }
        } else {
            errorTip.append(getString(id));
        }
        return errorTip.toString();
    }

    /**
     * 更新播放状态显示UI
     */
    private void refreshPlayStutsUI() {
        switch (mStatus) {
            case STATUS_PLAY:
                mEZUIPlayerView.dismissomLoading();
//                mPlayUI.mPlayImg.setImageResource(R.drawable.btn_stop_n);
//                mPlayUI.mRecordImg.setEnabled(true);
//                mPlayUI.mPictureImg.setEnabled(true);
                break;
            case STATUS_STOP:
                mEZUIPlayerView.dismissomLoading();
//                mPlayUI.mPlayImg.setImageResource(R.drawable.btn_play_n);
//                mPlayUI.mRecordImg.setEnabled(false);
//                mPlayUI.mPictureImg.setEnabled(false);
                break;
            default:
                break;
        }
    }

    /**
     * 设置播放是否开启音频
     */
    private void setRealPlaySound() {
        if (mEZPlayer != null) {
            if (isSoundOpen) {
                mEZPlayer.openSound();
            } else {
                mEZPlayer.closeSound();
            }
        }
    }

    public void release() {
        stopRecord();
        stopRealPlay();

        mHandler.removeCallbacksAndMessages(null);
        if (mEZPlayer != null) {
            mEZPlayer.release();
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

//        mPlayPresenter.release();


        mHandler.removeCallbacksAndMessages(null);
        if (mEZPlayer != null) {
            mEZPlayer.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mOffline) {//离线
            return;
        }

        if (isResumePlay.get() && isInitSurface.get()) {
            isResumePlay.set(false);
            EZLog.d(TAG, "onResume   isInitSurface = " + isInitSurface);
            if(mIsSettingQuality) {
                EZLog.d(TAG, "onResume   等待清晰度设置完成： " + mCameraName);
                mHandler.sendEmptyMessageDelayed(MSG_WAITING_SET_QUALITY, 500);
            } else {
                startRealPlay();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

//        Log.i(TAG, "onPause: 停止录制播放");

        if (mStatus != STATUS_STOP) {
            isResumePlay.set(true);
        }
        stopRecord();
        stopRealPlay();

//        Log.i(TAG, "onPause: 停止后： " +  parentFile.getAbsolutePath() + " --> 文件数量 " + parentFile.list().length);
    }

    /**
     * 开始播放
     */
    private void startRealPlay() {
        Log.d(TAG, "startRealPlay  mStatus = " + mStatus);
        if (mStatus == STATUS_START || mStatus == STATUS_PLAY) {
            return;
        }
        //检测网络
        if (!EZOpenUtils.isNetworkAvailable(this.getContext())) {
            showToast("请检查网络连接...");
            return;
        }
        //设备信息为空
        if (mPlayPresenter.getOpenDeviceInfo() == null || mPlayPresenter.getOpenCameraInfo() == null) {
            return;
        }
        if (mPlayPresenter.getOpenDeviceInfo().getIsEncrypt() == 0) {
            realStartPlay(null);
        } else {
            String verifyCode = mPlayPresenter.getDeviceEncrypt();
            if (!TextUtils.isEmpty(verifyCode)) {
                realStartPlay(verifyCode);
            } else {
                CommomAlertDialog.VerifyCodeInputDialog(getActivity(), new CommomAlertDialog.VerifyCodeInputListener() {
                    @Override
                    public void onInputVerifyCode(String verifyCode) {
                        if (!TextUtils.isEmpty(verifyCode)) {
                            mVerifyCode = verifyCode;
                            mPlayPresenter.setDeviceEncrypt(mDeviceSerial, mVerifyCode);
                            realStartPlay(mVerifyCode);
                        }
                    }
                }).show();
                showToast("设备密码...");
            }
        }
    }

    /**
     * 停止播放
     */
    public void stopRealPlay() {
        EZLog.d(TAG, "stopRealPlay");
        stopRealPlayUI();
        if (mEZPlayer != null) {
            mEZPlayer.stopRealPlay();
        }
    }

    /**
     * 停止播放UI
     */
    private void stopRealPlayUI() {
        Log.d(TAG, "stopRealPlayUI");
        mHandler.removeMessages(MSG_REFRESH_PLAY_UI);
//        mRateTextView.setText(String.format(getResources().getString(R.string.string_rate), "0.0 k/s"));
        mStatus = STATUS_STOP;
        refreshPlayStutsUI();
    }

    private void startRealPlayUI() {
        mEZUIPlayerView.showLoading();
//        mPlayUI.mPlayImg.setImageResource(R.drawable.btn_stop_n);
    }

    private void realStartPlay(String mVerifyCode) {
        if (!TextUtils.isEmpty(mVerifyCode)) {
            mEZPlayer.setPlayVerifyCode(mVerifyCode);
        }
        mStatus = STATUS_START;
        startRealPlayUI();
        mEZPlayer.startRealPlay();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(holder);
        }

        if(mOffline)//离线
            return;

        if (isInitSurface.compareAndSet(false,true) && isResumePlay.get()) {
            isResumePlay.set(false);

            if(mIsSettingQuality) {
                EZLog.d(TAG, "surfaceCreated   等待清晰度设置完成： " + mCameraName);
                mHandler.sendEmptyMessageDelayed(MSG_WAITING_SET_QUALITY, 500);
            } else {
                startRealPlay();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        EZLog.d(TAG, "surfaceDestroyed --> " + mCameraName);
        isInitSurface.set(false);
    }

    @Override
    public void handleEZOpenDeviceInfo() {

    }

    @Override
    public void handleEZOpenCameraInfo() {
    }

    @Override
    public void handlePrepareInfo() {
        refreshUI();
    }

    @Override
    public void handleSetQualitSuccess() {
        Log.i("test", "handleSetQualitSuccess: ");
        isSettingQuality.set(false);
        mIsSettingQuality = false;

        mVideoLevel = mSettingVideoLevel;

        reStartPlay();

        if(isInitSurface.get() && !isResumePlay.get())
            mHandler.sendEmptyMessageDelayed(MSG_WAITING_SET_QUALITY, 500);
    }

    @Override
    public void handleSetQualitFailed() {
        Log.i("test", "handleSetQualitFailed: 设置清晰度 失败！！！ --" + mCameraName);
        mSettingVideoLevel = mVideoLevel;
        isSettingQuality.set(false);
        mIsSettingQuality = false;

        if(isInitSurface.get() && !isResumePlay.get())
            mHandler.sendEmptyMessageDelayed(MSG_WAITING_SET_QUALITY, 500);
    }

    /**
     * 重启播放
     */
    private void reStartPlay() {
        if (mStatus == STATUS_PLAY) {
            // 停止播放
            stopRealPlay();
            //下面语句防止stopRealPlay线程还没释放surface, startRealPlay线程已经开始使用surface
            //因此需要等待500ms
//            SystemClock.sleep(500);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 开始播放
                    startRealPlay();
                }
            }, 500);
            // 开始播放
//            startRealPlay();
        }
    }


    private LoadProgressDialog mLoadProgressDialog;

    @Override
    public void showLoadDialog() {
//        if (mLoadProgressDialog == null){
//            mLoadProgressDialog = new LoadProgressDialog(getContext());
//            mLoadProgressDialog.setCancelable(false);
//            mLoadProgressDialog.setCanceledOnTouchOutside(false);
//        }
//        mLoadProgressDialog.show();
    }

    @Override
    public void showLoadDialog(int stringResId) {
//        if (mLoadProgressDialog == null){
//            mLoadProgressDialog = new LoadProgressDialog(getContext());
//            mLoadProgressDialog.setCancelable(false);
//            mLoadProgressDialog.setCanceledOnTouchOutside(false);
//            mLoadProgressDialog.setMessage(stringResId);
//        }else{
//            mLoadProgressDialog.setMessage(stringResId);
//        }
//        mLoadProgressDialog.show();
    }

    @Override
    public void showLoadDialog(String string) {
//        if (mLoadProgressDialog == null){
//            mLoadProgressDialog = new LoadProgressDialog(getContext());
//            mLoadProgressDialog.setCancelable(false);
//            mLoadProgressDialog.setCanceledOnTouchOutside(false);
//            mLoadProgressDialog.setMessage(TextUtils.isEmpty(string)?"":string);
//        }else{
//            mLoadProgressDialog.setMessage(TextUtils.isEmpty(string)?"":string);
//        }
//        mLoadProgressDialog.show();
    }

    @Override
    public void dismissLoadDialog() {
//        if (mLoadProgressDialog != null && mLoadProgressDialog.isShowing()) {
//            mLoadProgressDialog.dismiss();
//        }
    }

    @Override
    public void showToast(String res) {
        ToastUtls.showToast(getActivity(), res);
    }

    @Override
    public void showToast(int resId) {
        ToastUtls.showToast(getActivity(), resId);
    }

    @Override
    public void showToast(int resId, int errorCode) {
        ToastUtls.showToast(getActivity(), resId, errorCode);
    }


    private AtomicBoolean isSettingQuality = new AtomicBoolean(false);
    private boolean mIsSettingQuality = false;
    private boolean mOffline = true;

    /**
     * 更新UI
     */
    private void refreshUI() {
        if (mPlayPresenter.getOpenDeviceInfo() != null && mPlayPresenter.getOpenCameraInfo() != null) {
            if (mPlayPresenter.getOpenDeviceInfo().getStatus() == 2) {
                // TODO: 2016/12/28 不在线处理
                mEZUIPlayerView.showTipText(R.string.realplay_fail_device_not_exist);

                mOffline = true;
                return;
            } else {
                // TODO: 2016/12/28 设备在线处理
                mOffline = false;
            }

            RealmList<EZOpenVideoQualityInfo> qualityInfos = mPlayPresenter.getOpenCameraInfo().getEZOpenVideoQualityInfos();
            for (EZOpenVideoQualityInfo qualityInfo : qualityInfos) {
                int videoLevel = qualityInfo.getVideoLevel();
                Log.i("vl", "refreshUI: 支持清晰度 -- " + videoLevel);
            }
            int videoLevel = mPlayPresenter.getOpenCameraInfo().getVideoLevel();
            Log.i("vl", "refreshUI: 当前清晰度 --> " + videoLevel);

            int setLevel = mInitVideoLevel;//设置的视频清晰度
            if(videoLevel != setLevel) {
                EZLog.debugLog(TAG, "开始设置清晰度（--> " + setLevel + "): " +mCameraName);
                mSettingVideoLevel = mInitVideoLevel;
                //清晰度 不为均衡时
                isSettingQuality.set(true);
                mIsSettingQuality = true;
                mPlayPresenter.setQuality(mDeviceSerial, mCameraNo, setLevel);
            } else {
                EZLog.debugLog(TAG, "清晰度已经为 " + videoLevel + ", deviceSerial: " + mDeviceSerial);

            }
        }

    }

    public int mInitVideoLevel = 1;
    public int mVideoLevel = 1;
    public int mSettingVideoLevel = 1;

    public void changeQuality(int videoLevel) {
        if(mIsRecording) {
            EZLog.debugLog(TAG, "取消更改清晰度，正在录制视频: "  + mCameraName);
            return;
        }
        if(isSettingQuality.get() || mIsSettingQuality) {
            EZLog.debugLog(TAG, "正在设置清晰度（--> " + mSettingVideoLevel + "): "  + mCameraName);
            return;
        }
        if(videoLevel != mVideoLevel) {
            EZLog.debugLog(TAG, "开始设置清晰度（--> " + videoLevel + "): " +mCameraName);
            mSettingVideoLevel = videoLevel;

            isSettingQuality.set(true);
            mIsSettingQuality = true;
            mPlayPresenter.setQuality(mDeviceSerial, mCameraNo, videoLevel);
        } else {
            EZLog.debugLog(TAG, "清晰度已经为 " + videoLevel + ", deviceSerial: " + mDeviceSerial);

        }
    }

    public void setSurfaceSize() {
        EZLog.infoLog(TAG, "setSurfaceSize");
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//        mEZUIPlayerView.setSurfaceSize(dm.widthPixels, 0);
//        mEZUIPlayerView.setSurfaceSize(500, 0);

        boolean isWideScrren = true;
        //竖屏
        if (!isWideScrren) {
            mEZUIPlayerView.setSurfaceSize(dm.widthPixels, 0);
//            mOverlayTopBar.setVisibility(View.GONE);
//            mTopBar.setVisibility(View.VISIBLE);
        } else {
//            if (mIsOnTalk) {
//                return;
//            }
            //横屏
            mEZUIPlayerView.setSurfaceSize(dm.widthPixels/4, 0);
//            mEZUIPlayerView.setSurfaceSize(dm.widthPixels, dm.heightPixels);
//            mOverlayTopBar.setVisibility(View.VISIBLE);
//            showOverlayBar(true);
//            mTopBar.setVisibility(View.GONE);
//            mHandler.removeMessages(MSG_HIDE_TOPBAR);
//            mHandler.removeMessages(MSG_SHOW_TOPBAR);
//            mHandler.sendEmptyMessageDelayed(MSG_HIDE_TOPBAR, SHOW_TOP_BAR_TIME);
        }
    }

    public int mWidth;
    public int mHeight;

    public void setSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public void setSurfaceSize(int width, int height) {
        EZLog.infoLog(TAG, "setSurfaceSize(width, height)");
        mEZUIPlayerView.setSurfaceSize(width, height);

        setSize(width, height);
    }

    private boolean isZOrderOnTop;
    public void setZOrderOnTop(boolean onTop) {
        this.isZOrderOnTop = onTop;
    }
    public void setSurfaceSizeOnTop() {
        mEZUIPlayerView.setZOrderOnTop(isZOrderOnTop);
    }



    private boolean mIsRecording;
    private String mRecordPath;
    /**
     * 录像时长，单位秒
     */
    private int mRecordTime = 0;
    private String mLastOSDTime;

    public void setRecordPath(String recordPath) {
        this.mRecordPath = recordPath;
    }

    /**
     * 开启录像到手机
     */
    public void startRecord() {
        EZLog.debugLog(TAG, "startRecord");
        if (mEZPlayer == null) {
            Log.d(TAG, "EZPlaer is null");
            return;
        }
        if (mIsRecording) {
            stopRecord();
            return;
        }
        if (!EZOpenUtils.isSDCardUseable()) {
            // 提示SD卡不可用
            showToast(R.string.remoteplayback_SDCard_disable_use);
            return;
        }
        if (EZOpenUtils.getSDCardRemainSize() < EZOpenUtils.PIC_MIN_MEM_SPACE) {
            // 提示内存不足
            showToast(R.string.remoteplayback_record_fail_for_memory);
            return;
        }
        if(TextUtils.isEmpty(mRecordPath))
            mRecordPath = DataManager.getRecordFile();

        EZLog.d(TAG, "mRecordPath: " + mRecordPath);
        Toast.makeText(getActivity(), "开始录制···········", Toast.LENGTH_SHORT).show();
        //记录正在录制的视频路径
        CopyRecord.getInstance().addRecordingPath(mRecordPath);
        if (mEZPlayer != null) {
            EZOpenUtils.soundPool(getContext(), R.raw.record);
            if (mEZPlayer.startLocalRecordWithFile(mRecordPath)) {
                handleRecordSuccess();
            } else {
                handleRecordFail();
            }
        }
    }

    /**
     * 开始录像成功
     */
    private void handleRecordSuccess() {
        mIsRecording = true;
        // 计时按钮可见
        mRecordLayout.setVisibility(View.VISIBLE);
        mRecordTimeTextView.setText("00:00");
//        mPlayUI.mRecordImg.setImageResource(R.drawable.resource_selected);
        mRecordTime = 0;
    }

    /**
     * 开始录像失败
     */
    private void handleRecordFail() {
        showToast(R.string.remoteplayback_record_fail);
        if (mIsRecording) {
            stopRecord();
        }
    }

    /**
     * 设置当前录像时间
     */
    private void updateRecordTime() {
        if (mEZPlayer == null && mStatus != STATUS_PLAY) {
            return;
        }
        if (mIsRecording) {
            Calendar calendar = mEZPlayer.getOSDTime();
            if (calendar == null){
                return;
            }
            String time = DateUtil.OSD2Time(calendar);
            if (!TextUtils.equals(time, mLastOSDTime)) {
                mRecordTime++;
                mLastOSDTime = time;
            }
            mRecordTimeTextView.setText(DateUtil.getRecordTime(mRecordTime * 1000));
        }
    }

    /**
     * 停止录像
     */
    public void stopRecord() {
        if (mEZPlayer == null || !mIsRecording) {
            return;
        }
        EZLog.d(TAG, "stopRecord: " + mCameraName);
        showToast(mRecordPath);
        EZOpenUtils.soundPool(getContext(), R.raw.record);
        mEZPlayer.stopLocalRecord();
        // 计时按钮不可见
        mRecordLayout.setVisibility(View.GONE);
//        mPlayUI.mRecordImg.setImageResource(R.drawable.btn_record_selector);
        mIsRecording = false;

        //移除记录：正在录制的视频路径
        CopyRecord.getInstance().removeRecordingPath(mRecordPath);
    }

    public String getRecordPath() {
        return mRecordPath;
    }


    private final long mMaxRecordTime = 60 * 60;//一小时，单位为秒
    /**
     * 判断录像时间是否超过最大录制时间
     */
    public void checkRecordTime() {
        if(!mIsRecording) {
            return;
        }
        //超过最大录制时间
        if(mRecordTime >= mMaxRecordTime) {
            stopRecord();
        }
    }

    public String getCameraName() {
        return mCameraName;
    }

    public boolean hasAlreadyPlay(long time) {
        return startPlayTime != -1 && System.currentTimeMillis() - startPlayTime >= time;
    }


    private List<String> errorTxts;
    public String getStateDescribe() {
        StringBuilder sb = new StringBuilder();
        String statusContent = "未知状态";
        switch (mStatus) {
            case STATUS_INIT:
                statusContent = "初始化状态";
                break;
            case STATUS_PLAY:
                statusContent = "播放状态";
                break;
            case STATUS_START:
                statusContent = "开始状态";
                break;
            case STATUS_STOP:
                statusContent = "停止状态";
                break;

        }
        sb.append(statusContent).append(",");
        //重试播放次数
        sb.append(playFailRetryCount).append(",");

        if(errorTxts != null) {
            int size = errorTxts.size();
            for (int i = 0; i < size; i++) {
                sb.append(errorTxts.get(i)).append(",");
            }
            sb.deleteCharAt(sb.length()-1);
        }

        return sb.toString();
    }
}
