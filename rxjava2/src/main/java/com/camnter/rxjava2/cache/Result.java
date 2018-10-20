package com.camnter.rxjava2.cache;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author CaMnter
 */

public class Result {

    public static final int RESULT_CODE_SUCCESS = 0x260;
    public static final int RESULT_CODE_FAILURE = 0x261;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ RESULT_CODE_SUCCESS, RESULT_CODE_FAILURE })
    public @interface ResultCode {

    }


    @ResultCode
    private final int resultCode;

    @Nullable
    private CacheException exception;


    public Result(@ResultCode final int resultCode) {
        this.resultCode = resultCode;
    }


    public Result(@ResultCode final int resultCode, @NonNull final CacheException exception) {
        this.resultCode = resultCode;
        this.exception = exception;
    }


    @ResultCode
    public int getResultCode() {
        return this.resultCode;
    }


    @Nullable
    public CacheException getException() {
        return this.exception;
    }

}
