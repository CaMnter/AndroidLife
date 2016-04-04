package com.camnter.newlife.utils.other;

import android.app.Activity;
import java.lang.ref.WeakReference;

/**
 * Description：ActivityManager
 * Created by：CaMnter
 * Time：2016-02-27 21:54
 */
public class ActivityManager {

    private static ActivityManager instance = new ActivityManager();
    private WeakReference<Activity> activityWeakReference;


    private ActivityManager() {
    }


    /**
     * 获得 ActivityManager 实例
     *
     * @return ActivityManager
     */
    public static ActivityManager getInstance() {
        return instance;
    }


    /**
     * 获得 弱引用 缓存的 Activity
     *
     * @return Activity
     */
    public Activity getCurrentActivity() {
        Activity currentActivity = null;
        if (activityWeakReference != null) {
            currentActivity = activityWeakReference.get();
        }
        return currentActivity;
    }


    /**
     * 获得 设置 Activity 为弱引用
     *
     * @param activity activity
     */
    public void setCurrentActivity(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }
}
