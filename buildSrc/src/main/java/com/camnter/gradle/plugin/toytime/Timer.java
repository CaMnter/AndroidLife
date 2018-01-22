package com.camnter.gradle.plugin.toytime;

/**
 * Copy from gradle 4.1
 */
public interface Timer {
    String getElapsed();

    long getElapsedMillis();

    void reset();
}
