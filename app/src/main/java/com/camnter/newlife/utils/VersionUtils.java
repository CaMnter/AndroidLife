package com.camnter.newlife.utils;

import android.os.Build;

/**
 * Description：VersionUtil
 * Created by：CaMnter
 * Time：2015-12-04 22:37
 */
public class VersionUtils {
    /**
     * 检查当前版本是否大于目标版本
     * Check whether the current version is greater than the target version
     *
     * @param targetVersion target version {@link Build.VERSION_CODES}
     * @return result
     */
    public boolean thanOrEqual(int targetVersion) {
        return Build.VERSION.SDK_INT >= targetVersion;
    }
}
