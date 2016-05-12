/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.os.SystemClock;
import com.android.volley.Cache;
import com.android.volley.VolleyLog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache implementation that caches files directly onto the hard disk in the specified
 * directory. The default disk usage size is 5MB, but is configurable.
 */

/*
 * DiskBasedCache 是 Cache 的实现类
 * 用于将保存缓存文件在硬盘上的指定目录中
 * 默认的缓存大小是 5MB
 * 缓存大小是可以手动配置的
 */
public class DiskBasedCache implements Cache {

    /** Map of the Key, CacheHeader pairs */
    /*
     * 实例化了一个 LinkedHashMap，accessOrder 设置为 true
     * 即，按照访问顺序对数据进行排序，就是 LRU 的简单时间
     * 容量 initialCapacity 为默认值 16，虽然手动写了 16
     * 负载因子 loadFactor 默认值 0.75f，虽然手动写了 0.75
     * 负载因子是 当容量超过这个百分比就会扩容，0.75f的话，就是容量超过75%就会扩容
     *
     * CacheHeader 缓存
     * 这里的 CacheHeader 缓存，在 initialize() 的时候
     * 会给 CacheHeader 缓存的内容进行添加，把文件缓存的内容映射过来
     */
    private final Map<String, CacheHeader> mEntries = new LinkedHashMap<String, CacheHeader>(16,
            .75f, true);

    /** Total amount of space currently used by the cache in bytes. */
    // 缓存的数量，以一个 CacheHeader 为一个单位
    private long mTotalSize = 0;

    /** The root directory to use for the cache. */
    // 缓存文件目录
    private final File mRootDirectory;

    /** The maximum size of the cache in bytes. */
    // 缓存最大的 byte 容量
    private final int mMaxCacheSizeInBytes;

    /** Default maximum disk usage in bytes. */
    // 默认最大的缓存容量 5MB
    private static final int DEFAULT_DISK_USAGE_BYTES = 5 * 1024 * 1024;

    /** High water mark percentage for the cache */
    // 一个负载因子的概念，如果容量 90%，缓存就要进行扩容
    private static final float HYSTERESIS_FACTOR = 0.9f;

    /** Magic number for current version of cache file format. */
    /*
     * 这个很有意思
     *
     * 写入文件后
     * 这个数值用于标识 后面的内容是一个 CacheHeader 的内容
     * 前面的内容也是一个 CacheHeader 的内容
     * 相当于一个分割标识
     * 这个标识，可以说是一个 CacheHeader 的开始
     */
    private static final int CACHE_MAGIC = 0x20150306;


    /**
     * Constructs an instance of the DiskBasedCache at the specified directory.
     *
     * @param rootDirectory The root directory of the cache.
     * @param maxCacheSizeInBytes The maximum size of the cache in bytes.
     */
    /*
     * 构造方法
     * 需要传入
     * 1.目录
     * 2.最大容量
     */
    public DiskBasedCache(File rootDirectory, int maxCacheSizeInBytes) {
        mRootDirectory = rootDirectory;
        mMaxCacheSizeInBytes = maxCacheSizeInBytes;
    }


    /**
     * Constructs an instance of the DiskBasedCache at the specified directory using
     * the default maximum cache size of 5MB.
     *
     * @param rootDirectory The root directory of the cache.
     */
    /*
     * 构造方法
     * 需要传入：目录
     * 最大容量默认 DEFAULT_DISK_USAGE_BYTES = 5MB
     */
    public DiskBasedCache(File rootDirectory) {
        this(rootDirectory, DEFAULT_DISK_USAGE_BYTES);
    }


    /**
     * Clears the cache. Deletes all cached files from disk.
     */
    /*
     * 清空磁盘上的文件缓存
     */
    @Override public synchronized void clear() {
        File[] files = mRootDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        mEntries.clear();
        mTotalSize = 0;
        VolleyLog.d("Cache cleared.");
    }


