package com.alibaba.android.arouter.facade.service;

import android.content.Context;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * Provide degrade service for router, you can do something when route has lost.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 2016/9/22 14:51
 *
 * 扩展了 IProvider 接口，作为 路由器降级服务 的接口定义
 * 扩展了 onLost 方法
 *
 * 在 _ARouter # navigation 跳转过程中，没找到了需要的 RouteMeta
 * 并且在没有 NavCallback 或者 NavigationCallback 的情况下，就会调用 onLost
 *
 * 可以找到路由信息的时候，进行业务处理（交互提示 或者 跳转）
 */
public interface DegradeService extends IProvider {

    /**
     * Router has lost.
     *
     * @param postcard meta
     */
    void onLost(Context context, Postcard postcard);
}
