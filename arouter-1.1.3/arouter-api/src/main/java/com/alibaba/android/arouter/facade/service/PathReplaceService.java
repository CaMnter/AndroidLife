package com.alibaba.android.arouter.facade.service;

import android.net.Uri;
import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * Preprocess your path
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 2016/12/9 16:48
 *
 * 扩展了 IProvider 接口，作为 路由 Path 替换 的接口定义
 * 扩展了 forUri 方法          Uri 传入的预处理
 * 扩展了 forString 方法       Path 传入的预处理
 *
 * 可以跳转前预处理 地址
 */
public interface PathReplaceService extends IProvider {

    /**
     * For normal path.
     *
     * @param path raw path
     */
    String forString(String path);

    /**
     * For uri type.
     *
     * @param uri raw uri
     */
    Uri forUri(Uri uri);
}
