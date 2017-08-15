package com.alibaba.android.arouter.thread;

import java.util.concurrent.CountDownLatch;

/**
 * As its name.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/29 15:48
 *
 * 定义一个可取消阻塞的自定义 CountDownLatch，叫 CancelableCountDownLatch
 * 实质上，就是一个 while 强行把 count 变为 0，解开线程阻塞
 */
public class CancelableCountDownLatch extends CountDownLatch {
    /**
     * Constructs a {@code CountDownLatch} initialized with the given count.
     *
     * @param count the number of times {@link #countDown} must be invoked
     * before threads can pass through {@link #await}
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public CancelableCountDownLatch(int count) {
        super(count);
    }


    public void cancel() {
        while (getCount() > 0) {
            countDown();
        }
    }
}
