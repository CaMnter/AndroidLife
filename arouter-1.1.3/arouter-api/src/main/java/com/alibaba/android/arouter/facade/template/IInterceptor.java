package com.alibaba.android.arouter.facade.template;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.InterceptorCallback;

/**
 * Used for inject custom logic when navigation.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/23 13:56
 *
 * 提供自定义拦截器功能
 *
 * process(Postcard postcard, InterceptorCallback callback)
 * 传入了 路由信息 和 拦截器回调
 * 处理中可以根据情况执行回调，保证拦截器有效运作
 */
public interface IInterceptor extends IProvider {

    /**
     * The operation of this interceptor.
     *
     * @param postcard meta
     * @param callback cb
     */
    void process(Postcard postcard, InterceptorCallback callback);
}
