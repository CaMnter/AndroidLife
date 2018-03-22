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

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.didi.virtualapk.PluginManager;
import com.didi.virtualapk.utils.PluginUtil;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 动态代理 AMS
 *
 * @author johnsonlee
 */
public class ActivityManagerProxy implements InvocationHandler {

    private static final String TAG = "IActivityManagerProxy";

    public static final int INTENT_SENDER_BROADCAST = 1;
    public static final int INTENT_SENDER_ACTIVITY = 2;
    public static final int INTENT_SENDER_ACTIVITY_RESULT = 3;
    public static final int INTENT_SENDER_SERVICE = 4;


    public static IActivityManager newInstance(PluginManager pluginManager, IActivityManager activityManager) {
        return (IActivityManager) Proxy.newProxyInstance(
            activityManager.getClass().getClassLoader(), new Class[] { IActivityManager.class },
            new ActivityManagerProxy(pluginManager, activityManager));
    }


    private PluginManager mPluginManager;
    private IActivityManager mActivityManager;


    public ActivityManagerProxy(PluginManager pluginManager, IActivityManager activityManager) {
        this.mPluginManager = pluginManager;
        this.mActivityManager = activityManager;
    }


    /**
     * 动态代理
     *
     * IActivityManager # startService
     * IActivityManager # stopService
     * IActivityManager # stopServiceToken
     * IActivityManager # bindService
     * IActivityManager # unbindService
     * IActivityManager # getIntentSender
     * IActivityManager # overridePendingTransition
     *
     * @param proxy proxy
     * @param method method
     * @param args args
     * @return 返回值
     * @throws Throwable Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startService".equals(method.getName())) {
            try {
                return startService(proxy, method, args);
            } catch (Throwable e) {
                Log.e(TAG, "Start service error", e);
            }
        } else if ("stopService".equals(method.getName())) {
            try {
                return stopService(proxy, method, args);
            } catch (Throwable e) {
                Log.e(TAG, "Stop Service error", e);
            }
        } else if ("stopServiceToken".equals(method.getName())) {
            try {
                return stopServiceToken(proxy, method, args);
            } catch (Throwable e) {
                Log.e(TAG, "Stop service token error", e);
            }
        } else if ("bindService".equals(method.getName())) {
            try {
                return bindService(proxy, method, args);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if ("unbindService".equals(method.getName())) {
            try {
                return unbindService(proxy, method, args);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if ("getIntentSender".equals(method.getName())) {
            try {
                getIntentSender(method, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("overridePendingTransition".equals(method.getName())) {
            try {
                overridePendingTransition(method, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            // sometimes system binder has problems.
            return method.invoke(this.mActivityManager, args);
        } catch (Throwable th) {
            Throwable c = th.getCause();
            if (c != null && c instanceof DeadObjectException) {
                // retry connect to system binder
                IBinder ams = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                if (ams != null) {
                    IActivityManager am = ActivityManagerNative.asInterface(ams);
                    mActivityManager = am;
                }
            }

            Throwable cause = th;
            do {
                if (cause instanceof RemoteException) {
                    throw cause;
                }
            } while ((cause = cause.getCause()) != null);

            throw c != null ? c : th;
        }

    }


    /**
     * 动态代理 IActivityManager # startService
     *
     * 根据 intent 内的 Component
     * 查询是否存在对应的 插件 Service 信息
     *
     * 没的话，正常调用 IActivityManager # startService
     *
     * 有的话，得修改 intent 内的信息
     * 将插件 Service Intent 替换为 插桩 Service Intent
     * 然后 插件 Service Intent  作为 插桩 Service Intent 的数据存放
     *
     * 存放的插件 Service 行为记录为 startService
     *
     * 去启动 插桩 Service
     *
     * @param proxy proxy
     * @param method method
     * @param args args
     * @return 返回值
     * @throws Throwable Throwable
     */
    private Object startService(Object proxy, Method method, Object[] args) throws Throwable {
        IApplicationThread appThread = (IApplicationThread) args[0];
        Intent target = (Intent) args[1];
        ResolveInfo resolveInfo = this.mPluginManager.resolveService(target, 0);
        if (null == resolveInfo || null == resolveInfo.serviceInfo) {
            // is host service
            return method.invoke(this.mActivityManager, args);
        }

        return startDelegateServiceForTarget(target, resolveInfo.serviceInfo, null,
            RemoteService.EXTRA_COMMAND_START_SERVICE);
    }


