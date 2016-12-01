/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.fd.runtime;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.tools.fd.runtime.BootstrapApplication.LOG_TAG;

/**
 * Handler capable of restarting parts of the application in order for changes to become
 * apparent to the user:
 * <ul>
 * <li> Apply a tiny change immediately (possible if we can detect that the change
 * is only used in a limited context (such as in a layout) and we can directly
 * poke the view hierarchy and schedule a paint
 * <li> Apply a change to the current activity. We can restart just the activity
 * while the app continues running.
 * <li> Restart the app with state persistence (simulates what happens when a user
 * puts an app in the background, then it gets killed by the memory monitor,
 * and then restored when the user brings it back
 * <li> Restart the app completely.
 * </ul>
 *
 * 1. restartActivityOnUiThread：只在 UiThread 线程执行 updateActivity(...)
 * 2. restartActivity：重启 Activity
 * -    2.1 拿到该 Activity 的最顶层 Parent Activity
 * -    2.2 然后用 最顶层 Parent Activity 执行 recreate 方法
 * 3. restartApp：重启 App
 * -    3.1 判断 activities 是否没有内容
 * -    -    3.1.1 没有的话，这个方法就不做任何事情
 * -    -    3.1.2 有的话，继续
 * -    3.2 获取前台 Activity
 * -    -    3.2.1 前台 Activity 为 null，那么就拿到 activities 的第一个 Activity 打 Toast，然后直接关闭 App（ 杀死进程 ）
 * -    -    3.2.2 前台 Activity 为 存在，那么就拿 前台 Activity 打 Toast，然后继续
 * -    3.3 定制了一个 PendingIntent 是为了在未来打开这个 前台 Activity
 * -    3.4 获取 AlarmManager，设置定时任务，再未来的 100ms 后，通过 PendingIntent 打开这个 前台 Activity
 * -    3.5 杀死进程，等待 4. 的定时任务执行，并打开 前台 Activity，实现重启 App 的效果
 * 4. showToast：显示 toast
 * -    4.1 尝试获取 activity 的 base context
 * -    -    4.1.1 拿不到的话，return
 * -    4.2 如果如果 Toast 的内容大于 60 或者有换行（ \n ），那么持续时间长。否则，短
 * -    4.3 调用 Toast.makeText(...).show() 显示 Toast
 * 5. getForegroundActivity：获取前台显示的 Activity，也就是获取全部没有 paused 的 Activity，然后从这个取第一个
 * 6. getActivities：获取没有 paused 的 Activity
 * -    6.1 反射获取 ActivityThread 的 mActivities Field
 * -    6.2 获取 mActivities 的值，根据版本兼容：
 * -    -    6.2.1 拿不到的话，return
 * -    -    6.2.2 如果 > 4.4 && 是 ArrayMap 的话，转
 * -    -    6.2.3 都不是的话，会返回初始化好，没内容的 list
 * -    6.3 遍历 mActivities 值，拿到每一个 ActivityRecord
 * -    -    6.3.1 判断是否是 foregroundOnly：
 * -    -    -    6.3.1.1 true 的话，过滤出 ActivityRecord 的 paused == true 的 ActivityRecord
 * -    -    -    6.3.1.2 false 的话，不走过滤逻辑
 * -    6.4 然后反射 3. 下来的 ActivityRecord 的 activity Field
 * -    6.4 拿到 ActivityRecord 的 activity Field 的值，添加到 list 里
 * 7. updateActivity：调用 restartActivity 重启 Activity
 * 8. showToastWhenPossible：如果可能的话，显示 Toast
 * -    8.1 获取前台 Activity
 * -    8.2.1 如果拿到了，就调用 Restarter.showToast(...)
 * -    8.2.2 如果没拿到，进入重试方法 showToastWhenPossible(...)，根据重试次数，不断尝试显示 Toast
 * 9. showToastWhenPossible：重试显示 Toast 方法，根据重试次数，不断尝试显示 Toast
 * -    9.1 先实例化一个主线程 Handler，用于与主线程通信（ 现在 Toast ）
 * -    9.2 然后希望在主线程执行的任务 Runnable 内，拿到获取前台显示 Activity
 * -    -    9.2.1 如果此次拿到了，直接调用 showToast(...) 方法显示 Toast
 * -    -    9.2.1 如果此次拿不到，那么递归到下次，继续尝试拿，一直递归到重试次数大于 0 为止
 */
public class Restarter {

