package com.camnter.gradle.plugin.toytime;

import java.util.concurrent.TimeUnit;

/**
 * Copy from gradle 4.1
 */
public class TrueTimeProvider implements TimeProvider {
    public TrueTimeProvider() {
    }


    public long getCurrentTime() {
        return System.currentTimeMillis();
    }


    public long getCurrentTimeForDuration() {
        return TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    }
}
