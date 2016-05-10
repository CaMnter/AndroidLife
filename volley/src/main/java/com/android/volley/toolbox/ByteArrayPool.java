/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.toolbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * ByteArrayPool is a source and repository of <code>byte[]</code> objects. Its purpose is to
 * supply those buffers to consumers who need to use them for a short period of time and then
 * dispose of them. Simply creating and disposing such buffers in the conventional manner can
 * considerable heap churn and garbage collection delays on Android, which lacks good management of
 * short-lived heap objects. It may be advantageous to trade off some memory in the form of a
 * permanently allocated pool of buffers in order to gain heap performance improvements; that is
 * what this class does.
 * <p>
 * A good candidate user for this class is something like an I/O system that uses large temporary
 * <code>byte[]</code> buffers to copy data around. In these use cases, often the consumer wants
 * the buffer to be a certain minimum size to ensure good performance (e.g. when copying data
 * chunks
 * off of a stream), but doesn't mind if the buffer is larger than the minimum. Taking this into
 * account and also to maximize the odds of being able to reuse a recycled buffer, this class is
 * free to return buffers larger than the requested size. The caller needs to be able to gracefully
 * deal with getting buffers any size over the minimum.
 * <p>
 * If there is not a suitably-sized buffer in its recycling pool when a buffer is requested, this
 * class will allocate a new buffer and return it.
 * <p>
 * This class has no special ownership of buffers it creates; the caller is free to take a buffer
 * it receives from this pool, use it permanently, and never return it to the pool; additionally,
 * it is not harmful to return to this pool a buffer that was allocated elsewhere, provided there
 * are no other lingering references to it.
 * <p>
 * This class ensures that the total size of the buffers in its recycling pool never exceeds a
 * certain byte limit. When a buffer is returned that would cause the pool to exceed the limit,
 * least-recently-used buffers are disposed.
 */

/*
 * 这是一个 byte[] 缓存池
 * 用于 byte[] 的回收再利用，减少内存分配和回收。
 */
public class ByteArrayPool {

    /** The buffer pool, arranged both by last use and by buffer size */
    // 采用 LRU 的机制，最少使用的放在 index=0，最近使用的 放在 index=size()-1
    private List<byte[]> mBuffersByLastUse = new LinkedList<byte[]>();
    // 缓存 byte[] 缓存 List，采用根据 byte[].length 由小到大的方式排序
    private List<byte[]> mBuffersBySize = new ArrayList<byte[]>(64);

    /** The total size of the buffers in the pool */
    // 计算当前缓存池内的所有 byte[] 总长度之和
    private int mCurrentSize = 0;

    /**
     * The maximum aggregate size of the buffers in the pool. Old buffers are discarded to stay
     * under this limit.
     */
    // 缓存池内 byte[] 总长度之和的限制长度，经常拿来与 mCurrentSize 比较
    private final int mSizeLimit;

    /** Compares buffers by size */
    /*
     * 用于比较 byte[].length 的 Comparator
     * lhs.length - rhs.length = 0 ：说明 length 相等
     * lhs.length - rhs.length > 0 ：说明 lhs.length（前者）大
     * lhs.length - rhs.length < 0 ：说明 rhs.length（后者）大
     * 主要用于  Collections.binarySearch 二分查找时，进行的内部排序。
     */
    protected static final Comparator<byte[]> BUF_COMPARATOR = new Comparator<byte[]>() {
        @Override public int compare(byte[] lhs, byte[] rhs) {
            return lhs.length - rhs.length;
        }
    };


    /**
     * @param sizeLimit the maximum size of the pool, in bytes
     */
    // ByteArrayPool 构造方法，需要一个 总 byte[] 长度限制。
    public ByteArrayPool(int sizeLimit) {
        mSizeLimit = sizeLimit;
    }


    /**
     * Returns a buffer from the pool if one is available in the requested size, or allocates a new
     * one if a pooled one is not available.
     *
     * @param len the minimum size, in bytes, of the requested buffer. The returned buffer may be
     * larger.
     * @return a byte[] buffer is always returned.
     */
    /*
     * 根据 长度需求 len，获取一个 length=len 的 byte[] 缓存
     * 如果没有这样“规格”的 byte[]，就 new 一个返回
     */
    public synchronized byte[] getBuf(int len) {
        // 开始遍历 byte[] 缓存数组
        for (int i = 0; i < mBuffersBySize.size(); i++) {
            // 拿到每一个 byte[] 缓存
            byte[] buf = mBuffersBySize.get(i);
            // 如果长度符合了，进行取出 byte[] 缓存前的操作
            if (buf.length >= len) {
                // 先对 总长度 进行削减
                mCurrentSize -= buf.length;
                // 移除 byte[] 缓存组中的 对应 byte[] 缓存
                mBuffersBySize.remove(i);
                // 移除 byte[] LRU 组中的 对应 byte[] 缓存
                mBuffersByLastUse.remove(buf);
                // 最后，返回这个 byte[] 进行再次利用
                return buf;
            }
        }
        // for 循环走完，没返回的话，就是没有符合长度的 byte[]，只能 new 一个返回了。
        return new byte[len];
    }


    /**
     * Returns a buffer to the pool, throwing away old buffers if the pool would exceed its
     * allotted
     * size.
     *
     * @param buf the buffer to return to the pool.
     */
    // 将一个 byte[] 放入缓存池中
    public synchronized void returnBuf(byte[] buf) {
        // 如果 byte[] 为null 或者 byte[] 的长度超过了整个缓存池的总长限制
        if (buf == null || buf.length > mSizeLimit) {
            return;
        }
        // 放入 byte[] LRU 组中的末尾，对应了：最少使用的放在 index=0，最近使用的 放在 index=size()-1
        mBuffersByLastUse.add(buf);
        // 对 缓存组 mBuffersBySize 进行从小到大的排序后，二分查找此次缓存的 byte[] 所应该在的位置
        int pos = Collections.binarySearch(mBuffersBySize, buf, BUF_COMPARATOR);
        if (pos < 0) {
            pos = -pos - 1;
        }
        // 在对应位置插入此次缓存的 byte[]
        mBuffersBySize.add(pos, buf);
        // 总长度加上此次 byte[].length
        mCurrentSize += buf.length;
        // 由于加了数据，需要判断是否溢出，并可能进行溢出处理
        trim();
    }


    /**
     * Removes buffers from the pool until it is under its size limit.
     */
    private synchronized void trim() {
        // 如果数据溢出了
        while (mCurrentSize > mSizeLimit) {
            // 从 LRU 组内找出一个最近很少使用的 byte[]，这里也对应了：最少使用的放在 index=0，最近使用的 放在 index=size()-1
            byte[] buf = mBuffersByLastUse.remove(0);
            // 接着拿到从 LRU 组内删除的 byte[]，去缓存组内继续移除该 byte[]
            mBuffersBySize.remove(buf);
            // 总长度减去此次删除的 byte[].length
            mCurrentSize -= buf.length;
        }
    }
}
