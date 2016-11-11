package com.camnter.newlife.utils.camera;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * Description：自动聚焦
 * Created by：CaMnter
 */

public class AutoFocusManager implements Camera.AutoFocusCallback {

    private static final String TAG = AutoFocusManager.class.getSimpleName();
    private static final long AUTO_FOCUS_INTERVAL = 2666L;
    private static final List<String> FOCUS_MODES_CALLING = new ArrayList<>();


    static {
        FOCUS_MODES_CALLING.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING.add(Camera.Parameters.FOCUS_MODE_MACRO);
    }


    private final boolean autoFocus;
    private final Camera camera;
    private boolean isStopped;
    private boolean isFocusing;
    private AsyncTask<?, ?, ?> intervalTask;


    public AutoFocusManager(@NonNull final Camera camera) {
        this.camera = camera;
        final String currentFocusMode = this.camera.getParameters().getFocusMode();
        this.autoFocus = FOCUS_MODES_CALLING.contains(currentFocusMode);
        Log.i(TAG, "[currentFocusMode]:" + currentFocusMode + "\t\t\t [autoFocus]:" + autoFocus);
        this.startFocus();
    }


    /**
     * 开始聚焦
     */
    public synchronized void startFocus() {
        Log.i(TAG, "[startFocus()]:......");
        if (this.autoFocus) {
            this.intervalTask = null;
            if (!this.isStopped && !this.isFocusing) {
                try {
                    this.camera.autoFocus(this);
                    this.isFocusing = true;
                } catch (Exception e) {
                    // Have heard RuntimeException reported in Android 4.0.x+; continue?
                    Log.e(TAG, "[startFocus()]\t\t\tcatch (Exception e).......");
                    e.printStackTrace();
                    // Try again later to keep cycle going
                    this.runFocusInterval();
                }
            }
        }
    }


    /**
     * 停止聚焦
     */
    public synchronized void stopFocus() {
        Log.i(TAG, "[stopFocus()]:......");
        this.isStopped = true;
        if (this.autoFocus) {
            this.cancelAutoFocusTask();
            // Doesn't hurt to call this even if not focusing
            try {
                this.camera.cancelAutoFocus();
            } catch (Exception e) {
                // Have heard RuntimeException reported in Android 4.0.x+; continue?
                Log.e(TAG, "[stopFocus]\t\t\tUnexpected exception while cancelling focusing", e);
            }
        }
    }


    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        Log.i(TAG, "[onAutoFocus]\t\t\tsuccess==" + success);
        this.isFocusing = false;
        this.runFocusInterval();
    }


    /**
     * 执行聚焦间隔
     */
    private synchronized void runFocusInterval() {
        if (!this.isStopped && this.intervalTask == null) {
            AutoFocusIntervalTask autoFocusIntervalTask = new AutoFocusIntervalTask();
            try {
                autoFocusIntervalTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                this.intervalTask = autoFocusIntervalTask;
            } catch (RejectedExecutionException e) {
                Log.e(TAG, "[focus]\t\t\tCould not request auto focus", e);
            }
        }
    }


    /**
     * 取消聚焦间隔控制任务
     */
    private synchronized void cancelAutoFocusTask() {
        if (this.intervalTask != null) {
            if (this.intervalTask.getStatus() != AsyncTask.Status.FINISHED) {
                this.intervalTask.cancel(true);
            }
            this.intervalTask = null;
        }
    }


    /**
     * 聚焦间隔控制任务
     */
    private final class AutoFocusIntervalTask extends AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... params) {
            try {
                Thread.sleep(AUTO_FOCUS_INTERVAL);
            } catch (InterruptedException e) {
                Log.i(TAG, "[AutoFocusTask]\t\t\t auto focus interval task stop.......", e);
            }
            startFocus();
            return null;
        }
    }

}


