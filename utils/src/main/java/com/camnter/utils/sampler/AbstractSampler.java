package com.camnter.utils.sampler;

import android.os.Handler;
import com.camnter.utils.thread.HandlerThreadFactory;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AndroidPerformanceMonitor 摘抄并修改的源码
 * 采样数据的 抽象层
 * https://github.com/markzhai/AndroidPerformanceMonitor/blob/master/blockcanary-analyzer/src/main/java/com/github/moduth/blockcanary/HandlerThreadFactory.java
 *
 * @author CaMnter
 */
abstract class AbstractSampler {

    private static final int DEFAULT_SAMPLE_INTERVAL = 300;

    private static final HandlerThreadFactory.HandlerThreadWrapper LOOP_THREAD
        = new HandlerThreadFactory.HandlerThreadWrapper("loop");

    protected AtomicBoolean shouldSample = new AtomicBoolean(false);
    protected long sampleInterval;

    public static final String SEPARATOR = "\r\n";
    public static final SimpleDateFormat TIME_FORMATTER =
        new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            doSample();

            if (shouldSample.get()) {
                LOOP_THREAD.getHandler()
                    .postDelayed(runnable, sampleInterval);
            }
        }
    };


    public AbstractSampler(long sampleInterval) {
        if (0 == sampleInterval) {
            sampleInterval = DEFAULT_SAMPLE_INTERVAL;
        }
        this.sampleInterval = sampleInterval;
    }


    public void start(long delayMillis) {
        if (this.shouldSample.get()) {
            return;
        }
        this.shouldSample.set(true);

        final Handler handler = LOOP_THREAD.getHandler();
        handler.removeCallbacks(this.runnable);
        handler.postDelayed(this.runnable, delayMillis);
    }


    public void stop() {
        if (!this.shouldSample.get()) {
            return;
        }
        this.shouldSample.set(false);
        LOOP_THREAD.getHandler().removeCallbacks(this.runnable);
    }


    abstract void doSample();
}