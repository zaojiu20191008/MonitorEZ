package com.easygo.tv.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;

public class FocusRecyclerView extends RecyclerView {
    public FocusRecyclerView(Context context) {
        super(context);
    }

    public FocusRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean needControlFocus = true;
    public void setNeedControlFocus(boolean needControlFocus) {
        this.needControlFocus = needControlFocus;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result = super.dispatchKeyEvent(event);
        if(!needControlFocus) {
            return result;
        }
        View focusView = this.getFocusedChild();

        if (focusView == null) {
            return result;
        } else {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                return true;
            } else {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        View upView = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_UP);
                        if (upView != null) {
                            upView.requestFocus();
                            return true;
                        } else {
                            return false;
                        }
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        View downView = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_DOWN);
                        if (downView != null) {
                            downView.requestFocus();
                            return true;
                        } else {
                            return false;
                        }
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:

                        return false;
                }
            }
        }
        return result;
    }

}
