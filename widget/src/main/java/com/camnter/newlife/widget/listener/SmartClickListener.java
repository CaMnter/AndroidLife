package com.camnter.newlife.widget.listener;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Description：SmartClickListener
 * Created by：CaMnter
 */

public abstract class SmartClickListener implements View.OnClickListener {

    private static final long DEFAULT_DELAY_MILLIS = 777;

    private long delayMillis;
    private boolean waiting = false;
    @Nullable
    private View targetView;

    @NonNull
    private final Handler mainHandler;
    @NonNull
    private final Runnable waitingRunnable;


    public SmartClickListener() {
        this(null);
    }


    public SmartClickListener(final long delayMillis) {
        this(null, delayMillis);
    }


    public SmartClickListener(@Nullable final View targetView) {
        this(targetView, DEFAULT_DELAY_MILLIS);
    }


    public SmartClickListener(@Nullable final View targetView, final long delayMillis) {
        this.targetView = targetView;
        this.delayMillis = delayMillis;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.waitingRunnable = new Runnable() {
            @Override public void run() {
                waiting = false;
                setTargetViewEnabled(true);
            }
        };
    }


    public void setTargetView(@Nullable View targetView) {
        this.targetView = targetView;
    }


    public void setDelayMillis(final long delayMillis) {
        this.delayMillis = delayMillis;
    }


    private void setTargetViewEnabled(final boolean enabled) {
        if (this.targetView == null) return;
        this.targetView.setEnabled(enabled);
    }


    @Override
    public void onClick(View v) {
        if (this.waiting) return;
        this.waiting = true;
        this.setTargetViewEnabled(false);
        this.mainHandler.postDelayed(this.waitingRunnable, this.delayMillis);
        this.smartOnClick(v);
    }


    protected abstract void smartOnClick(View v);

}