    /**
     * Returns the cache entry with the specified key if it exists, null otherwise.
     */
    /*
     * 根据 key 拿一条缓存
     * 1. 从内存中，拿缓存，不存在就 return。因为内存的数据会在 DiskBasedCache 初始化的时候，
     *    将文件缓存中的内容映射过来。所以内存找不到的话，文件缓存也不能找到。
     * 2. 存在的话，根据这个 key 找到对应的文件缓存。读取文件，将读取到的 Entry 返回
     *
     * 这里即使 内容有 该 key 的缓存都不返回，直到找到对应的文件后，将文件内的 Entry 作为内容返回
     * 可见，文件缓存 就像 git 的 master 分支一样，一直保存一份最稳定的数据
     * 并且等待 develop （ 内存缓存 ）的更新
     */
    @Override public synchronized Entry get(String key) {
        CacheHeader entry = mEntries.get(key);
        // if the entry does not exist, return.
        if (entry == null) {
            return null;
        }

        // 根据这个 key  找到文件
        File file = getFileForKey(key);
        CountingInputStream cis = null;
        /*
         * 以下 读取这个文件
         * 并且 读取的数据封装 一个 Entry
         * 进行返回
         */
        try {
            cis = new CountingInputStream(new BufferedInputStream(new FileInputStream(file)));
            CacheHeader.readHeader(cis); // eat header
            byte[] data = streamToBytes(cis, (int) (file.length() - cis.bytesRead));
            return entry.toCacheEntry(data);
        } catch (IOException e) {
            VolleyLog.d("%s: %s", file.getAbsolutePath(), e.toString());
            remove(key);
            return null;
        } finally {
            if (cis != null) {
                try {
                    cis.close();
                } catch (IOException ioe) {
                    return null;
                }
            }
        }
    }


