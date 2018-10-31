package com.easygo.tv.mvp;

/**
 * Created by asus-e on 2017/11/15.
 */

public interface RequestListener<T> {
    void onSuccess(T result);

    void onFailed(String message);
}
