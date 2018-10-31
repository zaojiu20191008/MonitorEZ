package com.easygo.tv.mvp.base;


public abstract class BasePresenter<M extends BaseContract.IModel, V extends BaseContract.IView> {

    protected M mModel;
    protected V mView;

    public void attach(M m, V v) {
        this.mModel = m;
        this.mView = v;
    }

    public void detach() {
        this.mModel = null;
        this.mView = null;
    }


}
