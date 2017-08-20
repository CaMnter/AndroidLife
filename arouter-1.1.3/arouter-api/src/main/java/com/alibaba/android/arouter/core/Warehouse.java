package com.alibaba.android.arouter.core;

import com.alibaba.android.arouter.base.UniqueKeyTreeMap;
import com.alibaba.android.arouter.facade.model.RouteMeta;
import com.alibaba.android.arouter.facade.template.IInterceptor;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.android.arouter.facade.template.IRouteGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Storage of route meta and other data.
 *
 * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
 * @version 1.0
 * @since 2017/2/23 下午1:39
 *
 * {@link Warehouse#groupsIndex}
 * 路由组缓存
 * key   = 路由组 name
 * value = 路由组
 *
 * {@link Warehouse#routes}
 * 路由信息缓存
 * key   = 路由 path
 * value = 路由信息类
 *
 * {@link Warehouse#providers}
 * service 缓存
 * key   = 路由信息类 class
 * value = service
 *
 * {@link Warehouse#providersIndex}
 * service 的 路由信息缓存
 * key   = service 的 full package name
 * value = 路由信息类
 *
 * {@link Warehouse#interceptorsIndex}
 * 拦截器缓存
 * key   = 拦截器优先级
 * value = 拦截器 class
 *
 * {@link Warehouse#interceptors}
 * 拦截器缓存
 * value = 拦截器
 */
class Warehouse {

    // Cache route and metas
    static Map<String, Class<? extends IRouteGroup>> groupsIndex = new HashMap<>();
    static Map<String, RouteMeta> routes = new HashMap<>();

    // Cache provider
    static Map<Class, IProvider> providers = new HashMap<>();
    static Map<String, RouteMeta> providersIndex = new HashMap<>();

    // Cache interceptor
    static Map<Integer, Class<? extends IInterceptor>> interceptorsIndex = new UniqueKeyTreeMap<>(
        "More than one interceptors use same priority [%s]");
    static List<IInterceptor> interceptors = new ArrayList<>();


    static void clear() {
        routes.clear();
        groupsIndex.clear();
        providers.clear();
        providersIndex.clear();
        interceptors.clear();
        interceptorsIndex.clear();
    }

}
