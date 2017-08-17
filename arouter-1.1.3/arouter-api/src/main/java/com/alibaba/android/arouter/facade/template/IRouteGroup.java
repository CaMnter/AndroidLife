package com.alibaba.android.arouter.facade.template;

import com.alibaba.android.arouter.facade.model.RouteMeta;
import java.util.Map;

/**
 * Group element.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/23 16:37
 *
 * 路由组 接口
 * 一般这个类都是用来在 JavaPoet 生成的类中
 * 批量缓存 注册并缓存 路由
 *
 * loadInto(Map<String, RouteMeta> atlas)
 * 的 Map 就是用来缓存 路由的数据信息类
 */
public interface IRouteGroup {
    /**
     * Fill the atlas with routes in group.
     */
    void loadInto(Map<String, RouteMeta> atlas);
}
