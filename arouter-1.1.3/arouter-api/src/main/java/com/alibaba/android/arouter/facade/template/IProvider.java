package com.alibaba.android.arouter.facade.template;

import android.content.Context;

/**
 * Provider interface, base of other interface.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/23 23:08
 *
 * 所有 service 的父接口
 * 一般要扩展什么功能都要实现这个接口
 * 并且每个路由都是这个接口的子类
 *
 * 每个实现类都要通过 init 方法，传入 Android Context
 */
public interface IProvider {

    /**
     * Do your init work in this method, it well be call when processor has been load.
     *
     * @param context ctx
     */
    void init(Context context);
}
