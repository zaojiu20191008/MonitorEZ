package com.easygo.tv.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.easygo.monitor.R;
import com.easygo.monitor.model.EZOpenCameraInfo;
import com.easygo.tv.upload.CopyRecord;
import com.easygo.tv.adapter.PreviewAdapter;

import java.util.Map;

import io.realm.OrderedRealmCollection;
import rx.Subscription;


public class PreviewActivity extends AppCompatActivity {


    public static OrderedRealmCollection<EZOpenCameraInfo> data;
    private RecyclerView recycler_view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.test_preview);


        initView();


        //根据mac地址获取 需要显示的摄像头预览画面




    }

    private void initView() {
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);

        PreviewAdapter previewAdapter = new PreviewAdapter(this, data);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
//        GridLayoutManager layoutManager = new GridLayoutManager(this, 4,
//                GridLayoutManager.HORIZONTAL, false);

        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setAdapter(previewAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Map.Entry<Integer, Subscription> integerSubscriptionEntry : ((PreviewAdapter) recycler_view.getAdapter()).subscrptions.entrySet()) {
            integerSubscriptionEntry.getValue().unsubscribe();
        }
        ((PreviewAdapter) recycler_view.getAdapter()).stop = true;
    }

    @Override
    protected void onResume() {
        super.onResume();


        testCopy(null);
    }

    public void testCopy(View view) {

        Toast.makeText(this, "开始测试拷贝功能···", Toast.LENGTH_SHORT).show();
//        CopyRecord.testCopy();
        CopyRecord.getInstance().testCopyInDirectory();
    }
}
