package com.alibaba.android.arouter.base;

import java.util.TreeMap;

/**
 * TreeMap with unique key.
 *
 * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
 * @version 1.0
 * @since 2017/2/22 下午5:01
 *
 * 唯一值的 TreeMap
 * 只要存过一次 key value，下次再传入该 key 的 不同 value，会抛出 RuntimeException
 * 同一个 key 的 value，只会缓存一次
 */
public class UniqueKeyTreeMap<K, V> extends TreeMap<K, V> {
    private String tipText;


    public UniqueKeyTreeMap(String exceptionText) {
        super();

        tipText = exceptionText;
    }


    @Override
    public V put(K key, V value) {
        if (containsKey(key)) {
            throw new RuntimeException(String.format(tipText, key));
        } else {
            return super.put(key, value);
        }
    }
}