    /**
     * 动态代理 IActivityManager # stopService
     *
     * 根据 intent 内的 Component
     * 查询是否存在对应的 插件 Service 信息
     *
     * 没的话，正常调用 IActivityManager # startService
     *
     * 有的话，得修改 intent 内的信息
     * 将插件 Service Intent 替换为 插桩 Service Intent
     * 然后 插件 Service Intent  作为 插桩 Service Intent 的数据存放
     *
     * 存放的插件 Service 行为记录为 stopService
     *
     * 去启动 插桩 Service
     *
     * @param proxy proxy
     * @param method method
     * @param args args
     * @return 返回值
     * @throws Throwable Throwable
     */
    private Object stopService(Object proxy, Method method, Object[] args) throws Throwable {
        Intent target = (Intent) args[1];
        ResolveInfo resolveInfo = this.mPluginManager.resolveService(target, 0);
        if (null == resolveInfo || null == resolveInfo.serviceInfo) {
            // is hot service
            return method.invoke(this.mActivityManager, args);
        }

        startDelegateServiceForTarget(target, resolveInfo.serviceInfo, null,
            RemoteService.EXTRA_COMMAND_STOP_SERVICE);
        return 1;
    }


    /**
     * 动态代理 IActivityManager # stopServiceToken
     *
     * 根据 intent 内的 Component
     * 查询是否存在对应的 插件 Service 信息
     *
     * 没的话，正常调用 IActivityManager # startService
     *
     * 有的话，得修改 intent 内的信息
     * 将插件 Service Intent 替换为 插桩 Service Intent
     * 然后 插件 Service Intent  作为 插桩 Service Intent 的数据存放
     *
     * 存放的插件 Service 行为记录为 stopServiceToken
     *
     * 去启动 插桩 Service
     *
     * @param proxy proxy
     * @param method method
     * @param args args
     * @return 返回值
     * @throws Throwable Throwable
     */
    private Object stopServiceToken(Object proxy, Method method, Object[] args) throws Throwable {
        ComponentName component = (ComponentName) args[0];
        Intent target = new Intent().setComponent(component);
        ResolveInfo resolveInfo = this.mPluginManager.resolveService(target, 0);
        if (null == resolveInfo || null == resolveInfo.serviceInfo) {
            // is hot service
            return method.invoke(this.mActivityManager, args);
        }

        startDelegateServiceForTarget(target, resolveInfo.serviceInfo, null,
            RemoteService.EXTRA_COMMAND_STOP_SERVICE);
        return true;
    }


    /**
     * 动态代理 IActivityManager # bindService
     *
     * 根据 intent 内的 Component
     * 查询是否存在对应的 插件 Service 信息
     *
     * 没的话，正常调用 IActivityManager # startService
     *
     * 有的话，得修改 intent 内的信息
     * 将插件 Service Intent 替换为 插桩 Service Intent
     * 然后 插件 Service Intent  作为 插桩 Service Intent 的数据存放
     *
     * 存放的插件 Service 行为记录为 bindService
     *
     * 去启动 插桩 Service
     *
     * @param proxy proxy
     * @param method method
     * @param args args
     * @return 返回值
     * @throws Throwable Throwable
     */
    private Object bindService(Object proxy, Method method, Object[] args) throws Throwable {
        Intent target = (Intent) args[2];
        ResolveInfo resolveInfo = this.mPluginManager.resolveService(target, 0);
        if (null == resolveInfo || null == resolveInfo.serviceInfo) {
            // is host service
            return method.invoke(this.mActivityManager, args);
        }

        Bundle bundle = new Bundle();
        PluginUtil.putBinder(bundle, "sc", (IBinder) args[4]);
        startDelegateServiceForTarget(target, resolveInfo.serviceInfo, bundle,
            RemoteService.EXTRA_COMMAND_BIND_SERVICE);
        mPluginManager.getComponentsHandler().remberIServiceConnection((IBinder) args[4], target);
        return 1;
    }


    /**
     * 动态代理 IActivityManager # unbindService
     *
     * 根据 intent 内的 Component
     * 查询是否存在对应的 插件 Service 信息
     *
     * 没的话，正常调用 IActivityManager # startService
     *
     * 有的话，得修改 intent 内的信息
     * 将插件 Service Intent 替换为 插桩 Service Intent
     * 然后 插件 Service Intent  作为 插桩 Service Intent 的数据存放
     *
     * 存放的插件 Service 行为记录为 unbindService
     *
     * 去启动 插桩 Service
     *
     * @param proxy proxy
     * @param method method
     * @param args args
     * @return 返回值
     * @throws Throwable Throwable
     */
    private Object unbindService(Object proxy, Method method, Object[] args) throws Throwable {
        IBinder iServiceConnection = (IBinder) args[0];
        Intent target = mPluginManager.getComponentsHandler()
            .forgetIServiceConnection(iServiceConnection);
        if (target == null) {
            // is host service
            return method.invoke(this.mActivityManager, args);
        }

        ResolveInfo resolveInfo = this.mPluginManager.resolveService(target, 0);
        startDelegateServiceForTarget(target, resolveInfo.serviceInfo, null,
            RemoteService.EXTRA_COMMAND_UNBIND_SERVICE);
        return true;
    }


