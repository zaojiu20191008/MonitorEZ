package com.easygo.tv.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.easygo.monitor.BuildConfig;
import com.easygo.monitor.R;
import com.easygo.tv.adapter.TipMessageAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示在屏幕右方的消息对话框（支付成功xx个商品、可疑人员进店 等）
 */
public class TipMessageDialog extends Dialog {

    private FocusRecyclerView mRecycleView;
    private List<TipMessageBean> mData = new ArrayList<>();
    private TipMessageAdapter mTipMessageAdapter;

    public TipMessageDialog(@NonNull Context context) {
        super(context);
    }

    public TipMessageDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected TipMessageDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_tip_message_dialog);

        mRecycleView = ((FocusRecyclerView) findViewById(R.id.recycler_view));
        mRecycleView.setNeedControlFocus(false);

        if(BuildConfig.BUILD_TYPE.equals("dev")) {
            findViewById(R.id.rl_keyboard).setVisibility(View.VISIBLE);

            findViewById(R.id.up).setOnClickListener(mListener);
            findViewById(R.id.center).setOnClickListener(mListener);
            findViewById(R.id.down).setOnClickListener(mListener);

            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRecycleView.getLayoutParams();
            if(lp == null) {
                lp = new RelativeLayout.LayoutParams(-2, -2);
            }
            lp.height = displayMetrics.heightPixels / 3;
            mRecycleView.setLayoutParams(lp);

        } else {
            findViewById(R.id.rl_keyboard).setVisibility(View.GONE);
        }


//        mData.add(new TipMessageBean(TipMessageBean.TYPE_PAY_SUCCESS, 5, ""));
//        mData.add(new TipMessageBean(TipMessageBean.TYPE_BLACK_LIST, "小偷1", ""));
//        mData.add(new TipMessageBean(TipMessageBean.TYPE_BLACK_LIST, "小偷2", ""));
//        mData.add(new TipMessageBean(TipMessageBean.TYPE_PAY_SUCCESS, 10, ""));
//        mData.add(new TipMessageBean(TipMessageBean.TYPE_BLACK_LIST, "小偷3", ""));
//        mData.add(new TipMessageBean(TipMessageBean.TYPE_BLACK_LIST, "小偷4", ""));


        Resources resources = getContext().getResources();
        int no_focus = resources.getColor(R.color.color_text_no_focus);

        mTipMessageAdapter = new TipMessageAdapter();
        mTipMessageAdapter.setHasStableIds(true);
        mRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecycleView.setAdapter(mTipMessageAdapter);
        mRecycleView.setItemAnimator(null);


        mTipMessageAdapter.setColor(no_focus, Color.parseColor("#000000"));
        mTipMessageAdapter.setData(mData);

        mRecycleView.requestFocus();


    }

    public void resetFocus() {
        if(mRecycleView != null) {
            mRecycleView.clearFocus();
            mRecycleView.requestFocus();
        }
    }

    public void addData(TipMessageBean tipMessageBean) {
        this.mData.add(0, tipMessageBean);
        if (mTipMessageAdapter != null) {
//            mTipMessageAdapter.addData(tipMessageBean);
            mTipMessageAdapter.notifyItemInserted(0);
//            mTipMessageAdapter.notifyDataSetChanged();
        }
        if(mRecycleView != null) {
//            mRecycleView.smoothScrollToPosition(0);
            mRecycleView.scrollToPosition(0);
        }
    }

    public OnClickListener mListener = new OnClickListener();

    public class OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.up:
                    move(DIRECTION_UP);
                    break;
                case R.id.center:
                    removeTipMessage();
                    break;
                case R.id.down:
                    move(DIRECTION_DOWN);

                    break;
            }
        }
    }

    private void removeTipMessage() {

        View focusedChild = mRecycleView.getFocusedChild();
        if (focusedChild != null) {
            int position = 0;
            int childCount = mRecycleView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (focusedChild == mRecycleView.getChildAt(i)) {
                    position = i;
                    break;
                }
            }

            if(mTipMessageAdapter != null) {
                mTipMessageAdapter.remove(position);
            }
            resetFocus();
        } else {
            //没有焦点时进行请求获取
            mRecycleView.requestFocus();
            return;
        }

        if(mData.size() == 0) {
            dismiss();
        }
    }

    private final int DIRECTION_UP = 1;
    private final int DIRECTION_DOWN = 3;
    public void move(int direction) {

        switch (direction) {
            case DIRECTION_UP:

                KeyEvent up = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP);
                dispatchKeyEvent(up);
                break;
            case DIRECTION_DOWN:

                KeyEvent down = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN);
                dispatchKeyEvent(down);
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                dismiss();
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                removeTipMessage();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void dismiss() {
        super.dismiss();

        if(mTipMessageAdapter != null) {
            mTipMessageAdapter.clearData();
        }
        this.mData.clear();
    }

    public boolean containMsg(String deviceSerial) {

        int size = mData.size();
        for (int i = 0; i < size; i++) {
            TipMessageBean tipMessageBean = mData.get(i);
            if(deviceSerial.equals(tipMessageBean.deviceSerial)) {
                return true;
            }
        }
        return false;
    }

    public void removeMsg(String deviceSerial) {
        mTipMessageAdapter.remove(deviceSerial);
        if(mData.size() == 0) {
            dismiss();
        } else {
            resetFocus();
        }
    }
}
