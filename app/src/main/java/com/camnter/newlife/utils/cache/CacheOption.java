package com.camnter.newlife.utils.cache;

/**
 * Description：CacheOption
 * Created by：CaMnter
 * Time：2015-10-28 18:11
 */
public class CacheOption {
    /**
     * 作用域
     */
    public String scope;

    /**
     * model 名称
     */
    public String model;

    /**
     * 有效期类型：
     * DeadlineType.currentStart
     * DeadlineType.deadline
     * DeadlineType.currentVersion
     */
    public int deadlineType;

    /**
     * 版本
     */
    public String version;

    /**
     * 过期时间
     */
    public Long deadline;
}
