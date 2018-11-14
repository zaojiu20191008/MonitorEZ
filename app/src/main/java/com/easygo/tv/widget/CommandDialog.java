package com.easygo.tv.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.view.View;

import com.easygo.monitor.R;
import com.easygo.tv.Constant;
import com.easygo.tv.adapter.CommandAdapter;

import java.util.List;

public class CommandDialog extends Dialog {

    private FocusRecyclerView mRecycleView;
    private List<String> mData;

    public CommandDialog(@NonNull Context context) {
        super(context);
    }

    public CommandDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CommandDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_focus_dialog);

        mRecycleView = ((FocusRecyclerView) findViewById(R.id.recycler_view));


         findViewById(R.id.up).setOnClickListener(mListener);
         findViewById(R.id.center).setOnClickListener(mListener);
         findViewById(R.id.down).setOnClickListener(mListener);


        mData = Constant.CMD.data;

        CommandAdapter commandAdapter = new CommandAdapter();
        mRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecycleView.setAdapter(commandAdapter);

        commandAdapter.setData(mData);

        mRecycleView.requestFocus();
    }

    public void resetFocus() {
        if(mRecycleView != null) {
            mRecycleView.clearFocus();
            mRecycleView.requestFocus();
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
                    execCmd();
                    break;
                case R.id.down:
                    move(DIRECTION_DOWN);

                    break;
            }
        }
    }

    private void execCmd() {

        View focusedChild = mRecycleView.getFocusedChild();
        if (focusedChild != null) {
            String cmdText = (String) focusedChild.getTag();

            if(mOnCmdListener != null) {
                mOnCmdListener.onCmdSelected(cmdText);
            }
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

    private OnCmdListener mOnCmdListener;
    public interface OnCmdListener {
        void onCmdSelected(String cmdText);
    }
    public void setOnCmdListener(OnCmdListener listener) {
        this.mOnCmdListener = listener;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                dismiss();
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                execCmd();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
