package com.camnter.newlife.utils;

/**
 * Description：ClickUtil
 * Created by：CaMnter
 * Time：2015-12-08 17:54
 */
public class ClickUtil {
    private static long lastClickTime;
    private static long clickInterval = 500;

    public static ClickUtil ourInstance;

    public static ClickUtil getInstance() {
        if (ourInstance == null) ourInstance = new ClickUtil();
        return ourInstance;
    }

    public static ClickUtil setInterval(long interval) {
        clickInterval = interval;
        return getInstance();
    }

    public static ClickUtil clearLastTime() {
        lastClickTime = 0L;
        return getInstance();
    }

    public synchronized static boolean clickable() {
        long currentTime = System.currentTimeMillis();
        boolean status = currentTime - lastClickTime >= clickInterval;
        lastClickTime = currentTime;
        return status;
    }
}
