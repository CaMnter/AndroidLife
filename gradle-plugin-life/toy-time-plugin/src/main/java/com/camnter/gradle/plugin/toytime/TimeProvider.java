package com.camnter.gradle.plugin.toytime;

import java.io.Serializable;

/**
 * Copy from gradle 4.1
 */
public interface TimeProvider extends Serializable {
    long getCurrentTime();

    long getCurrentTimeForDuration();
}
