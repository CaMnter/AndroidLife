package com.google.android.agera;

import android.support.annotation.NonNull;
import java.util.Arrays;

final class IdentityMultiMap<K, V> {

    @NonNull
    private static final Object[] NO_KEY_VALUES = new Object[0];

    /**
     * 用于存放 key 和 value
     * 格式：
     * index -> 0       1       2       3       4       5       6       7
     * value -> key0    value0  key1    value1  key2    value2  key3    value4
     */
    @NonNull
    private Object[] keysValues;


    IdentityMultiMap() {
        this.keysValues = NO_KEY_VALUES;
    }


    @NonNull Object[] getKeysValues() {
        return keysValues;
    }


    /**
     * 存放 key 和 value
     *
     * @param key key
     * @param value value
     * @return 是否存放成功
     */
    synchronized boolean addKeyValuePair(@NonNull final K key, @NonNull final V value) {
        int size = 0;
        int indexToAdd = -1;
        boolean hasValue = false;
        /*
         *  遍历存放的数据
         *  判断是否要存放数据
         *
         *  如果 key 没有冲突，会寻找到一个可以添加 key 和 value 的位置
         *  记录为 indexToAdd
         *  然后结束循环，后进行 key 和 value 的存放工作
         */
        for (int index = 0; index < keysValues.length; index += 2) {
            final Object keysValue = keysValues[index];
            /*
             * 如果该位置的 key 为 null
             * 表示可以添加
             * 记录可添加的位置
             */
            if (keysValue == null) {
                indexToAdd = index;
            }
            /*
             * 如果 key 冲突了
             * 直接移到下一位置，就是该 key 的 value 位
             * 判断 value 是否也冲突
             * 如果冲突了 就表示 此次 存储 key  value
             * 不需要操作
             * 记录为 hasValue = true
             */
            if (keysValue == key) {
                size++;
                if (keysValues[index + 1] == value) {
                    indexToAdd = index;
                    hasValue = true;
                }
            }
        }

        /*
         * 如果上面 找到了 可以添加的位置
         * 会根据这个 位置
         * 进行数组 扩容（ 就是 copy 原来 数组的数据，创建一个 新的数组 ）
         *
         * 为什么扩容？
         * 因为可能这个位置是数组数据的最后一位
         */
        if (indexToAdd == -1) {
            indexToAdd = keysValues.length;
            keysValues = Arrays.copyOf(keysValues, indexToAdd < 2 ? 2 : indexToAdd * 2);
        }
        /*
         * 如果 在上面 的 循环中没有发生冲突
         * 代表没有存放过 value（ 冲突的话，会在冲突 key 的后一位 存储 value 值 ）
         * 就是要同时存放 key 和 value
         */
        if (!hasValue) {
            keysValues[indexToAdd] = key;
            keysValues[indexToAdd + 1] = value;
        }
        // 如果修改了，则返回 true
        return size == 0;
    }


    /**
     * 找到 对应 key 和 value
     * 一定要两个一致
     * 然后删除掉
     *
     * @param key key
     * @param value value
     */
    synchronized void removeKeyValuePair(@NonNull final K key, @NonNull final V value) {
        for (int index = 0; index < keysValues.length; index += 2) {
            if (keysValues[index] == key && keysValues[index + 1] == value) {
                keysValues[index] = null;
                keysValues[index + 1] = null;
            }
        }
    }


    /**
     * 找到 对应 key
     * 删除 该 key 和 key 对应的 value
     *
     * @param key key
     * @return 是否删除成功
     */
    synchronized boolean removeKey(@NonNull final K key) {
        boolean removed = false;
        for (int index = 0; index < keysValues.length; index += 2) {
            if (keysValues[index] == key) {
                keysValues[index] = null;
                keysValues[index + 1] = null;
                removed = true;
            }
        }
        return removed;
    }
}
