package com.easygo.tv.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.easygo.monitor.R;
import com.easygo.monitor.common.EZOpenConstant;
import com.easygo.monitor.model.EZOpenCameraInfo;
import com.easygo.tv.message.CMQ;
import com.easygo.monitor.view.fragment.TestFragment;

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


    public int[] ids = new int[]{
            R.id.fragment_1,
            R.id.fragment_2,
            R.id.fragment_3,
    };
    private ArrayList<TestFragment> lists;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test);

        ezPlay();

    }


    private void ezPlay() {

        if(data == null || data.size() == 0)
            return;

        Log.i(TAG, "ezPlay: data.size() - " + data.size());



        lists = new ArrayList<>();
        int count = ids.length;

        for (int i = 0; i < count; i++) {
            TestFragment testFragment = new TestFragment();


            Bundle bundle = new Bundle();
//            bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, serials[i]);
            bundle.putString(EZOpenConstant.EXTRA_DEVICE_SERIAL, data.get(i).getDeviceSerial());
            bundle.putString("DEVICE_NAME", data.get(i).getCameraName());
            bundle.putInt(EZOpenConstant.EXTRA_CAMERA_NO, 1);
            testFragment.setArguments(bundle);

            lists.add(testFragment);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < count; i++) {
            transaction.add(ids[i], lists.get(i), String.valueOf(i));
        }
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void startAcceptMessage() {
        CMQ.getInstance().accept(new CMQ.OnMessageListener() {
            @Override
            public void onAccept(String msg) {
                //接收后台返回的消息


                //1.获取摄像头 序列号
                //2.获取消息类型  （开门直播、录制）
                //3.根据类型 执行不同方法
            }
        });
    }

}
