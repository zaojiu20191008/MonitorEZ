package com.easygo.tv.module.login;

import com.easygo.tv.bean.TokenResponse;
import com.easygo.tv.http.HttpResult;
import com.easygo.tv.mvp.base.BaseContract;
import com.easygo.tv.mvp.RequestListener;

public class LoginContract {

    public interface ILoginModel extends BaseContract.IModel {
        void login(RequestListener<HttpResult<TokenResponse>> listener);
    }

    public interface ILoginPresenter extends BaseContract.IPresenter {
        void login();
    }

    public interface ILoginView extends BaseContract.IView {
        void loginSucces(String token);
        void loginFailed(String msg);
    }
}
