package com.alibaba.android.arouter.facade.template;

import java.util.Map;

/**
 * Template of interceptor group.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/29 09:51
 *
 * 提供自定义 拦截器组 功能
 *
 * loadInto(Map<Integer, Class<? extends IInterceptor>> interceptor)
 * 的 Map 就是用来缓存 拦截器
 */
public interface IInterceptorGroup {
    /**
     * Load interceptor to input
     *
     * @param interceptor input
     */
    void loadInto(Map<Integer, Class<? extends IInterceptor>> interceptor);
}
