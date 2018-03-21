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

package com.didi.virtualapk.delegate;

import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.didi.virtualapk.PluginManager;
import com.didi.virtualapk.internal.LoadedPlugin;
import com.didi.virtualapk.utils.PluginUtil;
import com.didi.virtualapk.utils.ReflectUtil;
import java.lang.reflect.Method;

/**
 * 插桩 Service
 * 一般用于 App 进程，意味着不是独立进程 插桩 Service
 *
 * 但是由于独立进程 和 非独立进程的 插件 Service ，在 create, onStart, onStop 方面的 反射逻辑
 * 都是一样的
 *
 * 只是在哪个进程进行 Service 的 create, onStart, onStop
 * 造就了插件 Service 所在进程是 独立进程 或 非独立进程
 *
 * 所以，RemoteService 也调用 LocalService 内的写的全部 反射代码
 *
 * @author johnsonlee
 */
public class LocalService extends Service {
    private static final String TAG = "LocalService";

    /**
     * The target service, usually it's a plugin service intent
     */
    public static final String EXTRA_TARGET = "target";
    public static final String EXTRA_COMMAND = "command";
    public static final String EXTRA_PLUGIN_LOCATION = "plugin_location";

    public static final int EXTRA_COMMAND_START_SERVICE = 1;
    public static final int EXTRA_COMMAND_STOP_SERVICE = 2;
    public static final int EXTRA_COMMAND_BIND_SERVICE = 3;
    public static final int EXTRA_COMMAND_UNBIND_SERVICE = 4;

    private PluginManager mPluginManager;


    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mPluginManager = PluginManager.getInstance(this);
    }


    /**
     * 分发插件 service action
     *
     * 1. 先处理 Service START_STICKY 的情况
     * 2. 插桩 service intent 中抽出 插件 service intent
     * 3. 根据 插件 service intent 获取到对应的 LoadedPlugin
     * 4. 替换 插件 service intent 中的 classloader
     * 5. 分发插件 service action
     * 6. 分发插件 service action 前，都得检查是否要 反射创建 service
     *
     * 关于 start  插件 service: 需要反射调用 Service # attach(...)，然后手动调用插件 Service # onCreate(...)
     * 关于 bind   插件 service: 同 start，还需要反射调用插件 Service # onBind(...) 和 service 对应的
     * -           IServiceConnection # connected(...)
     * 关于 stop   插件 service: 手动调用插件 Service # onDestroy()
     * 关于 unbind 插件 service: 手动调用插件 Service # onUnbind(...) 和 Service # onDestroy()
     *
     * start 和 bind，  都得考虑是否要添加 插件 service 缓存
     * stop  和 unbind，都得考虑是否要删除 插件 service 缓存
     *
     * @param intent intent
     * @param flags flags
     * @param startId startId
     * @return int
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent || !intent.hasExtra(EXTRA_TARGET) || !intent.hasExtra(EXTRA_COMMAND)) {
            return START_STICKY;
        }

        Intent target = intent.getParcelableExtra(EXTRA_TARGET);
        int command = intent.getIntExtra(EXTRA_COMMAND, 0);
        if (null == target || command <= 0) {
            return START_STICKY;
        }

        ComponentName component = target.getComponent();
        LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
        // ClassNotFoundException when unmarshalling in Android 5.1
        target.setExtrasClassLoader(plugin.getClassLoader());
        switch (command) {
            case EXTRA_COMMAND_START_SERVICE: {
                ActivityThread mainThread = (ActivityThread) ReflectUtil.getActivityThread(
                    getBaseContext());
                IApplicationThread appThread = mainThread.getApplicationThread();
                Service service;

                if (this.mPluginManager.getComponentsHandler().isServiceAvailable(component)) {
                    service = this.mPluginManager.getComponentsHandler().getService(component);
                } else {
                    try {
                        service = (Service) plugin.getClassLoader()
                            .loadClass(component.getClassName())
                            .newInstance();

                        Application app = plugin.getApplication();
                        IBinder token = appThread.asBinder();
                        Method attach = service.getClass()
                            .getMethod("attach", Context.class, ActivityThread.class, String.class,
                                IBinder.class, Application.class, Object.class);
                        IActivityManager am = mPluginManager.getActivityManager();

                        attach.invoke(service, plugin.getPluginContext(), mainThread,
                            component.getClassName(), token, app, am);
                        service.onCreate();
                        this.mPluginManager.getComponentsHandler()
                            .rememberService(component, service);
                    } catch (Throwable t) {
                        return START_STICKY;
                    }
                }

                service.onStartCommand(target, 0, this.mPluginManager.getComponentsHandler()
                    .getServiceCounter(service)
                    .getAndIncrement());
                break;
            }
            case EXTRA_COMMAND_BIND_SERVICE: {
                ActivityThread mainThread = (ActivityThread) ReflectUtil.getActivityThread(
                    getBaseContext());
                IApplicationThread appThread = mainThread.getApplicationThread();
                Service service = null;

                if (this.mPluginManager.getComponentsHandler().isServiceAvailable(component)) {
                    service = this.mPluginManager.getComponentsHandler().getService(component);
                } else {
                    try {
                        service = (Service) plugin.getClassLoader()
                            .loadClass(component.getClassName())
                            .newInstance();

                        Application app = plugin.getApplication();
                        IBinder token = appThread.asBinder();
                        Method attach = service.getClass()
                            .getMethod("attach", Context.class, ActivityThread.class, String.class,
                                IBinder.class, Application.class, Object.class);
                        IActivityManager am = mPluginManager.getActivityManager();

                        attach.invoke(service, plugin.getPluginContext(), mainThread,
                            component.getClassName(), token, app, am);
                        service.onCreate();
                        this.mPluginManager.getComponentsHandler()
                            .rememberService(component, service);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                try {
                    IBinder binder = service.onBind(target);
                    IBinder serviceConnection = PluginUtil.getBinder(intent.getExtras(), "sc");
                    IServiceConnection iServiceConnection = IServiceConnection.Stub.asInterface(
                        serviceConnection);
                    if (Build.VERSION.SDK_INT >= 26) {
                        ReflectUtil.invokeNoException(IServiceConnection.class, iServiceConnection,
                            "connected",
                            new Class[] { ComponentName.class, IBinder.class, boolean.class },
                            new Object[] { component, binder, false });
                    } else {
                        iServiceConnection.connected(component, binder);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case EXTRA_COMMAND_STOP_SERVICE: {
                Service service = this.mPluginManager.getComponentsHandler()
                    .forgetService(component);
                if (null != service) {
                    try {
                        service.onDestroy();
                    } catch (Exception e) {
                        Log.e(TAG, "Unable to stop service " + service + ": " + e.toString());
                    }
                } else {
                    Log.i(TAG, component + " not found");
                }
                break;
            }
            case EXTRA_COMMAND_UNBIND_SERVICE: {
                Service service = this.mPluginManager.getComponentsHandler()
                    .forgetService(component);
                if (null != service) {
                    try {
                        service.onUnbind(target);
                        service.onDestroy();
                    } catch (Exception e) {
                        Log.e(TAG, "Unable to unbind service " + service + ": " + e.toString());
                    }
                } else {
                    Log.i(TAG, component + " not found");
                }
                break;
            }
        }

        return START_STICKY;
    }

}
