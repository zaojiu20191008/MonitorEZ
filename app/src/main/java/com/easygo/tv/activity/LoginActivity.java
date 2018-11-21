package com.easygo.tv.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.easygo.monitor.R;
import com.easygo.monitor.view.avctivity.RootActivity;
import com.easygo.tv.module.login.LoginContract;
import com.easygo.tv.module.login.LoginPresenter;
import com.easygo.tv.mvp.model.LoginModel;

public class LoginActivity extends RootActivity implements LoginContract.ILoginView {

    private static final String TAG = "LoginActivity";
    protected LoginPresenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        loginPresenter = new LoginPresenter();
        loginPresenter.attach(new LoginModel(), this);
        loginPresenter.login();
    }

    protected int getLayoutId() {
        return R.layout.activity_welcome;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginPresenter.detach();

        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void loginSucces(String token) {

    }

    @Override
    public void loginFailed(String msg) {

    }


    protected final int MSG_LOGIN_FAILED = 1;
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            if(msg.what == MSG_LOGIN_FAILED) {
                loginPresenter.login();
            }
        }
    };

}
