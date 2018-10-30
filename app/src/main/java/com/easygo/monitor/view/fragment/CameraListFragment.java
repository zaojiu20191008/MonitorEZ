package com.easygo.monitor.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.easygo.monitor.common.EZOpenConstant;
import com.easygo.monitor.presenter.DevicePresenter;
import com.easygo.tv.activity.PreviewActivity;
import com.easygo.tv.activity.LiveStreamActivity;
import com.easygo.monitor.utils.EZDeviceDBManager;
import com.easygo.monitor.utils.EZLog;
import com.easygo.monitor.view.DeviceView;
import com.easygo.monitor.view.adapter.CameraAdapter;
import com.easygo.monitor.view.avctivity.DeviceSettingActivity;
import com.easygo.monitor.view.avctivity.ModifyNameActivity;
import com.easygo.monitor.view.avctivity.PlayActivity;
import com.easygo.monitor.view.widget.CameraSettingDialog;
import com.easygo.monitor.view.widget.PullRefreshRealmRecyclerView;
import com.easygo.monitor.R;
/**
 * Description:预览列表
 * Created by dingwei3
 *
 * @date : 2016/12/12
 */
public class CameraListFragment extends BaseLazyFragment implements DeviceView,PullRefreshRealmRecyclerView.OnAutoRefreshingListner, CameraAdapter.OnCameraItemClickListener,CameraSettingDialog.OnSettingItemClickLientener {


    public void test() {
        Intent intent = new Intent(getActivity(), LiveStreamActivity.class);

        LiveStreamActivity.data = mCameraAdapter.getData();

        startActivity(intent);
    }

    public void preview() {
        Intent intent = new Intent(getActivity(), PreviewActivity.class);

        PreviewActivity.data = mCameraAdapter.getData();

        startActivity(intent);
    }



    private final static String TAG = "CameraListFragment";
    private DevicePresenter mDevicePresenter;
    private PullRefreshRealmRecyclerView mPullRefreshRecyclerView;
    private CameraAdapter mCameraAdapter;

    public CameraListFragment(){
        mDevicePresenter = new DevicePresenter(this);
        Log.d(TAG,mDevicePresenter.getRealm().toString());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CameraListFragment","onCreate");
    }

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cameralist_layout,null);
        mPullRefreshRecyclerView = (PullRefreshRealmRecyclerView) view.findViewById(R.id.message_list_layout);
        mCameraAdapter = new CameraAdapter(mActivity,mDevicePresenter.getEZOpenCameraInfoList());
        mPullRefreshRecyclerView.setAdapter(mCameraAdapter);
        mPullRefreshRecyclerView.setOnAutoRefreshingListner(this);
        mCameraAdapter.setOnItemClickListener(this);


        //todo  test
        view.findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
        view.findViewById(R.id.preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview();
            }
        });


        return view;
    }

    @Override
    protected void lazyLoad() {
        mPullRefreshRecyclerView.onRefreshing();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("CameraListFragment","onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("CameraListFragment","onAttach");
    }

    @Override
    public void onDetach() {
        Log.d("CameraListFragment","onDetach");
        super.onDetach();
    }

    @Override
    public void loadFinish() {
        mPullRefreshRecyclerView.onRefreshComplete();
    }

    @Override
    public void refreshFinish() {
        mPullRefreshRecyclerView.onRefreshComplete();
    }

    @Override
    public void onAutoRefreshing() {
        mDevicePresenter.loadDeviceList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDevicePresenter.release();
    }

    @Override
    public void onItemClick(final String deviceSerial, int cameraNo) {
        Intent intent = new Intent(mActivity, PlayActivity.class);
        intent.putExtra(EZOpenConstant.EXTRA_DEVICE_SERIAL, deviceSerial);
        intent.putExtra(EZOpenConstant.EXTRA_CAMERA_NO, cameraNo);
        mActivity.startActivity(intent);
    }

    @Override
    public void onItemSetting(final int position) {
        String ss = EZDeviceDBManager.getDevPwd(mCameraAdapter.getItem(position).getDeviceSerial());
        EZLog.d(TAG,"deviceSerial = "+mCameraAdapter.getItem(position).getDeviceSerial()+",password = "+ (TextUtils.isEmpty(ss)?"":ss));
        int status = mCameraAdapter.getItem(position).getStatus();
        int defence = mCameraAdapter.getItem(position).isSupportDefence()?mCameraAdapter.getItem(position).getDefence():-1;
        CameraSettingDialog cameraSettingDialog = new CameraSettingDialog(mContext,position,status,defence,this);
        cameraSettingDialog.show();
    }


    @Override
    public void modifyCameraName(int position) {
        ModifyNameActivity.startModifyCameraNameActivity(mContext, mCameraAdapter.getItem(position).getCameraName(), mCameraAdapter.getItem(position).getDeviceSerial(),mCameraAdapter.getItem(position).getCameraNo());
    }

    @Override
    public void modifyCameraCover(int position) {
        EZLog.d(TAG,"modifyCameraCover deviceSerial = "+mCameraAdapter.getItem(position).getDeviceSerial() + "  CameraNo = "+mCameraAdapter.getItem(position).getCameraNo());
        mCameraAdapter.captureCover(position);
    }

    @Override
    public void setDefence(int position, int defence) {
        mDevicePresenter.setDeviceDefence(mCameraAdapter.getItem(position).getDeviceSerial(),defence);
    }

    @Override
    public void moreSetting(int position) {
        DeviceSettingActivity.startDeviceSetting(mContext, mCameraAdapter.getItem(position).getDeviceSerial());
    }
}


