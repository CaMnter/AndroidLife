package com.camnter.newlife.utils.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A cache that holds strong references to a limited number of values. Each time
 * a value is accessed, it is moved to the head of a queue. When a value is
 * added to a full cache, the value at the end of that queue is evicted and may
 * become eligible for garbage collection.
 *
 * <p>If your cached values hold resources that need to be explicitly released,
 * override {@link #entryRemoved}.
 *
 * <p>If a cache miss should be computed on demand for the corresponding keys,
 * override {@link #create}. This simplifies the calling code, allowing it to
 * assume a value will always be returned, even when there's a cache miss.
 *
 * <p>By default, the cache size is measured in the number of entries. Override
 * {@link #sizeOf} to size the cache in different units. For example, this cache
 * is limited to 4MiB of bitmaps:
 * <pre>   {@code
 *   int cacheSize = 4 * 1024 * 1024; // 4MiB
 *   LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
 *       protected int sizeOf(String key, Bitmap value) {
 *           return value.getByteCount();
 *       }
 *   }}</pre>
 *
 * <p>This class is thread-safe. Perform multiple cache operations atomically by
 * synchronizing on the cache: <pre>   {@code
 *   synchronized (cache) {
 *     if (cache.get(key) == null) {
 *         cache.put(key, value);
 *     }
 *   }}</pre>
 *
 * <p>This class does not allow null to be used as a key or value. A return
 * value of null from {@link #get}, {@link #put} or {@link #remove} is
 * unambiguous: the key was not in the cache.
 *
 * <p>This class appeared in Android 3.1 (Honeycomb MR1); it's available as part
 * of <a href="http://developer.android.com/sdk/compatibility-library.html">Android's
 * Support Package</a> for earlier releases.
 */
public class LruCache<K, V> {
    private final LinkedHashMap<K, V> map;

    /**
     * 缓存大小的单位。不规定元素的数量。
     */
    // 已经存储的数据大小
    private int size;
    // 最大存储大小
    private int maxSize;

    // 调用put的次数
    private int putCount;
    // 调用create的次数
    private int createCount;
    // 收回的次数 (如果出现)
    private int evictionCount;
    // 命中的次数（取出数据的成功次数）
    private int hitCount;
    // 丢失的次数（取出数据的丢失次数）
    private int missCount;


    /**
     * LruCache的构造方法：需要传入最大缓存个数
     */
    public LruCache(int maxSize) {
        // 最大缓存个数小于0，会抛出IllegalArgumentException
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        /*
         * 初始化LinkedHashMap
         * 第一个参数：initialCapacity，初始大小
         * 第二个参数：loadFactor，负载因子=0.75f
         * 第三个参数：accessOrder=true，基于访问顺序；accessOrder=false，基于插入顺序
         */
        this.map = new LinkedHashMap<K, V>(0, 0.75f, true);
    }


    /**
     * 设置缓存的大小。
     */
    public void resize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        // 防止外部多线程的情况下设置缓存大小造成的线程不安全
        synchronized (this) {
            this.maxSize = maxSize;
        }
        // 重整数据
        trimToSize(maxSize);
    }


    /**
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
            // LinkHashMap 如果设置按照访问顺序的话，这里每次get都会重整数据顺序
            mapValue = map.get(key);
            // 计算 命中次数
            if (mapValue != null) {
                hitCount++;
                return mapValue;
            }
            // 计算 丢失次数
            missCount++;
        }

        /*
         * 官方解释：
         * 尝试创建一个值，这可能需要很长时间，并且Map可能在create()返回的值时有所不同。如果在create()执行的时
         * 候，一个冲突的值被添加到Map，我们在Map中删除这个值，释放被创造的值。
         */
        V createdValue = create(key);
        if (createdValue == null) {
            return null;
        }

        /***************************
         * 不覆写create方法走不到下面 *
         ***************************/

        /*
         * 正常情况走不到这里
         * 走到这里的话 说明 实现了自定义的 create(K key) 逻辑
         * 因为默认的 create(K key) 逻辑为null
         */
        synchronized (this) {
            // 记录 create 的次数
            createCount++;
            // 将自定义create创建的值，放入LinkedHashMap中，如果key已经存在，会返回 之前相同key 的值
            mapValue = map.put(key, createdValue);

            // 如果之前存在相同key的value，即有冲突。
            if (mapValue != null) {
                /*
                 * 有冲突
                 * 所以 撤销 刚才的 操作
                 * 将 之前相同key 的值 重新放回去
                 */
                map.put(key, mapValue);
            } else {
                // 拿到键值对，计算出在容量中的相对长度，然后加上
                size += safeSizeOf(key, createdValue);
            }
        }

        // 如果上面 判断出了 将要放入的值发生冲突
        if (mapValue != null) {
            /*
             * 刚才create的值被删除了，原来的 之前相同key 的值被重新添加回去了
             * 告诉 自定义 的 entryRemoved 方法
             */
            entryRemoved(false, key, createdValue, mapValue);
            return mapValue;
        } else {
            // 上面 进行了 size += 操作 所以这里要重整长度
            trimToSize(maxSize);
            return createdValue;
        }
    }


    /**
     * 给对应key缓存value，该value将被移动到队头。
     */
    public final V put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }

        V previous;
        synchronized (this) {
            // 记录 put 的次数
            putCount++;
            // 拿到键值对，计算出在容量中的相对长度，然后加上
            size += safeSizeOf(key, value);
            /*
             * 放入 key value
             * 如果 之前存在key 则返回 之前key 的value
             * 记录在 previous
             */
            previous = map.put(key, value);
            // 如果存在冲突
            if (previous != null) {
                // 计算出 冲突键值 在容量中的相对长度，然后减去
                size -= safeSizeOf(key, previous);
            }
        }

        // 如果上面发生冲突
        if (previous != null) {
            /*
             * previous值被剔除了，此次添加的 value 已经作为key的 新值
             * 告诉 自定义 的 entryRemoved 方法
             */
            entryRemoved(false, key, previous, value);
        }
        trimToSize(maxSize);
        return previous;
    }


    /**
     * 删除最旧的数据直到剩余的数据的总数以下要求的大小。
     */
    public void trimToSize(int maxSize) {
        /*
         * 这是一个死循环，
         * 1.只有 扩容 的情况下能立即跳出
         * 2.非扩容的情况下，map的数据会一个一个删除，直到map里没有值了，就会跳出
         */
        while (true) {
            K key;
            V value;
            synchronized (this) {
                // 在重新调整容量大小前，本身容量就为空的话，会出异常的。
                if (size < 0 || (map.isEmpty() && size != 0)) {
                    throw new IllegalStateException(
                            getClass().getName() + ".sizeOf() is reporting inconsistent results!");
                }
                // 如果是 扩容 或者 map为空了，就会中断，因为扩容不会涉及到丢弃数据的情况
                if (size <= maxSize || map.isEmpty()) {
                    break;
                }

                Map.Entry<K, V> toEvict = map.entrySet().iterator().next();
                key = toEvict.getKey();
                value = toEvict.getValue();
                map.remove(key);
                // 拿到键值对，计算出在容量中的相对长度，然后减去。
                size -= safeSizeOf(key, value);
                // 添加一次收回次数
                evictionCount++;
            }
            /*
             * 将最后一次删除的最少访问数据回调出去
             */
            entryRemoved(true, key, value, null);
        }
    }


    /**
     * 如果对应key的entry存在，则删除。
     */
    public final V remove(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V previous;
        synchronized (this) {
            // 移除对应 键值对 ，并将移除的value 存放在 previous
            previous = map.remove(key);
            if (previous != null) {
                // 拿到键值对，计算出在容量中的相对长度，然后减去。
                size -= safeSizeOf(key, previous);
            }
        }

        // 如果 Map 中存在 该key ，并且成功移除了
        if (previous != null) {
            /*
             * 会通知 自定义的 entryRemoved
             * previous 已经被删除了
             */
            entryRemoved(false, key, previous, null);
        }

        return previous;
    }


    /**
     * 1.当被回收或者删掉时调用。该方法当value被回收释放存储空间时被remove调用
     * 或者替换条目值时put调用，默认实现什么都没做。
     * 2.该方法没用同步调用，如果其他线程访问缓存时，该方法也会执行。
     * 3.evicted=true：如果该条目被删除空间 （表示 进行了trimToSize or remove）  evicted=false：put冲突后 或 get里成功create后
     * 导致
     * 4.newValue!=null，那么则被put()或get()调用。
     */
    protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
    }


    /**
     * 1.缓存丢失之后计算相应的key的value后调用。
     * 返回计算后的值，如果没有value可以计算返回null。
     * 默认的实现返回null。
     * 2.该方法没用同步调用，如果其他线程访问缓存时，该方法也会执行。
     * 3.当这个方法返回的时候，如果对应key的value存在缓存内，被创建的value将会被entryRemoved()释放或者丢弃。
     * 这情况可以发生在多线程在同一时间上请求相同key（导致多个value被创建了），或者单线程中调用了put()去创建一个
     * 相同key的value
     */
    protected V create(K key) {
        return null;
    }


    /**
     * 计算 该 键值对 的相对长度
     * 如果不覆写 sizeOf 实现特殊逻辑的话，默认长度是1。
     */
    private int safeSizeOf(K key, V value) {
        int result = sizeOf(key, value);
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + key + "=" + value);
        }
        return result;
    }


    /**
     * 返回条目在用户定义单位的大小。默认实现返回1，这样的大小是条目的数量并且最大的大小是条目的最大数量。
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
     * 对于这个缓存，如果不覆写sizeOf()方法，这个方法返回的是条目的在缓存中的数量。但是对于其他缓存，返回的是
     * 条目在缓存中大小的总和。
     */
    public synchronized final int size() {
        return size;
    }


    /**
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
     * 返回的次数{@link #get}返回null或需要一个新的要创建价值。
     */
    public synchronized final int missCount() {
        return missCount;
    }


    /**
     * 返回的次数{@link #create(Object)}返回一个值。
     */
    public synchronized final int createCount() {
        return createCount;
    }


    /**
     * 返回put的次数。
     */
    public synchronized final int putCount() {
        return putCount;
    }


    /**
     * 返回被收回的value数量。
     */
    public synchronized final int evictionCount() {
        return evictionCount;
    }


    /**
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
