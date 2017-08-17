package com.alibaba.android.arouter.facade.service;

import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * Get class by user, maybe someone use InstantRun and other tech will move dex files.
 *
 * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
 * @version 1.0
 * @since 2017/2/23 下午12:16
 *
 * 扩展了 IProvider 接口，作为 dex 操作 的接口定义
 * 扩展了 forName 方法
 */
public interface ClassLoaderService extends IProvider {
    Class<?> forName();
}
