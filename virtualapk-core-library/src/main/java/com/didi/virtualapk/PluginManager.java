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

package com.didi.virtualapk;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Singleton;
import com.didi.virtualapk.delegate.ActivityManagerProxy;
import com.didi.virtualapk.delegate.IContentProviderProxy;
import com.didi.virtualapk.internal.ComponentsHandler;
import com.didi.virtualapk.internal.LoadedPlugin;
import com.didi.virtualapk.internal.PluginContentResolver;
import com.didi.virtualapk.internal.VAInstrumentation;
import com.didi.virtualapk.utils.PluginUtil;
import com.didi.virtualapk.utils.ReflectUtil;
import com.didi.virtualapk.utils.RunUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by renyugang on 16/8/9.
 */
@SuppressWarnings("DanglingJavadoc")
public class PluginManager {

    public static final String TAG = "PluginManager";

    private static volatile PluginManager sInstance = null;

    // Context of host app
    private Context mContext;
    private ComponentsHandler mComponentsHandler;

    /**
     * 所有 加载过的 Apk 信息
     */
    private Map<String, LoadedPlugin> mPlugins = new ConcurrentHashMap<>();

    /**
     * 缓存被 hook 掉的
     * Instrumentation
     * IActivityManager binder
     * IContentProvider binder
     *
     * 由于 都是用动态代理 hook
     * 如果想不走动态代理的逻辑
     * 就需要保存 未代理的 原引用
     */
    private Instrumentation mInstrumentation; // Hooked instrumentation
    private IActivityManager mActivityManager; // Hooked IActivityManager binder
    private IContentProvider mIContentProvider; // Hooked IContentProvider binder


    public static PluginManager getInstance(Context base) {
        if (sInstance == null) {
            synchronized (PluginManager.class) {
                if (sInstance == null) {
                    sInstance = new PluginManager(base);
                }
            }
        }

        return sInstance;
    }


    private PluginManager(Context context) {
        Context app = context.getApplicationContext();
        if (app == null) {
            this.mContext = context;
        } else {
            this.mContext = ((Application) app).getBaseContext();
        }
        prepare();
    }


    /**
     * 准备
     *
     * 1. 保存宿主 context
     * 2. Hook Instrumentation 和 ActivityThread # H # Handler.Callback mCallback
     * 3. 根据 是否是 8.0.0  版本，选择 Hook AMS 的方案。但是，都会 Hook AMS
     */
    private void prepare() {
        Systems.sHostContext = getHostContext();
        this.hookInstrumentationAndHandler();
        if (Build.VERSION.SDK_INT >= 26) {
            this.hookAMSForO();
        } else {
            this.hookSystemServices();
        }
    }


