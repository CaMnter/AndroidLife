package com.camnter.newlife.utils.cache;

/**
 * Description：DeadlineType
 * Created by：CaMnter
 * Time：2015-10-28 13:11
 */
public interface DeadlineType {

    /**
     * 本次启动
     */
    int currentStart = 0;

    /**
     * 截至日期
     */
    int deadline = 1;

    /**
     * 当前版本有效
     */
    int currentVersion = 2;
}