package com.camnter.newlife.utils;

/**
 * Description：ClickUtil
 * Created by：CaMnter
 * Time：2015-12-08 17:54
 */
public class ClickUtils {

    private static long lastClickTime;
    private static long clickInterval = 500;

    public static ClickUtils ourInstance;


    public static ClickUtils getInstance() {
        if (ourInstance == null) ourInstance = new ClickUtils();
        return ourInstance;
    }


    public static ClickUtils setInterval(long interval) {
        clickInterval = interval;
        return getInstance();
    }


    public static ClickUtils clearLastTime() {
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
