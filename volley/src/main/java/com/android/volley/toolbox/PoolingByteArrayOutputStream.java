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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A variation of {@link java.io.ByteArrayOutputStream} that uses a pool of byte[] buffers instead
 * of always allocating them fresh, saving on heap churn.
 */

/*
 * PoolingByteArrayOutputStream 继承了 原声的 ByteArrayOutputStream
 * 使用了 ByteArrayPool 回收利用一些 byte[]
 * 防止了 byte[] 的重复内存分配和回收
 */
public class PoolingByteArrayOutputStream extends ByteArrayOutputStream {
    /**
     * If the {@link #PoolingByteArrayOutputStream(ByteArrayPool)} constructor is called, this is
     * the default size to which the underlying byte array is initialized.
     */
    /*
     * 写入 byte[] （ ByteArrayOutputStream.buf ）的默认长度
     * 从下面源码（ PoolingByteArrayOutputStream(ByteArrayPool pool, int size) ）中
     * 也能看得出 是 ByteArrayOutputStream.buf 最小长度
     */
    private static final int DEFAULT_SIZE = 256;

    // byte[] 缓存池
    private final ByteArrayPool mPool;


    /**
     * Constructs a new PoolingByteArrayOutputStream with a default size. If more bytes are written
     * to this instance, the underlying byte array will expand.
     */
    /*
     * PoolingByteArrayOutputStream 的构造方法
     * 需要一个 ByteArrayPool 缓存池
     */
    public PoolingByteArrayOutputStream(ByteArrayPool pool) {
        this(pool, DEFAULT_SIZE);
    }


    /**
     * Constructs a new {@code ByteArrayOutputStream} with a default size of {@code size} bytes. If
     * more than {@code size} bytes are written to this instance, the underlying byte array will
     * expand.
     *
     * @param size initial size for the underlying byte array. The value will be pinned to a
     * default
     * minimum size.
     */
    /*
     * PoolingByteArrayOutputStream 的构造方法
     * 需要
     * 一个 ByteArrayPool 缓存池
     * 定义一个 写入 byte[] 的长度 size：“表示需要 size 这么长的 byte[]”，但是不能小于 256
     */
    public PoolingByteArrayOutputStream(ByteArrayPool pool, int size) {
        mPool = pool;
        /*
         * buf 是 ByteArrayOutputStream 的 buf 属性
         *
         * size 与 DEFAULT_SIZE=256 之间的最大值，size 如果小于256就会失效
         * 然后，去 ByteArrayPool 缓存池中找这么长的 byte[]，如果有可以再次利用的 byte[] 会返回，没有会 new 一个
         * byte[] 赋值给 buf，
         * 1.write(...)的内容会保存到 buf 内
         * 2.在后面的 extend(...)的时候会用到
         */
        buf = mPool.getBuf(Math.max(size, DEFAULT_SIZE));
    }


    @Override public void close() throws IOException {
        // 在 PoolingByteArrayOutputStream.close() 的时候，将此次的 buf byte[] 缓存到缓存池中，以供下次利用
        mPool.returnBuf(buf);
        buf = null;
        super.close();
    }


    @Override public void finalize() {
        // 在 PoolingByteArrayOutputStream.finalize() 的时候，将此次的 buf byte[] 缓存到缓存池中，以供下次利用
        mPool.returnBuf(buf);
    }


    /**
     * Ensures there is enough space in the buffer for the given number of additional bytes.
     *
     * ByteArrayOutputStream：
     * private void expand(int i) {
     * Can the buffer handle @i more bytes, if not expand it
     * if (count + i <= buf.length) {
     * return;
     * }
     *
     * byte[] newbuf = new byte[(count + i) * 2];
     * System.arraycopy(buf, 0, newbuf, 0, count);
     * buf = newbuf;
     * }
     */
    private void expand(int i) {
        /* Can the buffer handle @i more bytes, if not expand it */
        /*
         * count 是 ByteArrayOutputStream 的 count 属性
         * count 表示：要写的字节数
         *
         * 只有原本要写的字节数 + 现在要写的字节数 大于 已经从 ByteArrayPool 缓存池 拿到的 可再次利用 byte[] 的情况下
         * 才需要 重新申请一个足够长 byte[]
         */
        if (count + i <= buf.length) {
            return;
        }
        /*
         * 申请一个更大的 byte[]，长度= ( 原本要写的字节数 + 现在要写的字节数 ) * 2
         * 还是先去看看 ByteArrayPool 缓存池 有没有这么长的 byte[]
         * 即使没有，ByteArrayPool 缓存池也会帮助申请一个这么长的 byte[]
         */
        byte[] newbuf = mPool.getBuf((count + i) * 2);
        /*
         * 复制 buf 从index＝0 开始 长度为 count 的内容
         * 到 newbuf 从index＝0 开始 长度为 count 的地方
         *
         * 为什么要赋值 buf 的内容？
         * 因为 ByteArrayOutputStream.write(...) 的时候，要写的字节都保存在 buf 内
         */
        System.arraycopy(buf, 0, newbuf, 0, count);
        /*
         * 由于 原本的 buf 不能用，用的是新申请的 newbuf
         * 为了不想浪费 buf，所以将 buf 暂且保存在缓存池内，没准哪天，需要这么长的 byte[] 呢？
         */
        mPool.returnBuf(buf);
        // 重置 此时的 buf（ 要写入的 byte[] ）
        buf = newbuf;
    }


    /**
     * 执行 expand(len)
     * 将指定 byte 数组中从偏移量 offset 开始的 len 个字节写入此 byte 数组输出流
     *
     * @param buffer byte[] 数据
     * @param offset 数据的初始偏移量
     * @param len 要写入的字节数
     */
    @Override public synchronized void write(byte[] buffer, int offset, int len) {
        expand(len);
        super.write(buffer, offset, len);
    }


    /**
     * 执行 expand(len)
     * 将指定的字节写入此 byte 数组输出流。
     *
     * @param oneByte 要写入的字节
     */
    @Override public synchronized void write(int oneByte) {
        expand(1);
        super.write(oneByte);
    }
}