    /**
     * Initializes the DiskBasedCache by scanning for all files currently in the
     * specified root directory. Creates the root directory if necessary.
     */
    /*
     * 缓存初始化操作：
     * 1. 判断缓存目录是否存在，不存在则创建一系列文件夹，然后返回
     * 2. 存在缓存文件，开始读取缓存文件内容。每一个缓存文件内容对应一个 CacheHeader
     */
    @Override public synchronized void initialize() {
        // 判断缓存目录是否存在，不存在则创建一系列文件夹，然后返回
        if (!mRootDirectory.exists()) {
            if (!mRootDirectory.mkdirs()) {
                VolleyLog.e("Unable to create cache dir %s", mRootDirectory.getAbsolutePath());
            }
            return;
        }

        // 拿到这堆缓存文件
        File[] files = mRootDirectory.listFiles();
        if (files == null) {
            return;
        }
        // 开始读取缓存文件内容。每一个缓存文件内容对应一个 CacheHeader
        for (File file : files) {
            BufferedInputStream fis = null;
            try {
                fis = new BufferedInputStream(new FileInputStream(file));
                CacheHeader entry = CacheHeader.readHeader(fis);
                entry.size = file.length();
                putEntry(entry.key, entry);
            } catch (IOException e) {
                if (file != null) {
                    file.delete();
                }
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }


    /**
     * Invalidates an entry in the cache.
     *
     * @param key Cache key
     * @param fullExpire True to fully expire the entry, false to soft expire
     */
    /*
     * 给一个缓存 标记 无效
     * 表示这个缓存需要更新了
     * 这里修改了 缓存的过期时间和刷新时间
     */
    @Override public synchronized void invalidate(String key, boolean fullExpire) {
        Entry entry = get(key);
        if (entry != null) {
            entry.softTtl = 0;
            if (fullExpire) {
                entry.ttl = 0;
            }
            put(key, entry);
        }
    }


    /**
     * Puts the entry with the specified key into the cache.
     */
    /*
     * 写缓存
     * 1. 判断容量够不够写缓存，不够的话会做相应的处理（ 最少访问的数据会被删除 ），腾出空间
     * 2. 开始将 Entry 封装成一个 CacheHeader，写到一个文件里
     */
    @Override public synchronized void put(String key, Entry entry) {
        // 判断容量够不够写缓存，不够的话会做相应的处理（ 最少访问的数据会被删除 ），腾出空间
        pruneIfNeeded(entry.data.length);
        // 根据文件 key 拿到对应的 File
        File file = getFileForKey(key);
        // 开始将 Entry 封装成一个 CacheHeader，写到一个文件里
        try {
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
            CacheHeader e = new CacheHeader(key, entry);
            boolean success = e.writeHeader(fos);
            if (!success) {
                fos.close();
                VolleyLog.d("Failed to write header for %s", file.getAbsolutePath());
                throw new IOException();
            }
            fos.write(entry.data);
            fos.close();
            putEntry(key, e);
            return;
        } catch (IOException e) {
        }
        boolean deleted = file.delete();
        if (!deleted) {
            VolleyLog.d("Could not clean up file %s", file.getAbsolutePath());
        }
    }


    /**
     * Removes the specified key from the cache if it exists.
     */
    /*
     *  删除
     *  先删除文件缓存
     *  再删除内存缓存
     */
    @Override public synchronized void remove(String key) {
        boolean deleted = getFileForKey(key).delete();
        removeEntry(key);
        if (!deleted) {
            VolleyLog.d("Could not delete cache entry for key=%s, filename=%s", key,
                    getFilenameForKey(key));
        }
    }


    /**
     * Creates a pseudo-unique filename for the specified cache key.
     *
     * @param key The key to generate a file name for.
     * @return A pseudo-unique filename.
     */
    /*
     * 根据缓存 key
     * 获取其 对应的 文件缓存的名称
     */
    private String getFilenameForKey(String key) {
        int firstHalfLength = key.length() / 2;
        String localFilename = String.valueOf(key.substring(0, firstHalfLength).hashCode());
        localFilename += String.valueOf(key.substring(firstHalfLength).hashCode());
        return localFilename;
    }


    /**
     * Returns a file object for the given cache key.
     */
    /*
     * 根据缓存 key
     * 获取其 对应的 文件
     */
    public File getFileForKey(String key) {
        return new File(mRootDirectory, getFilenameForKey(key));
    }


    /**
     * Prunes the cache to fit the amount of bytes specified.
     *
     * @param neededSpace The amount of bytes we are trying to fit into the cache.
     */
    /*
     * 如果
     * 1.当前的容量 + 需要放入缓存容量 < 最大容量的时候 就返回
     * 2.当前的容量 + 需要放入缓存容量 > 最大容量的时候：
     *   需要删除 最少访问的数据最为代价，一直删除到能容纳此次需要添加的缓存的容量为止
     */
    private void pruneIfNeeded(int neededSpace) {
        if ((mTotalSize + neededSpace) < mMaxCacheSizeInBytes) {
            return;
        }
        if (VolleyLog.DEBUG) {
            VolleyLog.v("Pruning old cache entries.");
        }

        long before = mTotalSize;
        int prunedFiles = 0;
        long startTime = SystemClock.elapsedRealtime();
        /*
         * 遍历内存缓存的内容
         * 由于这个 Map 是 LinkedHashMap 并且 accessOrder 设置为 true
         * 所以这个 Map 实现了 LRU 的机制，根据访问顺序，数据会进行调整，最少访问的数据放在首部
         */
        Iterator<Map.Entry<String, CacheHeader>> iterator = mEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            // 首部的数据 是 最少访问的
            Map.Entry<String, CacheHeader> entry = iterator.next();
            CacheHeader e = entry.getValue();
            // 缓存溢出的时候，优先被删除，腾出空间
            boolean deleted = getFileForKey(e.key).delete();
            // 删除后 总容量进行调整
            if (deleted) {
                mTotalSize -= e.size;
            } else {
                VolleyLog.d("Could not delete cache entry for key=%s, filename=%s", e.key,
                        getFilenameForKey(e.key));
            }
            iterator.remove();
            prunedFiles++;

            /*
             * 此时，判断删除一个 最少访问数据后的的容量 + 需要添加缓存的容量 是否 小于 扩容容量
             * 这里 扩容容量 = 最大容量+负载因子 = ( 最大容量*90% )
             * 超了的话 继续删除下一个最少访问的数据（ 还是首部数据 ）
             * 没超的话 表示已经腾出足够空间，足够容纳 需要添加的缓存
             */
            if ((mTotalSize + neededSpace) < mMaxCacheSizeInBytes * HYSTERESIS_FACTOR) {
                break;
            }
        }

        if (VolleyLog.DEBUG) {
            VolleyLog.v("pruned %d files, %d bytes, %d ms", prunedFiles, (mTotalSize - before),
                    SystemClock.elapsedRealtime() - startTime);
        }
    }


    /**
     * Puts the entry with the specified key into the cache.
     *
     * @param key The key to identify the entry by.
     * @param entry The entry to cache.
     */
    /*
     * 添加缓存
     * 1. key 不存在直接添加
     * 2. 如果内存中存在 这个与此次添加缓存相同的 key 则替换：
     *    总长度上 删除 旧数据长度，添加新数据长度，完成
     */
    private void putEntry(String key, CacheHeader entry) {
        if (!mEntries.containsKey(key)) {
            mTotalSize += entry.size;
        } else {
            CacheHeader oldEntry = mEntries.get(key);
            mTotalSize += (entry.size - oldEntry.size);
        }
        mEntries.put(key, entry);
    }


    /**
     * Removes the entry identified by 'key' from the cache.
     */
    /*
     * 根据 key 删除一条缓存
     * 这里的话 只删除了内存中（ Map<String, CacheHeader> ）的缓存
     */
    private void removeEntry(String key) {
        CacheHeader entry = mEntries.get(key);
        if (entry != null) {
            // 对总缓存长度处理
            mTotalSize -= entry.size;
            // 内存中移除 该缓存
            mEntries.remove(key);
        }
    }


    /**
     * Reads the contents of an InputStream into a byte[].
     */
    /*
     * 根据一个 长度 和 流
     * 去读取一个对应长度的 byte[]
     */
    private static byte[] streamToBytes(InputStream in, int length) throws IOException {
        byte[] bytes = new byte[length];
        /*
         * 标记读取上来的的值
         * 下面用于判断是否为 -1，代表这个流读取完毕了
         */
        int count;
        int pos = 0;
        /*
         * in.read(bytes, pos, length - pos)
         * 读取上来的值
         * 进行倒着放入这个 byte[] 内，不是从 byte[0] 开始放
         * 1010 1111
         * 先读取上来的肯定是 1010 ，要放入 byte[] 只能先放 byte[高位]
         */
        while (pos < length && ((count = in.read(bytes, pos, length - pos)) != -1)) {
            pos += count;
        }
        if (pos != length) {
            throw new IOException("Expected " + length + " bytes, read " + pos + " bytes");
        }
        return bytes;
    }


    /**
     * Handles holding onto the cache headers for an entry.
     */
    // Visible for testing.
    /*
     *  内部静态类 CacheHeader
     *  包含了 Cache.Entry 的 Response 数据 + key
     */
    static class CacheHeader {
        /**
         * The size of the data identified by this CacheHeader. (This is not
         * serialized to disk.
         */
        /*
         * 缓存 Response contents 的数据长度
         * 由于 Response contents 到 Cache 就是 一个 byte[]
         * 这里的 size = byte[].length
         */
        public long size;

        /** The key that identifies the cache entry. */
        // 这个缓存数据对应的 key
        public String key;

        /** ETag for cache coherence. */
        // 缓存 Response Header "ETag"
        public String etag;

        /** Date of this response as reported by the server. */
        // 缓存 Response Header "Date"
        public long serverDate;

        /** The last modified date for the requested object. */
        // 缓存 Response Header Last-Modified
        public long lastModified;

        /** TTL for this record. */
        // 缓存 计算好的 过期时间
        public long ttl;

        /** Soft TTL for this record. */
        // 缓存 计算好的 刷新时间
        public long softTtl;

        /** Headers from the response resulting in this cache entry. */
        // 缓存 Response 对应的 Request Header 数据
        public Map<String, String> responseHeaders;


        // 注：不允许使用无参构造方法
        private CacheHeader() { }


        /**
         * Instantiates a new CacheHeader object
         *
         * @param key The key that identifies the cache entry
         * @param entry The cache entry.
         */

        /*
         * 屏蔽了无参构造方法，唯一开放的构造方法
         * 需要 传入
         * 1.这个缓存对应的 key
         * 2.这个缓存对应的 Response 数据 Cache.Entry
         */
        public CacheHeader(String key, Entry entry) {
            this.key = key;
            this.size = entry.data.length;
            this.etag = entry.etag;
            this.serverDate = entry.serverDate;
            this.lastModified = entry.lastModified;
            this.ttl = entry.ttl;
            this.softTtl = entry.softTtl;
            this.responseHeaders = entry.responseHeaders;
        }


        /**
         * Reads the header off of an InputStream and returns a CacheHeader object.
         *
         * @param is The InputStream to read from.
         * @throws IOException
         */
        /*
         * 读取一条 CacheHeader
         * 1. 先拿到设定好的 标识 CACHE_MAGIC，写的时候也会先写入这个标识
         * 2. 跳过标识之后 就是这个 CacheHeader 的内容了
         */
        public static CacheHeader readHeader(InputStream is) throws IOException {
            CacheHeader entry = new CacheHeader();
            // 拿到 标识
            int magic = readInt(is);
            // 根据这个标识 判断是否存在 CacheHeader
            if (magic != CACHE_MAGIC) {
                // don't bother deleting, it'll get pruned eventually
                throw new IOException();
            }
            entry.key = readString(is);
            entry.etag = readString(is);
            if (entry.etag.equals("")) {
                entry.etag = null;
            }
            entry.serverDate = readLong(is);
            entry.lastModified = readLong(is);
            entry.ttl = readLong(is);
            entry.softTtl = readLong(is);
            entry.responseHeaders = readStringStringMap(is);

            return entry;
        }


        /**
         * Creates a cache entry for the specified data.
         */
        /*
         * 一个缓存数据 byte[] -> Entry
         */
        public Entry toCacheEntry(byte[] data) {
            Entry e = new Entry();
            e.data = data;
            e.etag = etag;
            e.serverDate = serverDate;
            e.lastModified = lastModified;
            e.ttl = ttl;
            e.softTtl = softTtl;
            e.responseHeaders = responseHeaders;
            return e;
        }


        /**
         * Writes the contents of this CacheHeader to the specified OutputStream.
         */
        /*
         * 写入一条缓存 CacheHeader
         * 先写入一个 CACHE_MAGIC 标识，标记着 CacheHeader 数据的开始
         * 然后开始写 CacheHeader 的内容
         */
        public boolean writeHeader(OutputStream os) {
            try {
                // 先写入一个 CACHE_MAGIC 标识
                writeInt(os, CACHE_MAGIC);
                writeString(os, key);
                writeString(os, etag == null ? "" : etag);
                writeLong(os, serverDate);
                writeLong(os, lastModified);
                writeLong(os, ttl);
                writeLong(os, softTtl);
                writeStringStringMap(responseHeaders, os);
                os.flush();
                return true;
            } catch (IOException e) {
                VolleyLog.d("%s", e.toString());
                return false;
            }
        }
    }

    /*
     * 静态内部类 CountingInputStream
     * 特点记录行为 read 进行的次数
     */
    private static class CountingInputStream extends FilterInputStream {
        private int bytesRead = 0;


        private CountingInputStream(InputStream in) {
            super(in);
        }


        @Override public int read() throws IOException {
            int result = super.read();
            if (result != -1) {
                bytesRead++;
            }
            return result;
        }


        @Override public int read(byte[] buffer, int offset, int count) throws IOException {
            int result = super.read(buffer, offset, count);
            if (result != -1) {
                bytesRead += result;
            }
            return result;
        }
    }

    /*
     * Homebrewed simple serialization system used for reading and writing cache
     * headers on disk. Once upon a time, this used the standard Java
     * Object{Input,Output}Stream, but the default implementation relies heavily
     * on reflection (even for standard types) and generates a ton of garbage.
     *
     */
    /**
     * Homebrewed 版本的简单序列化系统上进行读/写缓存的磁盘操作，使用传统的 Java Object{Input,Output}Stream
     * 但默认的实现在很大程度上依赖于反射（即使是标准类型），并产生一吨垃圾
     */

    /**
     * 以下读/写基本类型的操作 ( int、long )
     * 都进行了位运算处理
     */

    /**
     * Simple wrapper around {@link InputStream#read()} that throws EOFException
     * instead of returning -1.
     */
    private static int read(InputStream is) throws IOException {
        int b = is.read();
        if (b == -1) {
            throw new EOFException();
        }
        return b;
    }


    /**
     * 0xff = 1111 1111 = 255
     *
     * 先不移动 进行 位与( & ) 运算
     * 平移8位 进行 位与( & ) 运算
     * 平移16位 进行 位与( & ) 运算
     * 平移24位 进行 位与( & ) 运算
     *
     * 如果是一个32位的2^30：
     * 0100 0000 0000 0000 | 0000 0000 0000 0000 | 0000 0000 0000 0000 | 0000 0000 0000 0000
     *
     * 第一次写入：0100 0000 0000 0000 | 0000 0000 0000 0000 | 0000 0000 0000 0000 | 0000 0000 0000 0000
     * 第二次写入：          0100 0000 0000 0000 | 0000 0000 0000 0000 | 0000 0000 0000 0000 | 0000 0000
     * 第三次写入：                                0100 0000 0000 0000 | 0000 0000 0000 0000 | 0000 0000
     * 第四次写入：                                                                            0100 0000
     *
     * @param os os
     * @param n n
     * @throws IOException
     */
    static void writeInt(OutputStream os, int n) throws IOException {
        os.write((n >> 0) & 0xff);
        os.write((n >> 8) & 0xff);
        os.write((n >> 16) & 0xff);
        os.write((n >> 24) & 0xff);
    }


    /**
     * @param is is
     * @return int
     * @throws IOException
     */
    static int readInt(InputStream is) throws IOException {
        int n = 0;
        n |= (read(is) << 0);
        n |= (read(is) << 8);
        n |= (read(is) << 16);
        n |= (read(is) << 24);
        return n;
    }


    static void writeLong(OutputStream os, long n) throws IOException {
        os.write((byte) (n >>> 0));
        os.write((byte) (n >>> 8));
        os.write((byte) (n >>> 16));
        os.write((byte) (n >>> 24));
        os.write((byte) (n >>> 32));
        os.write((byte) (n >>> 40));
        os.write((byte) (n >>> 48));
        os.write((byte) (n >>> 56));
    }


    static long readLong(InputStream is) throws IOException {
        long n = 0;
        n |= ((read(is) & 0xFFL) << 0);
        n |= ((read(is) & 0xFFL) << 8);
        n |= ((read(is) & 0xFFL) << 16);
        n |= ((read(is) & 0xFFL) << 24);
        n |= ((read(is) & 0xFFL) << 32);
        n |= ((read(is) & 0xFFL) << 40);
        n |= ((read(is) & 0xFFL) << 48);
        n |= ((read(is) & 0xFFL) << 56);
        return n;
    }


    /**
     * 缓存一个 String
     * 先缓存 这个 String.length
     * 然后把 String 拆放在 byte[] 去 缓存
     *
     * @param os os
     * @param s s
     * @throws IOException
     */
    static void writeString(OutputStream os, String s) throws IOException {
        byte[] b = s.getBytes("UTF-8");
        writeLong(os, b.length);
        os.write(b, 0, b.length);
    }


    /**
     * 读取一个 String
     * 先 readLong 读取 String.length
     * 然后通过这个 String.length 去读取 byte[]，这就是为什么要缓存 这个 String.length
     *
     * @param is is
     * @return String
     * @throws IOException
     */
    static String readString(InputStream is) throws IOException {
        int n = (int) readLong(is);
        byte[] b = streamToBytes(is, n);
        return new String(b, "UTF-8");
    }


    /**
     * 缓存  Map<String, String>
     *
     * 先缓存 map 的长度
     * 然后就是一个key 写入 一个 value写入
     * ...
     *
     * @param map map
     * @param os os
     * @throws IOException
     */
    static void writeStringStringMap(Map<String, String> map, OutputStream os) throws IOException {
        if (map != null) {
            writeInt(os, map.size());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                writeString(os, entry.getKey());
                writeString(os, entry.getValue());
            }
        } else {
            writeInt(os, 0);
        }
    }


    /**
     * 按照上面的 缓存原则
     *
     * 第一个肯定用 readInt(...) 去读取，作为 map 的长度
     * 然后根据这个长度值 去决定调用 多少次 readString(...)，这就是为什么前面要缓存 map.size()
     * 然后就把 key value 读取上来
     *
     * @param is is
     * @return Map<String, String>
     * @throws IOException
     */
    static Map<String, String> readStringStringMap(InputStream is) throws IOException {
        int size = readInt(is);
        Map<String, String> result = (size == 0)
                                     ? Collections.<String, String>emptyMap()
                                     : new HashMap<String, String>(size);
        for (int i = 0; i < size; i++) {
            String key = readString(is).intern();
            String value = readString(is).intern();
            result.put(key, value);
        }
        return result;
    }
}
