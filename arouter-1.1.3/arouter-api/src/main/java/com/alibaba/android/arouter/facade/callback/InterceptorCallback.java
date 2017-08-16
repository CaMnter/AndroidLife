package com.alibaba.android.arouter.facade.callback;

import com.alibaba.android.arouter.facade.Postcard;

/**
 * The callback of interceptor.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/4 17:36
 *
 * 拦截器回调接口
 * 有两个回调事件，继续 和 中断
 * 一般是在拦截器 process 业务过程中，处理数据的业务 或者 交互，根据完成情况
 * 回调 继续 或者 中断
 * 所以，在处理这个拦截器的时候，可以知道该拦截器的运行情况，以便是否执行下个拦截器（ InterceptorServiceImpl ）
 * 或者分发到业务交互上，进行业务交互处理
 */
public interface InterceptorCallback {

    /**
     * Continue process
     *
     * @param postcard route meta
     */
    void onContinue(Postcard postcard);

    /**
     * Interrupt process, pipeline will be destory when this method called.
     *
     * @param exception Reson of interrupt.
     */
    void onInterrupt(Throwable exception);
}
