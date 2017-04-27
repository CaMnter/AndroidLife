package com.camnter.mvvm;

import android.databinding.BaseObservable;

/**
 * Description：MVVMViewModel
 * Created by：CaMnter
 */

public abstract class MVVMViewModel<T>
    extends BaseObservable
    implements MVVMViewAdapter.VHandler {

    protected T data;
    protected MVVMViewAdapter<T> adapter;


    public T getData() {
        return this.data;
    }


    public void setData(T data) {
        this.data = data;
    }


    public MVVMViewAdapter<T> getAdapter() {
        return this.adapter;
    }


    public void setAdapter(MVVMViewAdapter<T> adapter) {
        this.adapter = adapter;
    }

    // Waiting for you implementing

}
