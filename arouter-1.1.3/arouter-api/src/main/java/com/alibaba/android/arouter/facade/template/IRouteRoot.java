package com.alibaba.android.arouter.facade.template;

import java.util.Map;

/**
 * Root element.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/23 16:36
 *
 * 路由根 接口
 * 一般这个类都是用来在 JavaPoet 生成的类中
 * 批量 路由组 实现类
 * 路由组实现类也是 JavaPoet 生成的
 *
 * loadInto(Map<String, Class<? extends IRouteGroup>> routes)
 * 的 Map 就是用来缓存 路由组 实现类
 */
public interface IRouteRoot {

    /**
     * Load routes to input
     *
     * @param routes input
     */
    void loadInto(Map<String, Class<? extends IRouteGroup>> routes);
}
