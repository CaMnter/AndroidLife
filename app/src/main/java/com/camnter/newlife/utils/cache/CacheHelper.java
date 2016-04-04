package com.camnter.newlife.utils.cache;

import android.content.Context;
import android.support.v4.util.LruCache;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

/**
 * Description：CacheHelper
 * Created by：CaMnter
 * Time：2015-10-28 16:11
 */
public abstract class CacheHelper {

    // 软引用缓存容量
    private static final int SOFT_CACHE_SIZE = 15;
    // 硬引用缓存（二级缓存）
    protected static LruCache<String, Object> mLruCache;
    // 软引用缓存（一级缓存）
    protected static LinkedHashMap<String, SoftReference<Object>> mSoftCache;


    public CacheHelper(Context context) {
        int cacheSize = 30;
        mLruCache = new LruCache<String, Object>(cacheSize) {
            /**
             * 返回自定义的item的大小
             * 默认返回1代表item的数量
             * 最大size就是最大item值
             * 这里不做计算单个item的大小，只计算个数
             * 返回1
             *
             * @param key
             * @param value
             * @return
             */
            @Override protected int sizeOf(String key, Object value) {
                return 1;
            }


            /**
             * 当item被回收或者删掉时调用。该方法当value被回收释放存储空间时被remove调用，
             * 或者替换item值时put调用，默认实现什么都没做。
             * true：为释放空间被删除
             * false：put或remove导致
             *
             * @param evicted
             * @param key
             * @param oldValue
             * @param newValue
             */
            @Override
            protected void entryRemoved(boolean evicted, String key, Object oldValue, Object newValue) {
                // 硬引用缓存容量满的时候，会根据LRU算法把最近没有被使用的转入此软引用缓存
                if (oldValue != null) mSoftCache.put(key, new SoftReference<>(oldValue));
            }
        };

        /**
         * int initialCapacity, float loadFactor, boolean accessOrder
         * initialCapacity：哈希映射表的初始容量
         * loadFactor：初始负荷系数。
         * accessOrder：true：如果排序基于最后一次访问（从最近最少访问到最近访问）
         *              false：如果排序应该是顺序插入的条目
         */
        mSoftCache = new LinkedHashMap<String, SoftReference<Object>>(SOFT_CACHE_SIZE, 0.75f,
                true) {
            /**
             * 移除最旧的数据
             * @param eldest
             * @return
             */
            @Override
            protected boolean removeEldestEntry(Entry<String, SoftReference<Object>> eldest) {
                return this.size() > SOFT_CACHE_SIZE;
            }
        };
    }


    /**
     * 判断缓存是否存在
     */
    public boolean cacheExit(String key) {
        Object result = mLruCache.get(key);
        return result != null || mSoftCache.containsKey(key);
    }


    /**
     * 获取缓存数据
     */
    protected Object getFromCache(String key) {
        Object result;
        //TODO 无论如何先从一级缓存中找
        SoftReference<Object> reference = mSoftCache.get(key);
        // 如果存在一级缓存
        if (reference != null) {
            result = reference.get();
            // 如果数据不为脏数据
            if (result != null) return result;
        }
        //TODO 再从二级缓存中找
        result = mLruCache.get(key);
        if (result != null) {
            // 放入一级缓存中
            mSoftCache.put(key, reference);
            return result;
        }
        return null;
    }


    /**
     * 添加数据到缓存
     */
    public void addToCache(String key, Object result) {
        if (result != null) {
            mLruCache.put(key, result);
        }
    }


    /**
     * 获取对应缓存
     */
    public abstract <T> T getCache(String scope, String model);

    /**
     * 删除对应缓存
     */
    public abstract <T> T delCache(String scope, String model);

    /**
     * 修改对应缓存
     */
    public abstract <T> Object modCache(CacheOption cacheOption, Object obj);

    /**
     * 保存缓存
     */
    public abstract void saveCache(CacheOption cacheOption, Object obj);

    /**
     * 缓存是否可以使用
     */
    public abstract boolean canUse(String scope, String model, int deadlineType);
}
