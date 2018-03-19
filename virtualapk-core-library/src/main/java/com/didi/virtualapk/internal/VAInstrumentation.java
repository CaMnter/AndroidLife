/*
 * Copyright (C) 2017 Beijing Didi Infinity Technology and Development Co.,Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.didi.virtualapk.internal;

import android.app.Activity;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.didi.virtualapk.PluginManager;
import com.didi.virtualapk.utils.PluginUtil;
import com.didi.virtualapk.utils.ReflectUtil;

/**
 * 1. Hook Instrumentation，主要为了 newActivity，加载插件 Activity
 * -  以及，在插件 Activity onCreate 之前，完成 Activity 内 Resources、Context 和 Application 的替换
 * 2. Hook H # Callback mCallback，拦截 H 中 插件 Activity 的 LAUNCH_ACTIVITY 消息
 *
 * Created by renyugang on 16/8/10.
 */

public class VAInstrumentation extends Instrumentation implements Handler.Callback {

    public static final String TAG = "VAInstrumentation";
    public static final int LAUNCH_ACTIVITY = 100;

    private Instrumentation mBase;

    PluginManager mPluginManager;


    public VAInstrumentation(PluginManager pluginManager, Instrumentation base) {
        this.mPluginManager = pluginManager;
        this.mBase = base;
    }


    /**
     * 进入 AMS 所在的进程前
     *
     * 覆写 Instrumentation # execStartActivity
     *
     * 在这里完成替换 intent 内部信息，替换为合适的插桩 Activity 信息
     *
     * @param who Context
     * @param contextThread IApplicationThread
     * @param token null
     * @param target null
     * @param intent intent
     * @param requestCode -1
     * @param options Bundle
     * @return ActivityResult
     */
    public ActivityResult execStartActivity(
        Context who, IBinder contextThread, IBinder token, Activity target,
        Intent intent, int requestCode, Bundle options) {
        mPluginManager.getComponentsHandler().transformIntentToExplicitAsNeeded(intent);
        // null component is an implicitly intent
        if (intent.getComponent() != null) {
            Log.i(TAG,
                String.format("execStartActivity[%s : %s]", intent.getComponent().getPackageName(),
                    intent.getComponent().getClassName()));
            // resolve intent with Stub Activity if needed
            this.mPluginManager.getComponentsHandler().markIntentIfNeeded(intent);
        }

        ActivityResult result = realExecStartActivity(who, contextThread, token, target,
            intent, requestCode, options);

        return result;

    }


