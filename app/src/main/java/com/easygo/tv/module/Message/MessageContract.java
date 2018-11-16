package com.easygo.tv.module.Message;

import com.easygo.tv.bean.TokenResponse;
import com.easygo.tv.http.HttpResult;
import com.easygo.tv.mvp.RequestListener;
import com.easygo.tv.mvp.base.BaseContract;
import com.videogo.openapi.bean.EZAlarmInfo;

import java.util.List;

public class MessageContract {

    public interface IMessageModel extends BaseContract.IModel {
    }

    public interface IMessagePresenter extends BaseContract.IPresenter {
    }

    public interface IMessageView extends BaseContract.IView {
        /**
         * 告警消息返回成功
         * @param list
         */
        void messageSuccess(List<EZAlarmInfo> list);
        /**
         * 错误返回
         */
        void onError();

    }
}
