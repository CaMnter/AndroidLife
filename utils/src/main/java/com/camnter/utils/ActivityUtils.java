package com.camnter.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Refer to the Instant run source code
 *
 * @author CaMnter
 */

public final class ActivityUtils {

    @Nullable
    public static Activity getForegroundActivity(@Nullable Context context) {
        List<Activity> list = getActivities(context, true);
        return list.isEmpty() ? null : list.get(0);
    }


    @NonNull
    public static List<Activity> getActivities(@Nullable Context context, boolean foregroundOnly) {
        List<Activity> list = new ArrayList<Activity>();
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = getActivityThread(context, activityThreadClass);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            // TODO: On older platforms, cast this to a HashMap

            Collection c;
            Object collection = activitiesField.get(activityThread);

            if (collection instanceof HashMap) {
                // Older platforms
                Map activities = (HashMap) collection;
                c = activities.values();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                collection instanceof ArrayMap) {
                ArrayMap activities = (ArrayMap) collection;
                c = activities.values();
            } else {
                return list;
            }
            for (Object activityRecord : c) {
                Class activityRecordClass = activityRecord.getClass();
                if (foregroundOnly) {
                    Field pausedField = activityRecordClass.getDeclaredField("paused");
                    pausedField.setAccessible(true);
                    if (pausedField.getBoolean(activityRecord)) {
                        continue;
                    }
                }
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                if (activity != null) {
                    list.add(activity);
                }
            }
        } catch (Throwable ignore) {
        }
        return list;
    }


    @Nullable
    public static Object getActivityThread(@Nullable Context context,
                                           @Nullable Class<?> activityThread) {
        try {
            if (activityThread == null) {
                activityThread = Class.forName("android.app.ActivityThread");
            }
            Method m = activityThread.getMethod("currentActivityThread");
            m.setAccessible(true);
            Object currentActivityThread = m.invoke(null);
            if (currentActivityThread == null && context != null) {
                // In older versions of Android (prior to frameworks/base 66a017b63461a22842)
                // the currentActivityThread was built on thread locals, so we'll need to try
                // even harder
                Field mLoadedApk = context.getClass().getField("mLoadedApk");
                mLoadedApk.setAccessible(true);
                Object apk = mLoadedApk.get(context);
                Field mActivityThreadField = apk.getClass().getDeclaredField("mActivityThread");
                mActivityThreadField.setAccessible(true);
                currentActivityThread = mActivityThreadField.get(apk);
            }
            return currentActivityThread;
        } catch (Throwable ignore) {
            return null;
        }
    }

}
