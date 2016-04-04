package com.camnter.newlife.component.service;

/**
 * Description：IBinderView
 * Created by：CaMnter
 * Time：2015-11-16 17:17
 */
public interface IBinderView {
    /**
     * 开始下载
     */
    void downloadStart();

    /**
     * 下载成功
     */
    void downloadSuccess(String imageFilePath);

    /**
     * 下载失败
     */
    void downloadFailure();
}
