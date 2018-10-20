package com.camnter.rxjava2.cache;

import android.support.annotation.NonNull;

/**
 * @author CaMnter
 */

public class CacheException extends Exception {

    public CacheException(@NonNull final Class<?> clazz) {
        super("「" + clazz.getSimpleName() + "」" + "");
    }


    public CacheException(@NonNull final Class<?> clazz, @NonNull final Exception exception) {
        this(clazz, exception.getMessage());
    }


    public CacheException(@NonNull final Class<?> clazz, @NonNull final String message) {
        super("「" + clazz.getSimpleName() + "」" + message);
    }

}