    /**
     * 只在 UiThread 线程执行 updateActivity(...)
     *
     * runOnUiThread 的原理很简单，就是判断是不是主线程，是的话，直接 run 这个 Runnable
     * 不是的话，调用 Activity 内置的主线程 mHandler ，给主线程的 MessageQueue 发 消息，
     * 回到主线程中处理该 Runnable
     */
    /** Restart an activity. Should preserve as much state as possible. */
    public static void restartActivityOnUiThread(@NonNull final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Resources updated: notify activities");
                }
                updateActivity(activity);
            }
        });
    }


    /**
     * 重启 Activity
     *
     * 1. 拿到该 Activity 的最顶层 Parent Activity
     * 2. 然后用 最顶层 Parent Activity 执行 recreate 方法
     *
     * @param activity activity
     */
    private static void restartActivity(@NonNull Activity activity) {
        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.v(LOG_TAG, "About to restart " + activity.getClass().getSimpleName());
        }

        // You can't restart activities that have parents: find the top-most activity
        while (activity.getParent() != null) {
            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, activity.getClass().getSimpleName()
                    + " is not a top level activity; restarting "
                    + activity.getParent().getClass().getSimpleName() + " instead");
            }
            activity = activity.getParent();
        }

        // Directly supported by the framework!
        activity.recreate();
    }


    /**
     * Attempt to restart the app. Ideally this should also try to preserve as much state as
     * possible:
     * <ul>
     * <li>The current activity</li>
     * <li>If possible, state in the current activity, and</li>
     * <li>The activity stack</li>
     * </ul>
     *
     * This may require some framework support. Apparently it may already be possible
     * (Dianne says to put the app in the background, kill it then restart it; need to
     * figure out how to do this.)
     *
     * 重启 App
     *
     * 1. 判断 activities 是否没有内容
     * -    1.1 没有的话，这个方法就不做任何事情
     * -    1.2 有的话，继续
     * 2. 获取前台 Activity
     * -    2.1 前台 Activity 为 null，那么就拿到 activities 的第一个 Activity 打 Toast，然后直接关闭 App（ 杀死进程 ）
     * -    2.2 前台 Activity 为 存在，那么就拿 前台 Activity 打 Toast，然后继续
     * 3. 定制了一个 PendingIntent 是为了在未来打开这个 前台 Activity
     * 4. 获取 AlarmManager，设置定时任务，在未来的 100ms 后，通过 PendingIntent 打开这个 前台 Activity
     * 5. 杀死进程，等待 4. 的定时任务执行，并打开 前台 Activity，实现重启 App 的效果
     */
    public static void restartApp(@Nullable Context appContext,
                                  @NonNull Collection<Activity> knownActivities,
                                  boolean toast) {
        if (!knownActivities.isEmpty()) {
            // Can't live patch resources; instead, try to restart the current activity
            Activity foreground = getForegroundActivity(appContext);

            if (foreground != null) {
                // http://stackoverflow.com/questions/6609414/howto-programatically-restart-android-app
                //noinspection UnnecessaryLocalVariable
                if (toast) {
                    showToast(foreground, "Restarting app to apply incompatible changes");
                }
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "RESTARTING APP");
                }
                @SuppressWarnings("UnnecessaryLocalVariable") // fore code clarify
                    Context context = foreground;
                Intent intent = new Intent(context, foreground.getClass());
                int intentId = 0;
                PendingIntent pendingIntent = PendingIntent.getActivity(context, intentId,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Scheduling activity " + foreground
                        + " to start after exiting process");
                }
            } else {
                showToast(knownActivities.iterator().next(), "Unable to restart app");
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Couldn't find any foreground activities to restart " +
                        "for resource refresh");
                }
            }
            System.exit(0);
        }
    }


    /**
     * 显示 toast
     *
     * 1. 尝试获取 activity 的 base context
     * -    1.1 拿不到的话，return
     * -    1.2 拿到的话，继续
     * 2. 如果如果 Toast 的内容大于 60 或者有换行（ \n ），那么持续时间长。否则，短
     * 3. 调用 Toast.makeText(...).show() 显示 Toast
     *
     * @param activity activity
     * @param text text
     */
    static void showToast(@NonNull final Activity activity, @NonNull final String text) {
        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.v(LOG_TAG, "About to show toast for activity " + activity + ": " + text);
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = activity.getApplicationContext();
                    if (context instanceof ContextWrapper) {
                        Context base = ((ContextWrapper) context).getBaseContext();
                        if (base == null) {
                            if (Log.isLoggable(LOG_TAG, Log.WARN)) {
                                Log.w(LOG_TAG, "Couldn't show toast: no base context");
                            }
                            return;
                        }
                    }

                    // For longer messages, leave the message up longer
                    int duration = Toast.LENGTH_SHORT;
                    if (text.length() >= 60 || text.indexOf('\n') != -1) {
                        duration = Toast.LENGTH_LONG;
                    }

                    // Avoid crashing when not available, e.g.
                    //   java.lang.RuntimeException: Can't create handler inside thread that has
                    //        not called Looper.prepare()
                    Toast.makeText(activity, text, duration).show();
                } catch (Throwable e) {
                    if (Log.isLoggable(LOG_TAG, Log.WARN)) {
                        Log.w(LOG_TAG, "Couldn't show toast", e);
                    }
                }
            }
        });
    }


    /**
     * 获取前台显示的 Activity
     *
     * 也就是获取全部没有 paused 的 Activity，然后从这个取第一个
     *
     * @param context context
     * @return 前台 Activity
     */
    @Nullable
    public static Activity getForegroundActivity(@Nullable Context context) {
        List<Activity> list = getActivities(context, true);
        return list.isEmpty() ? null : list.get(0);
    }

    // http://stackoverflow.com/questions/11411395/how-to-get-current-foreground-activity-context-in-android


    /**
     * 获取没有 paused 的 Activity
     *
     * 1. 反射获取 ActivityThread 的 mActivities Field
     * 2. 获取 mActivities 的值，根据版本兼容：
     * -    2.1 如果是 HashMap 的话，转
     * -    2.2 如果 > 4.4 && 是 ArrayMap 的话，转
     * -    2.3 都不是的话，会返回初始化好，没内容的 list
     * 3. 遍历 mActivities 值，拿到每一个 ActivityRecord
     * -    3.1 判断是否是 foregroundOnly：
     * -    -    true 的话，过滤出 ActivityRecord 的 paused == true 的 ActivityRecord
     * -    -    false 的话，不走过滤逻辑
     * 4. 然后反射 3. 下来的 ActivityRecord 的 activity Field
     * 5. 拿到 ActivityRecord 的 activity Field 的值，添加到 list 里
     *
     * @param context context
     * @param foregroundOnly foregroundOnly
     * @return activities
     */
    @NonNull
    public static List<Activity> getActivities(@Nullable Context context, boolean foregroundOnly) {
        List<Activity> list = new ArrayList<Activity>();
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = MonkeyPatcher.getActivityThread(context, activityThreadClass);
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


    /**
     * 调用 restartActivity 重启 Activity
     *
     * @param activity activity
     */
    private static void updateActivity(@NonNull Activity activity) {
        // This method can be called for activities that are not in the foreground, as long
        // as some of its resources have been updated. Therefore we'll need to make sure
        // that this activity is in the foreground, and if not do nothing. Ways to do
        // that are outlined here:
        // http://stackoverflow.com/questions/3667022/checking-if-an-android-application-is-running-in-the-background/5862048#5862048

        // Try to force re-layout; there are many approaches; see
        // http://stackoverflow.com/questions/5991968/how-to-force-an-entire-layout-view-refresh

        // This doesn't seem to update themes properly -- may need to do recreate() instead!
        //getWindow().getDecorView().findViewById(android.R.id.content).invalidate();

        // This is a bit of a sledgehammer. We should consider having an incremental updater,
        // similar to IntelliJ's Look &amp; Feel updater which iterates to the view hierarchy
        // and tries to incrementally refresh the LAF delegates and force a repaint.
        // On the other hand, we may never be able to succeed with that, since there could be
        // UI elements on the screen cached from callbacks. I should probably *not* attempt
        // to try to poke the user's data models; recreating the current layout should be
        // enough (e.g. if a layout references @string/foo, we'll recreate those widgets
        //    if (mLastContentView != -1) {
        //        setContentView(mLastContentView);
        //    } else {
        //        recreate();
        //    }
        // -- nope, even that's iffy. I had code which *after* calling setContentView would
        // do some findViewById calls etc to reinitialize views.
        //
        // So what I should really try to do is have some knowledge about what changed,
        // and see if I can figure out that the change is minor (e.g. doesn't affect themes
        // or layout parameters etc), and if so, just try to poke the view hierarchy directly,
        // and if not, just recreate

        //    if (changeManager.isSimpleDelta()) {
        //        changeManager.applyDirectly(this);
        //    } else {

        // Note: This doesn't handle manifest changes like changing the application title

        restartActivity(activity);
    }

    /** Show a toast when an activity becomes available (if possible). */
    /**
     * 如果可能的话，显示 Toast
     *
     * 1. 获取前台 Activity
     * 2.1 如果拿到了，就调用 Restarter.showToast(...)
     * 2.2 如果没拿到，进入重试方法 showToastWhenPossible(...)，根据重试次数，不断尝试显示 Toast
     *
     * @param context context
     * @param message toast 内容
     */
    public static void showToastWhenPossible(@Nullable Context context, @NonNull String message) {
        Activity activity = Restarter.getForegroundActivity(context);
        if (activity != null) {
            Restarter.showToast(activity, message);
        } else {
            // Only try for about 10 seconds
            showToastWhenPossible(context, message, 10);
        }
    }


    /**
     * 重试显示 Toast 方法
     * 根据重试次数，不断尝试显示 Toast
     *
     * 1. 先实例化一个主线程 Handler，用于与主线程通信（ 现在 Toast ）
     * 2. 然后希望在主线程执行的任务 Runnable 内，拿到获取前台显示 Activity
     * -    2.1 如果此次拿到了，直接调用 showToast(...) 方法显示 Toast
     * -    2.2 如果此次拿不到，那么递归到下次，继续尝试拿，一直递归到重试次数大于 0 为止
     *
     * @param context context
     * @param message message
     * @param remainingAttempts remainingAttempts
     */
    private static void showToastWhenPossible(
        @Nullable final Context context,
        @NonNull final String message,
        final int remainingAttempts) {
        Looper mainLooper = Looper.getMainLooper();
        Handler handler = new Handler(mainLooper);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Activity activity = getForegroundActivity(context);
                if (activity != null) {
                    showToast(activity, message);
                } else {
                    if (remainingAttempts > 0) {
                        showToastWhenPossible(context, message, remainingAttempts - 1);
                    }
                }
            }
        }, 1000);
    }
}