    public void init() {
        mComponentsHandler = new ComponentsHandler(this);
        RunUtil.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                doInWorkThread();
            }
        });
    }


    private void doInWorkThread() {
    }


    /**
     * hookSystemServices, but need to compatible with Android O in future.
     *
     * Android 7.1.1 一下 hook AMS
     *
     * AMS 的 远程 Binder Proxy 类 放在了 ActivityManagerNative 内
     */
    private void hookSystemServices() {
        /**
         * ActivityManagerNative 部分源码
         *
         * -----------------------------------------------------------------------------------------
         *
         * public abstract class ActivityManagerNative extends Binder implements IActivityManager{
         *
         *      ...
         *
         *      private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
         *          protected IActivityManager create() {
         *              IBinder b = ServiceManager.getService("activity");
         *              if (false) {
         *                  Log.v("ActivityManager", "default service binder = " + b);
         *              }
         *              IActivityManager am = asInterface(b);
         *              if (false) {
         *                  Log.v("ActivityManager", "default service = " + am);
         *              }
         *              return am;
         *          }
         *      };
         *
         *      ...
         *
         *
         * }
         *
         * -----------------------------------------------------------------------------------------
         *
         * Singleton 的类结构
         *
         * -----------------------------------------------------------------------------------------
         *
         * package android.util;
         *
         *  **
         *  * Singleton helper class for lazily initialization.
         *  *
         *  * Modeled after frameworks/base/include/utils/Singleton.h
         *  *
         *  * @hide
         *  **
         *
         * public abstract class Singleton<T> {
         *      private T mInstance;
         *
         *      protected abstract T create();
         *
         *      public final T get() {
         *          synchronized (this) {
         *              if (mInstance == null) {
         *                  mInstance = create();
         *              }
         *                  return mInstance;
         *          }
         *      }
         * }
         *
         * -----------------------------------------------------------------------------------------
         */
        try {
            Singleton<IActivityManager> defaultSingleton = (Singleton<IActivityManager>) ReflectUtil
                .getField(ActivityManagerNative.class, null, "gDefault");
            IActivityManager activityManagerProxy = ActivityManagerProxy.newInstance(this,
                defaultSingleton.get());

            // Hook IActivityManager from ActivityManagerNative
            ReflectUtil.setField(defaultSingleton.getClass().getSuperclass(), defaultSingleton,
                "mInstance", activityManagerProxy);

            if (defaultSingleton.get() == activityManagerProxy) {
                this.mActivityManager = activityManagerProxy;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Android 8.0.0 以上 hook AMS
     *
     * AMS 的 远程 Binder Proxy 类 放在了 ActivityManager 内
     */
    private void hookAMSForO() {
        /**
         * ActivityManager 部分源码
         *
         * -----------------------------------------------------------------------------------------
         *
         * public class ActivityManager {
         *
         *      ...
         *
         *      private static final Singleton<IActivityManager> IActivityManagerSingleton =
         *              new Singleton<IActivityManager>() {
         *                  @Override
         *                      protected IActivityManager create() {
         *                          final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
         *                          final IActivityManager am = IActivityManager.Stub.asInterface(b);
         *                          return am;
         *                      }
         *              };
         *
         *      ...
         *
         * }
         *
         * -----------------------------------------------------------------------------------------
         *
         * Singleton 的类结构
         *
         * -----------------------------------------------------------------------------------------
         *
         * package android.util;
         *
         *  **
         *  * Singleton helper class for lazily initialization.
         *  *
         *  * Modeled after frameworks/base/include/utils/Singleton.h
         *  *
         *  * @hide
         *  **
         *
         * public abstract class Singleton<T> {
         *      private T mInstance;
         *
         *      protected abstract T create();
         *
         *      public final T get() {
         *          synchronized (this) {
         *              if (mInstance == null) {
         *                  mInstance = create();
         *              }
         *                  return mInstance;
         *          }
         *      }
         * }
         *
         * -----------------------------------------------------------------------------------------
         */
        try {
            Singleton<IActivityManager> defaultSingleton = (Singleton<IActivityManager>) ReflectUtil
                .getField(ActivityManager.class, null, "IActivityManagerSingleton");
            IActivityManager activityManagerProxy = ActivityManagerProxy.newInstance(this,
                defaultSingleton.get());
            ReflectUtil.setField(defaultSingleton.getClass().getSuperclass(), defaultSingleton,
                "mInstance", activityManagerProxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Hook Instrumentation 和 ActivityThread # H # Handler.Callback mCallback
     *
     * 用 VAInstrumentation 去 hook
     * VAInstrumentation 继承了 Instrumentation 类，实现了 Handler.Callback 接口
     *
     * 唯一处理的地方就是
     * 判断 class full name 是否含有 lbe
     * 不能运行在 lbe 上
     */
    private void hookInstrumentationAndHandler() {
        try {
            Instrumentation baseInstrumentation = ReflectUtil.getInstrumentation(this.mContext);
            if (baseInstrumentation.getClass().getName().contains("lbe")) {
                // reject executing in paralell space, for example, lbe.
                System.exit(0);
            }

            final VAInstrumentation instrumentation = new VAInstrumentation(this,
                baseInstrumentation);
            Object activityThread = ReflectUtil.getActivityThread(this.mContext);
            ReflectUtil.setInstrumentation(activityThread, instrumentation);
            ReflectUtil.setHandlerCallback(this.mContext, instrumentation);
            this.mInstrumentation = instrumentation;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 用于动态代理 RemoteContentProvider 的远程 Binder proxy
     *
     * hook 点是
     *
     * ActivityThread # ArrayMap<ProviderKey, ProviderClientRecord> mProviderMap
     * ActivityThread # ProviderClientRecord # IContentProvider mProvider
     */
    private void hookIContentProviderAsNeeded() {
        Uri uri = Uri.parse(PluginContentResolver.getUri(mContext));
        mContext.getContentResolver().call(uri, "wakeup", null, null);
        try {
            Field authority = null;
            Field mProvider = null;
            ActivityThread activityThread = (ActivityThread) ReflectUtil.getActivityThread(
                mContext);
            Map mProviderMap = (Map) ReflectUtil.getField(activityThread.getClass(), activityThread,
                "mProviderMap");
            Iterator iter = mProviderMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object val = entry.getValue();
                String auth;
                if (key instanceof String) {
                    auth = (String) key;
                } else {
                    if (authority == null) {
                        authority = key.getClass().getDeclaredField("authority");
                        authority.setAccessible(true);
                    }
                    auth = (String) authority.get(key);
                }
                if (auth.equals(PluginContentResolver.getAuthority(mContext))) {
                    if (mProvider == null) {
                        mProvider = val.getClass().getDeclaredField("mProvider");
                        mProvider.setAccessible(true);
                    }
                    IContentProvider rawProvider = (IContentProvider) mProvider.get(val);
                    IContentProvider proxy = IContentProviderProxy.newInstance(mContext,
                        rawProvider);
                    mIContentProvider = proxy;
                    Log.d(TAG, "hookIContentProvider succeed : " + mIContentProvider);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * load a plugin into memory, then invoke it's Application.
     *
     * @param apk the file of plugin, should end with .apk
     * @throws Exception Exception
     *
     * 加载 Apk 文件
     *
     * 将 Apk 文件加载成 VA 识别的 LoadedPlugin
     */
    public void loadPlugin(File apk) throws Exception {
        if (null == apk) {
            throw new IllegalArgumentException("error : apk is null.");
        }

        if (!apk.exists()) {
            throw new FileNotFoundException(apk.getAbsolutePath());
        }

        LoadedPlugin plugin = LoadedPlugin.create(this, this.mContext, apk);
        if (null != plugin) {
            this.mPlugins.put(plugin.getPackageName(), plugin);
            // try to invoke plugin's application
            plugin.invokeApplication();
        } else {
            throw new RuntimeException(
                "Can't load plugin which is invalid: " + apk.getAbsolutePath());
        }
    }


    /**
     * 根据 Intent 抽取出对应的 LoadedPlugin
     * 可以理解为抽取出对应的 Apk 信息
     *
     * @param intent intent
     * @return LoadedPlugin
     */
    public LoadedPlugin getLoadedPlugin(Intent intent) {
        ComponentName component = PluginUtil.getComponent(intent);
        return getLoadedPlugin(component.getPackageName());
    }


    /**
     * 根据 ComponentName 抽取出对应的 LoadedPlugin
     * 可以理解为抽取出对应的 Apk 信息
     *
     * @param component component
     * @return LoadedPlugin
     */
    public LoadedPlugin getLoadedPlugin(ComponentName component) {
        return this.getLoadedPlugin(component.getPackageName());
    }


    /**
     * 根据 packageName 抽取出对应的 LoadedPlugin
     * 可以理解为抽取出对应的 Apk 信息
     *
     * @param packageName packageName
     * @return LoadedPlugin
     */
    public LoadedPlugin getLoadedPlugin(String packageName) {
        return this.mPlugins.get(packageName);
    }


    /**
     * 获取所有内存加载过的 Apk 信息
     * 即，LoadedPlugin
     *
     * @return List<LoadedPlugin>
     */
    public List<LoadedPlugin> getAllLoadedPlugins() {
        List<LoadedPlugin> list = new ArrayList<>();
        list.addAll(mPlugins.values());
        return list;
    }


    /**
     * 获取 宿主 Context
     *
     * @return Context
     */
    public Context getHostContext() {
        return this.mContext;
    }


    /**
     * 返回被 hook 掉的 Instrumentation
     * 就是原来的 Instrumentation
     *
     * 以便可以执行 非动态代理 原逻辑
     *
     * @return Instrumentation
     */
    public Instrumentation getInstrumentation() {
        return this.mInstrumentation;
    }


    /**
     * 返回被 hook 掉的 IActivityManager
     * 就是原来的 IActivityManager
     *
     * 以便可以执行 非动态代理 原逻辑
     *
     * @return IActivityManager
     */
    public IActivityManager getActivityManager() {
        return this.mActivityManager;
    }


    /**
     * 返回被 hook 掉的 RemoteContentProvider
     * 就是原来的 RemoteContentProvider
     *
     * 以便可以执行 非动态代理 原逻辑
     *
     * @return IContentProvider
     */
    public synchronized IContentProvider getIContentProvider() {
        if (mIContentProvider == null) {
            hookIContentProviderAsNeeded();
        }

        return mIContentProvider;
    }


    public ComponentsHandler getComponentsHandler() {
        return mComponentsHandler;
    }


    public ResolveInfo resolveActivity(Intent intent) {
        return this.resolveActivity(intent, 0);
    }


    /**
     * 从 所有内存加载过的 Apk 信息 中
     * 寻找是否有该 intent 对应的 Activity 信息
     *
     * @param intent intent
     * @param flags flags
     * @return ResolveInfo
     */
    public ResolveInfo resolveActivity(Intent intent, int flags) {
        for (LoadedPlugin plugin : this.mPlugins.values()) {
            ResolveInfo resolveInfo = plugin.resolveActivity(intent, flags);
            if (null != resolveInfo) {
                return resolveInfo;
            }
        }

        return null;
    }


    /**
     * 从 所有内存加载过的 Apk 信息 中
     * 寻找是否有该 intent 对应的 Service 信息
     *
     * @param intent intent
     * @param flags flags
     * @return ResolveInfo
     */
    public ResolveInfo resolveService(Intent intent, int flags) {
        for (LoadedPlugin plugin : this.mPlugins.values()) {
            ResolveInfo resolveInfo = plugin.resolveService(intent, flags);
            if (null != resolveInfo) {
                return resolveInfo;
            }
        }

        return null;
    }


    /**
     * 从 所有内存加载过的 Apk 信息 中
     * 寻找是否有该 协议 对应的 ContentProvider 信息
     *
     * @param name name
     * @param flags flags
     * @return ProviderInfo
     */
    public ProviderInfo resolveContentProvider(String name, int flags) {
        for (LoadedPlugin plugin : this.mPlugins.values()) {
            ProviderInfo providerInfo = plugin.resolveContentProvider(name, flags);
            if (null != providerInfo) {
                return providerInfo;
            }
        }

        return null;
    }


    /**
     * used in PluginPackageManager, do not invoke it from outside.
     *
     * 从 所有内存加载过的 Apk 信息 中
     * 寻找是否有该 intent 对应的 Activity 信息
     *
     * @param intent intent
     * @param flags flags
     * @return List<ResolveInfo>
     */
    @Deprecated
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();

        for (LoadedPlugin plugin : this.mPlugins.values()) {
            List<ResolveInfo> result = plugin.queryIntentActivities(intent, flags);
            if (null != result && result.size() > 0) {
                resolveInfos.addAll(result);
            }
        }

        return resolveInfos;
    }


    /**
     * used in PluginPackageManager, do not invoke it from outside.
     *
     * 从 所有内存加载过的 Apk 信息 中
     * 寻找是否有该 intent 对应的 Service 信息
     *
     * @param intent intent
     * @param flags flags
     * @return List<ResolveInfo>
     */
    @Deprecated
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();

        for (LoadedPlugin plugin : this.mPlugins.values()) {
            List<ResolveInfo> result = plugin.queryIntentServices(intent, flags);
            if (null != result && result.size() > 0) {
                resolveInfos.addAll(result);
            }
        }

        return resolveInfos;
    }


    /**
     * used in PluginPackageManager, do not invoke it from outside.
     *
     * 从 所有内存加载过的 Apk 信息 中
     * 寻找是否有该 intent 对应的 BroadcastReceiver 信息
     *
     * @param intent intent
     * @param flags flags
     * @return List<ResolveInfo>
     */
    @Deprecated
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();

        for (LoadedPlugin plugin : this.mPlugins.values()) {
            List<ResolveInfo> result = plugin.queryBroadcastReceivers(intent, flags);
            if (null != result && result.size() > 0) {
                resolveInfos.addAll(result);
            }
        }

        return resolveInfos;
    }

}