package com.camnter.newlife.utils.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Static library version of {@link android.util.LruCache}. Used to write apps
 * that run on API levels prior to 12. When running on API level 12 or above,
 * this implementation is still used; it does not try to switch to the
 * framework's implementation. See the framework SDK documentation for a class
 * overview.
 */
public class LruCache<K, V> {
    private final LinkedHashMap<K, V> map;

    /**
     * Size of this cache in units. Not necessarily the number of elements.
     * 这个缓存大小的单位。不规定元素的数量。
     */

    // 已经存储的数据大小
    private int size;
    // 最大存储大小
    private int maxSize;

    // 调用put方法的次数
    private int putCount;
    // 调用create方法的次数
    private int createCount;
    // 回收的次数
    private int evictionCount;
    // 命中的次数（取出数据的成功次数）
    private int hitCount;
    // 丢失的次数（取出数据的失败次数）
    private int missCount;


    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     * the maximum number of entries in the cache. For all other caches,
     * this is the maximum sum of the sizes of the entries in this cache.
     */
    public LruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<K, V>(0, 0.75f, true);
    }


    /**
     * Sets the size of the cache.
     * 设置缓存的大小。
     *
     * @param maxSize The new maximum size.
     */
    public void resize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }

        synchronized (this) {
            this.maxSize = maxSize;
        }
        trimToSize(maxSize);
    }


    /**
     * Returns the value for {@code key} if it exists in the cache or can be
     * created by {@code #create}. If a value was returned, it is moved to the
     * head of the queue. This returns null if a value is not cached and cannot
     * be created.
     * 根据key查询缓存，如果存在于缓存或者被create方法创建了。
     * 如果值返回了，那么它将被移动到队列的头部。
     * 如果如果没有缓存的值，则返回null。
     */
    public final V get(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V mapValue;
        synchronized (this) {
            mapValue = map.get(key);
            if (mapValue != null) {
                hitCount++;
                return mapValue;
            }
            missCount++;
        }

        /*
         * Attempt to create a value. This may take a long time, and the map
         * may be different when create() returns. If a conflicting value was
         * added to the map while create() was working, we leave that value in
         * the map and release the created value.
         *
         * 尝试创建一个值，这可能需要很长时间，并且Map可能在create()返回的值时有所不同。如果在create()执行的时
         * 候，一个冲突的值被添加到Map，我们在Map中删除这个值，释放被创造的值。
         */

        V createdValue = create(key);
        if (createdValue == null) {
            return null;
        }

        synchronized (this) {
            createCount++;
            mapValue = map.put(key, createdValue);

            // 如果之前存在相同key的value，即有冲突。
            if (mapValue != null) {
                /*
                 * There was a conflict so undo that last put
                 * 有冲突所以撤销最后一把
                 */
                map.put(key, mapValue);
            } else {
                size += safeSizeOf(key, createdValue);
            }
        }

        // 如果发生冲突
        if (mapValue != null) {

            entryRemoved(false, key, createdValue, mapValue);
            return mapValue;
        } else {
            trimToSize(maxSize);
            return createdValue;
        }
    }


    /**
     * Caches {@code value} for {@code key}. The value is moved to the head of
     * the queue.
     * 给对应key缓存value，该value将被移动到队头。
     *
     * @return the previous value mapped by {@code key}.
     */
    public final V put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }

        V previous;
        synchronized (this) {
            putCount++;
            size += safeSizeOf(key, value);
            previous = map.put(key, value);
            if (previous != null) {
                size -= safeSizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, value);
        }

        trimToSize(maxSize);
        return previous;
    }


    /**
     * Remove the eldest entries until the total of remaining entries is at or
     * below the requested size.
     * 删除最旧的数据直到剩余的数据的总数以下要求的大小。
     *
     * @param maxSize the maximum size of the cache before returning. May be -1
     * to evict even 0-sized elements.
     */
    public void trimToSize(int maxSize) {
        while (true) {
            K key;
            V value;
            synchronized (this) {
                if (size < 0 || (map.isEmpty() && size != 0)) {
                    throw new IllegalStateException(
                            getClass().getName() + ".sizeOf() is reporting inconsistent results!");
                }

                if (size <= maxSize || map.isEmpty()) {
                    break;
                }

                Map.Entry<K, V> toEvict = map.entrySet().iterator().next();
                key = toEvict.getKey();
                value = toEvict.getValue();
                map.remove(key);
                size -= safeSizeOf(key, value);
                evictionCount++;
            }

            entryRemoved(true, key, value, null);
        }
    }


    /**
     * Removes the entry for {@code key} if it exists.
     * 如果对应key的entry存在，则删除。
     *
     * @return the previous value mapped by {@code key}.
     */
    public final V remove(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V previous;
        synchronized (this) {
            previous = map.remove(key);
            if (previous != null) {
                size -= safeSizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, null);
        }

        return previous;
    }


    /**
     * Called for entries that have been evicted or removed. This method is
     * invoked when a value is evicted to make space, removed by a call to
     * {@link #remove}, or replaced by a call to {@link #put}. The default
     * implementation does nothing.
     * <p/>
     * 当被回收或者删掉时调用。该方法当value被回收释放存储空间时被remove调用
     * 或者替换条目值时put调用，默认实现什么都没做。
     * <p/>
     * <p>The method is called without synchronization: other threads may
     * access the cache while this method is executing.
     * 该方法没用同步调用，如果其他线程访问缓存时，该方法也会执行。
     *
     * @param evicted true if the entry is being removed to make space, false
     * if the removal was caused by a {@link #put} or {@link #remove}.
     * <p/>
     * true：如果该条目被删除空间
     * false：put或remove导致
     * @param newValue the new value for {@code key}, if it exists. If non-null,
     * this removal was caused by a {@link #put}. Otherwise it was caused by
     * an eviction or a {@link #remove}.
     * <p/>
     * 如果存在key对应的新value。如果不为null，那么被put()或remove()调用。
     */
    protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
    }


    /**
     * Called after a cache miss to compute a value for the corresponding key.
     * Returns the computed value or null if no value can be computed. The
     * default implementation returns null.
     * <p/>
     * 缓存丢失之后计算相应的key的value后调用。
     * 返回计算后的值，如果没有value可以计算返回null。
     * 默认的实现返回null。
     * <p>The method is called without synchronization: other threads may
     * access the cache while this method is executing.
     * 该方法没用同步调用，如果其他线程访问缓存时，该方法也会执行。
     * <p/>
     * <p>If a value for {@code key} exists in the cache when this method
     * returns, the created value will be released with {@link #entryRemoved}
     * and discarded. This can occur when multiple threads request the same key
     * at the same time (causing multiple values to be created), or when one
     * thread calls {@link #put} while another is creating a value for the same
     * key.
     * <p/>
     * 当这个方法返回的时候，如果对应key的value存在缓存内，被创建的value将会被entryRemoved()释放或者丢弃。
     * 这情况可以发生在多线程在同一时间上请求相同key（导致多个value被创建了），或者单线程中调用了put()去创建一个
     * 相同key的value
     */
    protected V create(K key) {
        return null;
    }


    private int safeSizeOf(K key, V value) {
        int result = sizeOf(key, value);
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + key + "=" + value);
        }
        return result;
    }


    /**
     * Returns the size of the entry for {@code key} and {@code value} in
     * user-defined units.  The default implementation returns 1 so that size
     * is the number of entries and max size is the maximum number of entries.
     * 返回条目在用户定义单位的大小。默认实现返回1，这样的大小是条目的数量并且最大的大小是条目的最大数量。
     * <p/>
     * <p>An entry's size must not change while it is in the cache.
     * 一个条目的大小必须不能在缓存中改变
     */
    protected int sizeOf(K key, V value) {
        return 1;
    }


    /**
     * Clear the cache, calling {@link #entryRemoved} on each removed entry.
     * 清理缓存
     */
    public final void evictAll() {
        trimToSize(-1); // -1 will evict 0-sized elements
    }


    /**
     * For caches that do not override {@link #sizeOf}, this returns the number
     * of entries in the cache. For all other caches, this returns the sum of
     * the sizes of the entries in this cache
     * <p/>
     * 对于这个缓存，如果不覆写sizeOf()方法，这个方法返回的是条目的在缓存中的数量。但是对于其他缓存，返回的是
     * 条目在缓存中大小的总和。
     */
    public synchronized final int size() {
        return size;
    }


    /**
     * For caches that do not override {@link #sizeOf}, this returns the maximum
     * number of entries in the cache. For all other caches, this returns the
     * maximum sum of the sizes of the entries in this cache.
     * <p/>
     * 对于这个缓存，如果不覆写sizeOf()方法，这个方法返回的是条目的在缓存中的最大数量。但是对于其他缓存，返回的是
     * 条目在缓存中最大大小的总和。
     */
    public synchronized final int maxSize() {
        return maxSize;
    }


    /**
     * Returns the number of times {@link #get} returned a value that was
     * already present in the cache.
     * 返回的次数{@link #get}这是返回一个值在缓存中已经存在。
     */
    public synchronized final int hitCount() {
        return hitCount;
    }


    /**
     * Returns the number of times {@link #get} returned null or required a new
     * value to be created.
     * 返回的次数{@link #get}返回null或需要一个新的要创建价值。
     */
    public synchronized final int missCount() {
        return missCount;
    }


    /**
     * Returns the number of times {@link #create(Object)} returned a value.
     * 返回的次数{@link #create(Object)}返回一个值。
     */
    public synchronized final int createCount() {
        return createCount;
    }


    /**
     * Returns the number of times {@link #put} was called.
     * 返回{@link #put}的次数。
     */
    public synchronized final int putCount() {
        return putCount;
    }


    /**
     * Returns the number of values that have been evicted.
     * 返回被回收的value数量。
     */
    public synchronized final int evictionCount() {
        return evictionCount;
    }


    /**
     * Returns a copy of the current contents of the cache, ordered from least
     * recently accessed to most recently accessed.
     *
     * 返回当前缓存内容的一个副本，从最近很少访问到最最近经常访问。
     */
    public synchronized final Map<K, V> snapshot() {
        return new LinkedHashMap<K, V>(map);
    }


    @Override public synchronized final String toString() {
        int accesses = hitCount + missCount;
        int hitPercent = accesses != 0 ? (100 * hitCount / accesses) : 0;
        return String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", maxSize,
                hitCount, missCount, hitPercent);
    }
}