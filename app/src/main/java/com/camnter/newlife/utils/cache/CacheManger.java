package com.camnter.newlife.utils.cache;

import android.content.Context;

/**
 * Description：CacheManger
 * Created by：CaMnter
 * Time：2015-10-28 15:14
 */
public class CacheManger {

    public static CacheManger cacheManger;
    public CacheHelper cacheHelper;


    private CacheManger() {
    }


    public static CacheManger getInstance() {
        if (cacheManger == null) {
            cacheManger = new CacheManger();
        }
        return cacheManger;
    }


    public void init(Context context) {
        this.cacheHelper = new FileCacheHelper(context);
    }


    public <T> T get(String scope, String model) {
        return this.cacheHelper.getCache(scope, model);
    }


    /**
     * 保存缓存文件
     */
    public void save(CacheOption option, Object obj) {
        this.cacheHelper.saveCache(option, obj);
    }


    /**
     * 修改缓存文件
     */
    public void modify(CacheOption cacheOption, Object obj) {
        this.cacheHelper.modCache(cacheOption, obj);
    }


    /**
     * 删除缓存文件
     */
    public void delete(String scope, String model) {
        this.cacheHelper.delCache(scope, model);
    }


    /**
     * 是否可以使用该缓存
     */
    public void canUse(String scope, String model, int deadlineType) {
        this.cacheHelper.canUse(scope, model, deadlineType);
    }
}