    /**
     * 主要完成
     *
     * wrapperTargetIntent(...) 修改完 Intent 信息后
     * 启动插桩 Service
     *
     * @param target target
     * @param serviceInfo serviceInfo
     * @param extras extras
     * @param command command
     * @return ComponentName
     */
    private ComponentName startDelegateServiceForTarget(Intent target, ServiceInfo serviceInfo, Bundle extras, int command) {
        Intent wrapperIntent = wrapperTargetIntent(target, serviceInfo, extras, command);
        return mPluginManager.getHostContext().startService(wrapperIntent);
    }


    /**
     * 主要完成
     *
     * 将插件 Service Intent 替换为 插桩 Service Intent
     * 然后 插件 Service Intent  作为 插桩 Service Intent 的数据存放
     *
     * 还有存放
     * 插件 Service 的 行为（ start, stop 等 ）
     * 插件 Service 所属 apk 的绝对路径
     * 插件 Service Intent 的全部数据
     *
     * @param target target
     * @param serviceInfo serviceInfo
     * @param extras extras
     * @param command command
     * @return Intent
     */
    private Intent wrapperTargetIntent(Intent target, ServiceInfo serviceInfo, Bundle extras, int command) {
        // fill in service with ComponentName
        target.setComponent(new ComponentName(serviceInfo.packageName, serviceInfo.name));
        String pluginLocation = mPluginManager.getLoadedPlugin(target.getComponent()).getLocation();

        // start delegate service to run plugin service inside
        boolean local = PluginUtil.isLocalService(serviceInfo);
        Class<? extends Service> delegate = local ? LocalService.class : RemoteService.class;
        Intent intent = new Intent();
        intent.setClass(mPluginManager.getHostContext(), delegate);
        intent.putExtra(RemoteService.EXTRA_TARGET, target);
        intent.putExtra(RemoteService.EXTRA_COMMAND, command);
        intent.putExtra(RemoteService.EXTRA_PLUGIN_LOCATION, pluginLocation);
        if (extras != null) {
            intent.putExtras(extras);
        }

        return intent;
    }


    /**
     * 动态代理 IActivityManager # getIntentSender
     *
     * PendingIntent 内，有不少方法调用 IActivityManager # getIntentSender
     * 不动态代理的话，拿不到插件 Service 或者 插件 Activity 的相关信息
     *
     * 当然不止 PendingIntent 内，其他地方的 IActivityManager # getIntentSender 也会有问题
     *
     * @param method method
     * @param args args
     */
    private void getIntentSender(Method method, Object[] args) {
        String hostPackageName = mPluginManager.getHostContext().getPackageName();
        args[1] = hostPackageName;

        Intent target = ((Intent[]) args[5])[0];
        int intentSenderType = (int) args[0];
        if (intentSenderType == INTENT_SENDER_ACTIVITY) {
            mPluginManager.getComponentsHandler().transformIntentToExplicitAsNeeded(target);
            mPluginManager.getComponentsHandler().markIntentIfNeeded(target);
        } else if (intentSenderType == INTENT_SENDER_SERVICE) {
            ResolveInfo resolveInfo = this.mPluginManager.resolveService(target, 0);
            if (resolveInfo != null && resolveInfo.serviceInfo != null) {
                // find plugin service
                Intent wrapperIntent = wrapperTargetIntent(target, resolveInfo.serviceInfo, null,
                    RemoteService.EXTRA_COMMAND_START_SERVICE);
                ((Intent[]) args[5])[0] = wrapperIntent;
            }
        } else if (intentSenderType == INTENT_SENDER_BROADCAST) {
            // no action
        }
    }


    /**
     * 动态代理 IActivityManager # overridePendingTransition
     *
     * 替换 参数 (IBinder token, String packageName, int enterAnim, int exitAnim)
     * 中的 packageName
     *
     * @param method method
     * @param args args
     */
    private void overridePendingTransition(Method method, Object[] args) {
        String hostPackageName = mPluginManager.getHostContext().getPackageName();
        args[1] = hostPackageName;
    }

}
