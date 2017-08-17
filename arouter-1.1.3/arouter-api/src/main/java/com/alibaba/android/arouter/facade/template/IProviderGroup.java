package com.alibaba.android.arouter.facade.template;

import com.alibaba.android.arouter.facade.model.RouteMeta;
import java.util.Map;

/**
 * Template of provider group.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/08/30 12:42
 *
 * 提供者组 接口
 * 一般这个类都是用来在 JavaPoet 生成的类中
 * 批量缓存 其他功能 service
 *
 * loadInto(Map<String, RouteMeta> providers)
 * 的 Map 就是用来缓存 功能 service 的 RouteMeta
 */
public interface IProviderGroup {
    /**
     * Load providers map to input
     *
     * @param providers input
     */
    void loadInto(Map<String, RouteMeta> providers);
}