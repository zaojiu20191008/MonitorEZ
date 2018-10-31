package com.easygo.tv.module.login;

import com.easygo.tv.mvp.base.BaseContract;
import com.easygo.tv.mvp.RequestListener;

public class LoginContract {

    public interface ILoginModel extends BaseContract.IModel {
        void login(RequestListener listener);
        void getSerials(RequestListener listener);
    }

    public interface ILoginPresenter extends BaseContract.IPresenter {
        void login();
        void getSerials();
    }

    public interface ILoginView extends BaseContract.IView {
        void loginSucces();
        void loginFailed();

        void serialsSuccess();
        void serialsfailed();
    }
}
