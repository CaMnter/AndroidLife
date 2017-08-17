package com.alibaba.android.arouter.facade.callback;

import com.alibaba.android.arouter.facade.Postcard;

/**
 * Callback after navigation.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 2016/9/22 14:15
 *
 * 注：ARouter 内部使用，业务层使用的是 NavCallback
 *
 * 在 _ARouter # navigation 进行路由跳转时的回调接口
 *
 * onFound          表示在 _ARouter # navigation 跳转过程中，找到了需要的 RouteMeta
 * onLost           表示在 _ARouter # navigation 跳转过程中，没找到了需要的 RouteMeta
 * onArrival        表示在 _ARouter # navigation 跳转过程中，成功
 * onInterrupt      表示在 _ARouter # navigation 跳转过程中，被某拦截器拦截，发生中断
 */
public interface NavigationCallback {

    /**
     * Callback when find the destination.
     *
     * @param postcard meta
     */
    void onFound(Postcard postcard);

    /**
     * Callback after lose your way.
     *
     * @param postcard meta
     */
    void onLost(Postcard postcard);

    /**
     * Callback after navigation.
     *
     * @param postcard meta
     */
    void onArrival(Postcard postcard);

    /**
     * Callback on interrupt.
     *
     * @param postcard meta
     */
    void onInterrupt(Postcard postcard);
}
