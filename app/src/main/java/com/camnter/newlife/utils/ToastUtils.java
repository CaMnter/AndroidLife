package com.camnter.newlife.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;

/**
 * Description：ToastUtil
 * Created by：CaMnter
 * Time：2015-12-14 23:01
 */
public class ToastUtils {

    public static final long DEFAULT_DURATION = 1000L;

    public static final int LENGTH_LONG = android.widget.Toast.LENGTH_LONG;
    public static final int LENGTH_SHORT = android.widget.Toast.LENGTH_SHORT;

    private static android.widget.Toast normalToast;
    private static android.widget.Toast gravityToast;
    private static Handler handler;


    static {
        if (!(Looper.myLooper() == Looper.getMainLooper())) {
            Looper.prepare();
        }
        handler = new Handler();
    }


    private static Runnable run = new Runnable() {
        public void run() {
            if (normalToast != null) normalToast.cancel();
            if (gravityToast != null) gravityToast.cancel();
        }
    };


    private static void toast(Context context, CharSequence text, int duration) {
        if (context == null) return;
        handler.removeCallbacks(run);
        long delayMillis;
        switch (duration) {
            case LENGTH_LONG:
                delayMillis = 3000L;
                break;
            case LENGTH_SHORT:
            default:
                delayMillis = DEFAULT_DURATION;
                break;
        }
        if (normalToast == null) {
            normalToast = android.widget.Toast.makeText(context, text, duration);
        } else {
            normalToast.setText(text);
        }
        handler.postDelayed(run, delayMillis);
        normalToast.show();
    }


    private static void toast(Context context, CharSequence text, int duration, int gravity, int xOffset, int yOffset) {
        if (context == null) return;
        handler.removeCallbacks(run);
        long delayMillis;
        switch (duration) {
            case LENGTH_LONG:
                delayMillis = 3000L;
                break;
            case LENGTH_SHORT:
            default:
                delayMillis = DEFAULT_DURATION;
                break;
        }
        if (gravityToast == null) {
            gravityToast = android.widget.Toast.makeText(context, text, duration);
        } else {
            gravityToast.setText(text);
        }
        gravityToast.setGravity(gravity, xOffset, yOffset);
        handler.postDelayed(run, delayMillis);
        gravityToast.show();
    }


    /**
     * 弹出Toast
     *
     * @param context 弹出Toast的上下文
     * @param text 弹出Toast的内容
     * @param duration 弹出Toast的持续时间
     */
    public static void show(Context context, CharSequence text, int duration) {
        if (duration > 0) {
            duration = LENGTH_SHORT;
        }
        toast(context, text, duration);
    }


    /**
     * 中间弹出Toast
     *
     * @param context 弹出Toast的上下文
     * @param text 弹出Toast的内容
     */
    public static void showCenter(Context context, CharSequence text) {
        toast(context, text, LENGTH_SHORT, Gravity.CENTER, 0, 0);
    }


    /**
     * 中弹出Toast
     *
     * @param context 弹出Toast的上下文
     * @param text 弹出Toast的内容
     * @param gravity 弹出Toast的gravity
     * @param xOffset 弹出Toast的x间距
     * @param yOffset 弹出Toast的y间距
     */
    public static void showGravity(Context context, CharSequence text, int gravity, int xOffset, int yOffset) {
        toast(context, text, LENGTH_SHORT, gravity, xOffset, yOffset);
    }


    /**
     * 弹出Toast
     *
     * @param context 弹出Toast的上下文
     * @param text 弹出Toast的内容
     * @param duration 弹出Toast的持续时间
     * @param gravity 弹出Toast的gravity
     * @param xOffset 弹出Toast的x间距
     * @param yOffset 弹出Toast的y间距
     */
    public static void showGravity(Context context, CharSequence text, int duration, int gravity, int xOffset, int yOffset) {
        toast(context, text, duration, gravity, xOffset, yOffset);
    }


    /**
     * 弹出Toast
     *
     * @param context 弹出Toast的上下文
     * @param resId 弹出Toast的内容的资源ID
     * @param duration 弹出Toast的持续时间
     */
    public static void show(Context context, int resId, int duration) throws NullPointerException {
        if (null == context) throw new NullPointerException("The context is null!");
        duration = duration > 0 ? LENGTH_LONG : LENGTH_SHORT;
        toast(context, context.getResources().getString(resId), duration);
    }
}