    /**
     * 进入 AMS 所在的进程前
     *
     * 真正调用 反射调用 原声 Instrumentation # execStartActivity 方法
     *
     * 去访问 AMS 所在进程
     *
     * @param who Context
     * @param contextThread IApplicationThread
     * @param token null
     * @param target null
     * @param intent intent
     * @param requestCode -1
     * @param options Bundle
     * @return ActivityResult
     */
    private ActivityResult realExecStartActivity(
        Context who, IBinder contextThread, IBinder token, Activity target,
        Intent intent, int requestCode, Bundle options) {
        ActivityResult result = null;
        try {
            Class[] parameterTypes = { Context.class, IBinder.class, IBinder.class, Activity.class,
                Intent.class,
                int.class, Bundle.class };
            result = (ActivityResult) ReflectUtil.invoke(Instrumentation.class, mBase,
                "execStartActivity", parameterTypes,
                who, contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            if (e.getCause() instanceof ActivityNotFoundException) {
                throw (ActivityNotFoundException) e.getCause();
            }
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 5.0.0 以下才有 关于 fragment 的 execStartActivity
     * 适配 5.0.0 以下
     *
     * 进入 AMS 所在的进程前
     *
     * 覆写 Instrumentation # execStartActivity
     *
     * 在这里完成替换 intent 内部信息，替换为合适的插桩 Activity 信息
     *
     * @param who Context
     * @param contextThread IApplicationThread
     * @param token null
     * @param target null
     * @param intent intent
     * @param requestCode -1
     * @param options Bundle
     * @return ActivityResult
     */
    public ActivityResult execStartActivity(
        Context who, IBinder contextThread, IBinder token, Fragment target,
        Intent intent, int requestCode, Bundle options) {
        mPluginManager.getComponentsHandler().transformIntentToExplicitAsNeeded(intent);
        // null component is an implicitly intent
        if (intent.getComponent() != null) {
            Log.i(TAG,
                String.format("execStartActivity[%s : %s]", intent.getComponent().getPackageName(),
                    intent.getComponent().getClassName()));
            // resolve intent with Stub Activity if needed
            this.mPluginManager.getComponentsHandler().markIntentIfNeeded(intent);
        }

        ActivityResult result = realExecStartActivity(who, contextThread, token, target,
            intent, requestCode, options);

        return result;

    }


    /**
     * 5.0.0 以下才有 关于 fragment 的 execStartActivity
     * 适配 5.0.0 以下
     *
     * 进入 AMS 所在的进程前
     *
     * 真正调用 反射调用 原声 Instrumentation # execStartActivity 方法
     *
     * 去访问 AMS 所在进程
     *
     * @param who Context
     * @param contextThread IApplicationThread
     * @param token null
     * @param target null
     * @param intent intent
     * @param requestCode -1
     * @param options Bundle
     * @return ActivityResult
     */
    private ActivityResult realExecStartActivity(
        Context who, IBinder contextThread, IBinder token, Fragment target,
        Intent intent, int requestCode, Bundle options) {
        ActivityResult result = null;
        try {
            Class[] parameterTypes = { Context.class, IBinder.class, IBinder.class, Fragment.class,
                Intent.class,
                int.class, Bundle.class };
            result = (ActivityResult) ReflectUtil.invoke(Instrumentation.class, mBase,
                "execStartActivity", parameterTypes,
                who, contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            if (e.getCause() instanceof ActivityNotFoundException) {
                throw (ActivityNotFoundException) e.getCause();
            }
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 进入 AMS 所在的进程前
     *
     * 真正调用 反射调用 原声 Instrumentation # execStartActivity 方法
     *
     * 去访问 AMS 所在进程
     *
     * @param who Context
     * @param contextThread IApplicationThread
     * @param token null
     * @param target null
     * @param intent intent
     * @param requestCode -1
     * @param options Bundle
     * @return ActivityResult
     */
    private ActivityResult realExecStartActivity(
        Context who, IBinder contextThread, IBinder token, String target,
        Intent intent, int requestCode, Bundle options) {
        ActivityResult result = null;
        try {
            Class[] parameterTypes = { Context.class, IBinder.class, IBinder.class, String.class,
                Intent.class,
                int.class, Bundle.class };
            result = (ActivityResult) ReflectUtil.invoke(Instrumentation.class, mBase,
                "execStartActivity", parameterTypes,
                who, contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            if (e.getCause() instanceof ActivityNotFoundException) {
                throw (ActivityNotFoundException) e.getCause();
            }
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 进入 AMS 所在的进程前
     *
     * 覆写 Instrumentation # execStartActivity
     *
     * 在这里完成替换 intent 内部信息，替换为合适的插桩 Activity 信息
     *
     * @param who Context
     * @param contextThread IApplicationThread
     * @param token null
     * @param target null
     * @param intent intent
     * @param requestCode -1
     * @param options Bundle
     * @return ActivityResult
     */
    public ActivityResult execStartActivity(
        Context who, IBinder contextThread, IBinder token, String target,
        Intent intent, int requestCode, Bundle options) {
        mPluginManager.getComponentsHandler().transformIntentToExplicitAsNeeded(intent);
        // null component is an implicitly intent
        if (intent.getComponent() != null) {
            Log.i(TAG,
                String.format("execStartActivity[%s : %s]", intent.getComponent().getPackageName(),
                    intent.getComponent().getClassName()));
            // resolve intent with Stub Activity if needed
            this.mPluginManager.getComponentsHandler().markIntentIfNeeded(intent);
        }

        ActivityResult result = realExecStartActivity(who, contextThread, token, target,
            intent, requestCode, options);
        return null;
    }


    /**
     * 重写 newActivity 方法，适配了 new 插件 Activity
     *
     * 1. 如果当前 classloader 找不到 classname
     * 2. 通过 插件 classloader 去实现 newActivity
     * 3. 兼容 4.1 以上的 Resources mResources 适配，hook 掉 activity 的 mResources，替换为 插件 resources
     *
     * @param cl ClassLoader
     * @param className className
     * @param intent intent
     * @return Activity
     * @throws InstantiationException InstantiationException
     * @throws IllegalAccessException IllegalAccessException
     * @throws ClassNotFoundException ClassNotFoundException
     */
    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            LoadedPlugin plugin = this.mPluginManager.getLoadedPlugin(intent);
            String targetClassName = PluginUtil.getTargetActivity(intent);

            Log.i(TAG, String.format("newActivity[%s : %s]", className, targetClassName));

            if (targetClassName != null) {
                Activity activity = mBase.newActivity(plugin.getClassLoader(), targetClassName,
                    intent);
                activity.setIntent(intent);

                try {
                    // for 4.1+
                    ReflectUtil.setField(ContextThemeWrapper.class, activity, "mResources",
                        plugin.getResources());
                } catch (Exception ignored) {
                    // ignored.
                }

                return activity;
            }
        }

        return mBase.newActivity(cl, className, intent);
    }


    /**
     * 在 ActivityThread # performLaunchActivity 会调用该方法
     *
     * private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
     *
     * -    ActivityInfo aInfo = r.activityInfo;
     *
     * -    ...
     *
     * -   ComponentName component = r.intent.getComponent();
     *
     * -   ...
     *
     * -   ContextImpl appContext = createBaseContextForActivity(r);
     * -   Activity activity = null;
     * -   try {
     * -       java.lang.ClassLoader cl = appContext.getClassLoader();
     * -       activity = mInstrumentation.newActivity(
     * -               cl, component.getClassName(), r.intent);
     * -       StrictMode.incrementExpectedActivityCount(activity.getClass());
     * -       r.intent.setExtrasClassLoader(cl);
     * -       r.intent.prepareToEnterProcess();
     * -       if (r.state != null) {
     * -           r.state.setClassLoader(cl);
     * -       }
     * -   } catch (Exception e) {
     * -       ...
     * -   }
     *
     * -   ...
     *
     * -   Application app = r.packageInfo.makeApplication(false, mInstrumentation);
     *
     * -   activity.attach(appContext, this, getInstrumentation(), r.token,...)
     *
     * -   if (r.isPersistable()) {
     * -       mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
     * -   } else {
     * -       mInstrumentation.callActivityOnCreate(activity, r.state);
     * -   }
     *
     * -   ...
     *
     * -   r.activity = activity;
     *
     * -   ...
     *
     * -   mActivities.put(r.token, r);
     *
     * -   ...
     *
     * -   return activity;
     * }
     *
     *
     * 1. Activity 的 intent 信息
     * 2. 根据 intent 信息，判断是否是 插件 Activity
     * 3. 根据 intent 信息，获取对应的 LoadedPlugin
     * 4. 根据 LoadedPlugin 内的信息
     * 4.1   hook 掉 base context 的 Resources mResources
     * 4.2   hook 掉 activity 的 Context mBase（ 视为 ContextWrapper class 进行 hook ）
     * 4.3   hook 掉 activity 的 Application mApplication
     * 4.4   hook 掉 activity 的 Context mBase（ 视为 ContextThemeWrapper class 进行 hook ）
     * 5. 设置 垂直 或者 水平 显示
     *
     * @param activity activity
     * @param icicle icicle
     */
    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        final Intent intent = activity.getIntent();
        if (PluginUtil.isIntentFromPlugin(intent)) {
            Context base = activity.getBaseContext();
            try {
                LoadedPlugin plugin = this.mPluginManager.getLoadedPlugin(intent);
                ReflectUtil.setField(base.getClass(), base, "mResources", plugin.getResources());
                ReflectUtil.setField(ContextWrapper.class, activity, "mBase",
                    plugin.getPluginContext());
                ReflectUtil.setField(Activity.class, activity, "mApplication",
                    plugin.getApplication());
                ReflectUtil.setFieldNoException(ContextThemeWrapper.class, activity, "mBase",
                    plugin.getPluginContext());

                // set screenOrientation
                ActivityInfo activityInfo = plugin.getActivityInfo(PluginUtil.getComponent(intent));
                if (activityInfo.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                    activity.setRequestedOrientation(activityInfo.screenOrientation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        mBase.callActivityOnCreate(activity, icicle);
    }


    /**
     * 拦截 H 中 插件 Activity 的 LAUNCH_ACTIVITY 消息
     *
     * *********************************************************************************************
     *
     * ActivityThread #
     *
     * private class ApplicationThread extends IApplicationThread.Stub {
     *
     * -    public final void  scheduleLaunchActivity(...) {
     *
     * -        ...
     *
     * -        ActivityClientRecord r = new ActivityClientRecord();
     * -        r.token = token;
     * -        r.ident = ident;
     * -        r.intent = intent;
     * -        r.referrer = referrer;
     * -        r.voiceInteractor = voiceInteractor;
     * -        r.activityInfo = info;
     * -        r.compatInfo = compatInfo;
     * -        r.state = state;
     * -        r.persistentState = persistentState;
     * -        r.pendingResults = pendingResults;
     * -        r.pendingIntents = pendingNewIntents;
     * -        r.startsNotResumed = notResumed;
     * -        r.isForward = isForward;
     * -        r.profilerInfo = profilerInfo;
     * -        r.overrideConfig = overrideConfig;
     *
     * -        ...
     *
     * -        sendMessage(H.LAUNCH_ACTIVITY, r);
     *
     * -    }
     * }
     *
     * *********************************************************************************************
     *
     * ActivityThread # H
     * private class H extends Handler {
     *
     * -    public static final int LAUNCH_ACTIVITY         = 100;
     *
     * -    ...
     *
     * -    public void handleMessage(Message msg) {
     * -        switch (msg.what) {
     * -                case LAUNCH_ACTIVITY: {
     * -                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
     * -                    final ActivityClientRecord r = (ActivityClientRecord) msg.obj;
     * -                    r.packageInfo = getPackageInfoNoCheck(
     * -                            r.activityInfo.applicationInfo, r.compatInfo);
     * -                    handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
     * -                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
     * -                } break;
     *
     * -                ...
     *
     * -    ...
     *
     * -    }
     *
     * }
     *
     * *********************************************************************************************
     *
     * public class Handler {
     *
     * ...
     *
     * final Callback mCallback;
     *
     * public void dispatchMessage(Message msg) {
     * if (msg.callback != null) {
     * handleCallback(msg);
     * } else {
     * if (mCallback != null) {
     * if (mCallback.handleMessage(msg)) {
     * return;
     * }
     * }
     * }
     * }
     *
     * }
     *
     * *********************************************************************************************
     *
     * Hook H 的 Callback mCallback，拦截 插件 activity 对应的 LAUNCH_ACTIVITY 消息
     *
     * 1. 用 Hook H # Callback mCallback 的方式，获取了 ActivityClientRecord
     * 2. 反射获取 ActivityClientRecord 内的 Intent intent
     * 3. 设置 intent 的 setExtrasClassLoader
     * 4. 反射获取 ActivityClientRecord 内的 ActivityInfo activityInfo
     * 5. 获取插件主题，设置在 activityInfo 上
     *
     * @param msg msg
     * @return boolean
     */
    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == LAUNCH_ACTIVITY) {
            // ActivityClientRecord r
            Object r = msg.obj;
            try {
                Intent intent = (Intent) ReflectUtil.getField(r.getClass(), r, "intent");
                intent.setExtrasClassLoader(VAInstrumentation.class.getClassLoader());
                ActivityInfo activityInfo = (ActivityInfo) ReflectUtil.getField(r.getClass(), r,
                    "activityInfo");

                if (PluginUtil.isIntentFromPlugin(intent)) {
                    int theme = PluginUtil.getTheme(mPluginManager.getHostContext(), intent);
                    if (theme != 0) {
                        Log.i(TAG,
                            "resolve theme, current theme:" + activityInfo.theme + "  after :0x" +
                                Integer.toHexString(theme));
                        activityInfo.theme = theme;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    @Override
    public Context getContext() {
        return mBase.getContext();
    }


    @Override
    public Context getTargetContext() {
        return mBase.getTargetContext();
    }


    @Override
    public ComponentName getComponentName() {
        return mBase.getComponentName();
    }

}
