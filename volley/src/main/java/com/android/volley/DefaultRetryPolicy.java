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

package com.android.volley;

/**
 * Default retry policy for requests.
 */

/*
 * DefaultRetryPolicy 继承自 RetryPolicy
 *
 * 注意的地方就是：
 * mBackoffMultiplier 和 DEFAULT_BACKOFF_MULT 用于设置 退避乘数
 * 跟 "指数退避" 有关
 */
public class DefaultRetryPolicy implements RetryPolicy {
    /** The current timeout in milliseconds. */
    // 超时时间
    private int mCurrentTimeoutMs;

    /** The current retry count. */
    // 重试次数
    private int mCurrentRetryCount;

    /** The maximum number of attempts. */
    // 最大重试次数
    private final int mMaxNumRetries;

    /** The backoff multiplier for the policy. */
    // 退避乘数，可以用来实现 "指数退避"
    private final float mBackoffMultiplier;

    /** The default socket timeout in milliseconds */
    // 默认的 socket 超时时间是 2.5s
    public static final int DEFAULT_TIMEOUT_MS = 2500;

    /** The default number of retries */
    // 默认的最大重试次数是 1
    public static final int DEFAULT_MAX_RETRIES = 1;

    /** The default backoff multiplier */
    // 默认 退避乘数
    public static final float DEFAULT_BACKOFF_MULT = 1f;


    /**
     * Constructs a new retry policy using the default timeouts.
     */
    /*
     * 默认无参构造方法
     *
     * mCurrentTimeoutMs = DEFAULT_TIMEOUT_MS = 2500
     * mMaxNumRetries = DEFAULT_MAX_RETRIES = 1
     * mBackoffMultiplier = DEFAULT_BACKOFF_MULT = 1f
     */
    public DefaultRetryPolicy() {
        this(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULT);
    }


    /**
     * Constructs a new retry policy.
     *
     * @param initialTimeoutMs The initial timeout for the policy.
     * @param maxNumRetries The maximum number of retries.
     * @param backoffMultiplier Backoff multiplier for the policy.
     */
    /*
     * 需要传入：
     * 1.超时时间
     * 2.最大重试次数
     * 3.退避乘数
     */
    public DefaultRetryPolicy(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier) {
        mCurrentTimeoutMs = initialTimeoutMs;
        mMaxNumRetries = maxNumRetries;
        mBackoffMultiplier = backoffMultiplier;
    }


    /**
     * Returns the current timeout.
     */
    // 获取 超时时间
    @Override public int getCurrentTimeout() {
        return mCurrentTimeoutMs;
    }


    /**
     * Returns the current retry count.
     */
    // 获取 重试次数
    @Override public int getCurrentRetryCount() {
        return mCurrentRetryCount;
    }


    /**
     * Returns the backoff multiplier for the policy.
     */
    // 获取 退避乘数
    public float getBackoffMultiplier() {
        return mBackoffMultiplier;
    }


    /**
     * Prepares for the next retry by applying a backoff to the timeout.
     *
     * @param error The error code of the last attempt.
     */
    // 重试
    @Override public void retry(VolleyError error) throws VolleyError {
        // 重试次数 + 1
        mCurrentRetryCount++;
        // 根据 退避乘数 重新设置 超时时间
        mCurrentTimeoutMs += (mCurrentTimeoutMs * mBackoffMultiplier);
        // 判断是否能重试
        if (!hasAttemptRemaining()) {
            // 不能重试 抛出异常
            throw error;
        }
    }


    /**
     * Returns true if this policy has attempts remaining, false otherwise.
     */
    /*
     * true ：表示 没有超过最大重试次数，还能重试
     * false ：表示 超过最大重试次数，不能重试
     */
    protected boolean hasAttemptRemaining() {
        return mCurrentRetryCount <= mMaxNumRetries;
    }
}
