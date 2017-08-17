package com.alibaba.android.arouter.facade.callback;

import com.alibaba.android.arouter.facade.Postcard;

/**
 * Easy to use navigation callback.
 *
 * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
 * @version 1.0
 * @since 2017/4/10 下午12:59
 *
 * 注：ARouter 内部不会使用该类，而是提供给业务层使用的
 *
 * 实现了 NavigationCallback 回调接口，只让外部强制覆写（适配器模式） onArrival
 * 这样就能和 NavigationCallback 一样，使用同一个对外方法 _ARouter # navigation
 *
 * 在 _ARouter # navigation 进行路由跳转时的回调抽象类
 *
 * onFound          表示在 _ARouter # navigation 跳转过程中，找到了需要的 RouteMeta
 * onLost           表示在 _ARouter # navigation 跳转过程中，没找到了需要的 RouteMeta
 * onArrival        表示在 _ARouter # navigation 跳转过程中，成功
 * onInterrupt      表示在 _ARouter # navigation 跳转过程中，被某拦截器拦截，发生中断
 */
public abstract class NavCallback implements NavigationCallback {
    @Override
    public void onFound(Postcard postcard) {
        // Do nothing
    }


    @Override
    public void onLost(Postcard postcard) {
        // Do nothing
    }


    @Override
    public abstract void onArrival(Postcard postcard);


    @Override
    public void onInterrupt(Postcard postcard) {
        // Do nothing
    }
}
