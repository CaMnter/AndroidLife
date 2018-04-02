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

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.VersionedPackage;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.WorkerThread;
import com.didi.virtualapk.PluginManager;
import com.didi.virtualapk.utils.DexUtil;
import com.didi.virtualapk.utils.PackageParserCompat;
import com.didi.virtualapk.utils.PluginUtil;
import com.didi.virtualapk.utils.ReflectUtil;
import com.didi.virtualapk.utils.RunUtil;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Apk 文件在 VirtualApk 中的内存格式
 *
 * Created by renyugang on 16/8/9.
 */
public final class LoadedPlugin {

    public static final String TAG = "LoadedPlugin";


    public static LoadedPlugin create(PluginManager pluginManager, Context host, File apk)
        throws Exception {
        return new LoadedPlugin(pluginManager, host, apk);
    }


    /**
     * 构造一个加载该 Apk 的 classloader
     *
     * 然后把这个 classloader 的 Element 插入宿主的 DexPathList # Element[] dexElements 中
     * 宿主 classloader 也可以访问该 Apk 内的 class
     *
     * @param context context
     * @param apk apk
     * @param libsDir libsDir
     * @param parent parent
     * @return ClassLoader
     */
    private static ClassLoader createClassLoader(Context context, File apk, File libsDir, ClassLoader parent) {
        File dexOutputDir = context.getDir(Constants.OPTIMIZE_DIR, Context.MODE_PRIVATE);
        String dexOutputPath = dexOutputDir.getAbsolutePath();
        DexClassLoader loader = new DexClassLoader(apk.getAbsolutePath(), dexOutputPath,
            libsDir.getAbsolutePath(), parent);

        if (Constants.COMBINE_CLASSLOADER) {
            try {
                DexUtil.insertDex(loader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return loader;
    }


    /**
     * 反射创建一个 插件 AssetManager
     *
     * @param context context
     * @param apk apk
     * @return AssetManager
     */
    private static AssetManager createAssetManager(Context context, File apk) {
        try {
            AssetManager am = AssetManager.class.newInstance();
            ReflectUtil.invoke(AssetManager.class, am, "addAssetPath", apk.getAbsolutePath());
            return am;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 根据需要 反射创建 Resources
     *
     * 可以选择反射创建 复合资源，就是 Resources 包含和 所有插件 Resources + 宿主 Resources
     * 还可悬着反射创建 单一资源，就是仅仅是该插件的 Resources
     *
     * @param context context
     * @param apk apk
     * @return Resources
     */
    @WorkerThread
    private static Resources createResources(Context context, File apk) {
        if (Constants.COMBINE_RESOURCES) {
            Resources resources = ResourcesManager.createResources(context, apk.getAbsolutePath());
            ResourcesManager.hookResources(context, resources);
            return resources;
        } else {
            Resources hostResources = context.getResources();
            AssetManager assetManager = createAssetManager(context, apk);
            return new Resources(assetManager, hostResources.getDisplayMetrics(),
                hostResources.getConfiguration());
        }
    }


    private static ResolveInfo chooseBestActivity(Intent intent, String s, int flags, List<ResolveInfo> query) {
        return query.get(0);
    }


    /**
     * Apk 的 绝对路径
     */
    private final String mLocation;
    /**
     * Apk 的 对应 PluginManager
     * 里面有被 hook 掉的 原数据缓存
     */
    private PluginManager mPluginManager;
    /**
     * 宿主 context
     */
    private Context mHostContext;
    /**
     * 自定义的 插件 context
     * 内部有部分功能是选择用宿主 context 调用的
     * 可以认为是访问宿主的信息
     *
     * 还有部分是用插件 context 去调用的
     * 就是访问插件的信息
     */
    private Context mPluginContext;
    /**
     * native lib 文件夹路径
     * so lib 文件夹路径
     */
    private final File mNativeLibDir;
    /**
     * Apk 文件解析后的 PackageParser.Package
     * 可以拿到插件 activity, service, content provider, broadcast receiver 等信息
     */
    private final PackageParser.Package mPackage;
    /**
     * 新建的一个 PackageInfo
     * 里面的信息从 PackageParser.Package 中搬取
     */
    private final PackageInfo mPackageInfo;
    /**
     * 该 Apk 内存形式上 所持有的资源
     * 可能是 复合资源（ 所以插件资源 + 宿主资源 ）
     * 可能是 插件单一资源
     * 根据配置
     */
    private Resources mResources;
    /**
     * new 的一个 DexClassloader
     * 用于加载插件
     *
     * 同时，将该 classloader 的 Element 也插入到宿主 的 DexPathList # Element[] dexElements 中
     * 宿主的 classloader 就整合成了 可以访问该插件 class 的 classloader
     */
    private ClassLoader mClassLoader;
    /**
     * 自定义的插件 PluginPackageManager
     */
    private PluginPackageManager mPackageManager;

    /**
     * 插件的 activity 信息
     * 需要在 插件 apk 的 AndManifest 内配置了 才生效
     */
    private Map<ComponentName, ActivityInfo> mActivityInfos;
    /**
     * 插件的 service 信息
     * 需要在 插件 apk 的 AndManifest 内配置了 才生效
     */
    private Map<ComponentName, ServiceInfo> mServiceInfos;
    /**
     * 插件的 receiver 信息
     * 需要在 插件 apk 的 AndManifest 内配置了 才生效
     */
    private Map<ComponentName, ActivityInfo> mReceiverInfos;
    /**
     * 插件的 provider 信息
     * 需要在 插件 apk 的 AndManifest 内配置了 才生效
     */
    private Map<ComponentName, ProviderInfo> mProviderInfos;
    /**
     * 根据 协议 缓存插件 Provider
     */
    private Map<String, ProviderInfo> mProviders; // key is authorities of provider

    /**
     * Apk 解析出来的 PackageParser.Instrumentation 缓存
     */
    private Map<ComponentName, InstrumentationInfo> mInstrumentationInfos;

    /**
     * 反射创建出来的 Application
     * 这个 Application
     * 用的是被 hook Instrumentation 去反射创建
     */
    private Application mApplication;


    /**
     * 初始化 关于 Apk 的很多信息
     *
     * @param pluginManager pluginManager
     * @param context context
     * @param apk apk
     * @throws PackageParser.PackageParserException
     */
    LoadedPlugin(PluginManager pluginManager, Context context, File apk)
        throws PackageParser.PackageParserException {
        this.mPluginManager = pluginManager;
        this.mHostContext = context;
        this.mLocation = apk.getAbsolutePath();
        this.mPackage = PackageParserCompat.parsePackage(context, apk,
            PackageParser.PARSE_MUST_BE_APK);
        this.mPackage.applicationInfo.metaData = this.mPackage.mAppMetaData;
        this.mPackageInfo = new PackageInfo();
        this.mPackageInfo.applicationInfo = this.mPackage.applicationInfo;
        this.mPackageInfo.applicationInfo.sourceDir = apk.getAbsolutePath();
        this.mPackageInfo.signatures = this.mPackage.mSignatures;
        this.mPackageInfo.packageName = this.mPackage.packageName;
        if (pluginManager.getLoadedPlugin(mPackageInfo.packageName) != null) {
            throw new RuntimeException(
                "plugin has already been loaded : " + mPackageInfo.packageName);
        }
        this.mPackageInfo.versionCode = this.mPackage.mVersionCode;
        this.mPackageInfo.versionName = this.mPackage.mVersionName;
        this.mPackageInfo.permissions = new PermissionInfo[0];
        this.mPackageManager = new PluginPackageManager();
        this.mPluginContext = new PluginContext(this);
        this.mNativeLibDir = context.getDir(Constants.NATIVE_DIR, Context.MODE_PRIVATE);
        this.mResources = createResources(context, apk);
        this.mClassLoader = createClassLoader(context, apk, this.mNativeLibDir,
            context.getClassLoader());

        tryToCopyNativeLib(apk);

        // Cache instrumentations
        Map<ComponentName, InstrumentationInfo> instrumentations
            = new HashMap<ComponentName, InstrumentationInfo>();
        for (PackageParser.Instrumentation instrumentation : this.mPackage.instrumentation) {
            instrumentations.put(instrumentation.getComponentName(), instrumentation.info);
        }
        this.mInstrumentationInfos = Collections.unmodifiableMap(instrumentations);
        this.mPackageInfo.instrumentation = instrumentations.values()
            .toArray(new InstrumentationInfo[instrumentations.size()]);

        // Cache activities
        Map<ComponentName, ActivityInfo> activityInfos = new HashMap<ComponentName, ActivityInfo>();
        for (PackageParser.Activity activity : this.mPackage.activities) {
            activityInfos.put(activity.getComponentName(), activity.info);
        }
        this.mActivityInfos = Collections.unmodifiableMap(activityInfos);
        this.mPackageInfo.activities = activityInfos.values()
            .toArray(new ActivityInfo[activityInfos.size()]);

        // Cache services
        Map<ComponentName, ServiceInfo> serviceInfos = new HashMap<ComponentName, ServiceInfo>();
        for (PackageParser.Service service : this.mPackage.services) {
            serviceInfos.put(service.getComponentName(), service.info);
        }
        this.mServiceInfos = Collections.unmodifiableMap(serviceInfos);
        this.mPackageInfo.services = serviceInfos.values()
            .toArray(new ServiceInfo[serviceInfos.size()]);

        // Cache providers
        Map<String, ProviderInfo> providers = new HashMap<String, ProviderInfo>();
        Map<ComponentName, ProviderInfo> providerInfos = new HashMap<ComponentName, ProviderInfo>();
        for (PackageParser.Provider provider : this.mPackage.providers) {
            providers.put(provider.info.authority, provider.info);
            providerInfos.put(provider.getComponentName(), provider.info);
        }
        this.mProviders = Collections.unmodifiableMap(providers);
        this.mProviderInfos = Collections.unmodifiableMap(providerInfos);
        this.mPackageInfo.providers = providerInfos.values()
            .toArray(new ProviderInfo[providerInfos.size()]);

        // Register broadcast receivers dynamically
        Map<ComponentName, ActivityInfo> receivers = new HashMap<ComponentName, ActivityInfo>();
        for (PackageParser.Activity receiver : this.mPackage.receivers) {
            receivers.put(receiver.getComponentName(), receiver.info);

            try {
                BroadcastReceiver br = BroadcastReceiver.class.cast(
                    getClassLoader().loadClass(receiver.getComponentName().getClassName())
                        .newInstance());
                for (PackageParser.ActivityIntentInfo aii : receiver.intents) {
                    this.mHostContext.registerReceiver(br, aii);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mReceiverInfos = Collections.unmodifiableMap(receivers);
        this.mPackageInfo.receivers = receivers.values()
            .toArray(new ActivityInfo[receivers.size()]);
    }


    /**
     * 尝试复制 native lib
     *
     * @param apk apk
     */
    private void tryToCopyNativeLib(File apk) {
        Bundle metaData = this.mPackageInfo.applicationInfo.metaData;
        if (metaData != null && metaData.getBoolean("VA_IS_HAVE_LIB")) {
            PluginUtil.copyNativeLib(apk, mHostContext, mPackageInfo, mNativeLibDir);
        }
    }


    public String getLocation() {
        return this.mLocation;
    }


    public String getPackageName() {
        return this.mPackage.packageName;
    }


    public PackageManager getPackageManager() {
        return this.mPackageManager;
    }


    public AssetManager getAssets() {
        return getResources().getAssets();
    }


    public Resources getResources() {
        return this.mResources;
    }


    public void updateResources(Resources newResources) {
        this.mResources = newResources;
    }


    public ClassLoader getClassLoader() {
        return this.mClassLoader;
    }


    public PluginManager getPluginManager() {
        return this.mPluginManager;
    }


    public Context getHostContext() {
        return this.mHostContext;
    }


    public Context getPluginContext() {
        return this.mPluginContext;
    }


    public Application getApplication() {
        return mApplication;
    }


    /**
     * 初始化 Application
     * 这个 Application
     * 用的是被 hook Instrumentation 去反射创建
     */
    public void invokeApplication() {
        if (mApplication != null) {
            return;
        }

        // make sure application's callback is run on ui thread.
        RunUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mApplication = makeApplication(false, mPluginManager.getInstrumentation());
            }
        }, true);
    }


    public String getPackageResourcePath() {
        int myUid = Process.myUid();
        ApplicationInfo appInfo = this.mPackage.applicationInfo;
        return appInfo.uid == myUid ? appInfo.sourceDir : appInfo.publicSourceDir;
    }


    public String getCodePath() {
        return this.mPackage.applicationInfo.sourceDir;
    }


    /**
     * 获取插件 Apk 中的 launch activity 的 intent
     *
     * @return Intent
     */
    public Intent getLaunchIntent() {
        ContentResolver resolver = this.mPluginContext.getContentResolver();
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

        for (PackageParser.Activity activity : this.mPackage.activities) {
            for (PackageParser.ActivityIntentInfo intentInfo : activity.intents) {
                if (intentInfo.match(resolver, launcher, false, TAG) > 0) {
                    return Intent.makeMainActivity(activity.getComponentName());
                }
            }
        }

        return null;
    }


    /**
     * 获取插件 Apk 中的 leanback launch activity 的 intent
     *
     * @return Intent
     */
    public Intent getLeanbackLaunchIntent() {
        ContentResolver resolver = this.mPluginContext.getContentResolver();
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(
            Intent.CATEGORY_LEANBACK_LAUNCHER);

        for (PackageParser.Activity activity : this.mPackage.activities) {
            for (PackageParser.ActivityIntentInfo intentInfo : activity.intents) {
                if (intentInfo.match(resolver, launcher, false, TAG) > 0) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(activity.getComponentName());
                    intent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                    return intent;
                }
            }
        }

        return null;
    }


    /**
     * 获取 插件 apk 的 ApplicationInfo
     * 从 PackageParser.Package 中抽取
     *
     * @return ApplicationInfo
     */
    public ApplicationInfo getApplicationInfo() {
        return this.mPackage.applicationInfo;
    }


    public PackageInfo getPackageInfo() {
        return this.mPackageInfo;
    }


    /**
     * 根据 ComponentName 获取插件 Apk 中的对应的 ActivityInfo
     *
     * @param componentName componentName
     * @return ActivityInfo
     */
    public ActivityInfo getActivityInfo(ComponentName componentName) {
        return this.mActivityInfos.get(componentName);
    }


    /**
     * 根据 ComponentName 获取插件 Apk 中的对应的 ServiceInfo
     *
     * @param componentName componentName
     * @return ServiceInfo
     */
    public ServiceInfo getServiceInfo(ComponentName componentName) {
        return this.mServiceInfos.get(componentName);
    }


    /**
     * 根据 ComponentName 获取插件 Apk 中的对应的 ReceiverInfo ( ActivityInfo )
     *
     * @param componentName componentName
     * @return ActivityInfo
     */
    public ActivityInfo getReceiverInfo(ComponentName componentName) {
        return this.mReceiverInfos.get(componentName);
    }


    /**
     * 根据 ComponentName 获取插件 Apk 中的对应的 ProviderInfo
     *
     * @param componentName componentName
     * @return ProviderInfo
     */
    public ProviderInfo getProviderInfo(ComponentName componentName) {
        return this.mProviderInfos.get(componentName);
    }


    /**
     * 获取插件 Theme
     *
     * @return Resources.Theme
     */
    public Resources.Theme getTheme() {
        Resources.Theme theme = this.mResources.newTheme();
        theme.applyStyle(PluginUtil.selectDefaultTheme(this.mPackage.applicationInfo.theme,
            Build.VERSION.SDK_INT), false);
        return theme;
    }


    /**
     * hook 插件 Resources 中的 Theme res id
     *
     * @param resid resid
     */
    public void setTheme(int resid) {
        try {
            ReflectUtil.setField(Resources.class, this.mResources, "mThemeResId", resid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 反射 创建 Application
     *
     * 这个 Application
     * 用的是被 hook Instrumentation 去反射创建
     *
     * 这的 Instrumentation，就是被 VAInstrumentation hook 后，替换下来的 原生
     * Instrumentation
     *
     * 还得手动回调一次 Instrumentation # callApplicationOnCreate(Application app)
     *
     * @param forceDefaultAppClass forceDefaultAppClass
     * @param instrumentation instrumentation
     * @return Application
     */
    private Application makeApplication(boolean forceDefaultAppClass, Instrumentation instrumentation) {
        if (null != this.mApplication) {
            return this.mApplication;
        }

        String appClass = this.mPackage.applicationInfo.className;
        if (forceDefaultAppClass || null == appClass) {
            appClass = "android.app.Application";
        }

        try {
            this.mApplication = instrumentation.newApplication(this.mClassLoader, appClass,
                this.getPluginContext());
            instrumentation.callApplicationOnCreate(this.mApplication);
            return this.mApplication;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 根据 Intent 和 flag
     * 获取 Apk 中对应的插件 Activity 信息
     *
     * @param intent intent
     * @param flags flags
     * @return ResolveInfo
     */
    public ResolveInfo resolveActivity(Intent intent, int flags) {
        List<ResolveInfo> query = this.queryIntentActivities(intent, flags);
        if (null == query || query.isEmpty()) {
            return null;
        }

        ContentResolver resolver = this.mPluginContext.getContentResolver();
        return chooseBestActivity(intent, intent.resolveTypeIfNeeded(resolver), flags, query);
    }


    /**
     * 根据 Intent 和 flag
     * 查询该 Apk 中是否有对应的插件 Activity 信息
     *
     * @param intent intent
     * @param flags flags
     * @return List<ResolveInfo>
     */
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        ComponentName component = intent.getComponent();
        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
        ContentResolver resolver = this.mPluginContext.getContentResolver();

        for (PackageParser.Activity activity : this.mPackage.activities) {
            if (match(activity, component)) {
                ResolveInfo resolveInfo = new ResolveInfo();
                resolveInfo.activityInfo = activity.info;
                resolveInfos.add(resolveInfo);
            } else if (component == null) {
                // only match implicit intent
                for (PackageParser.ActivityIntentInfo intentInfo : activity.intents) {
                    if (intentInfo.match(resolver, intent, true, TAG) >= 0) {
                        ResolveInfo resolveInfo = new ResolveInfo();
                        resolveInfo.activityInfo = activity.info;
                        resolveInfos.add(resolveInfo);
                        break;
                    }
                }
            }
        }

        return resolveInfos;
    }


    /**
     * 根据 Intent 和 flag
     * 获取 Apk 中对应的插件 Service 信息
     *
     * @param intent intent
     * @param flags flags
     * @return ResolveInfo
     */
    public ResolveInfo resolveService(Intent intent, int flags) {
        List<ResolveInfo> query = this.queryIntentServices(intent, flags);
        if (null == query || query.isEmpty()) {
            return null;
        }

        ContentResolver resolver = this.mPluginContext.getContentResolver();
        return chooseBestActivity(intent, intent.resolveTypeIfNeeded(resolver), flags, query);
    }


    /**
     * 根据 Intent 和 flag
     * 查询该 Apk 中是否有对应的插件 Service 信息
     *
     * @param intent intent
     * @param flags flags
     * @return List<ResolveInfo>
     */
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        ComponentName component = intent.getComponent();
        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
        ContentResolver resolver = this.mPluginContext.getContentResolver();

        for (PackageParser.Service service : this.mPackage.services) {
            if (match(service, component)) {
                ResolveInfo resolveInfo = new ResolveInfo();
                resolveInfo.serviceInfo = service.info;
                resolveInfos.add(resolveInfo);
            } else if (component == null) {
                // only match implicit intent
                for (PackageParser.ServiceIntentInfo intentInfo : service.intents) {
                    if (intentInfo.match(resolver, intent, true, TAG) >= 0) {
                        ResolveInfo resolveInfo = new ResolveInfo();
                        resolveInfo.serviceInfo = service.info;
                        resolveInfos.add(resolveInfo);
                        break;
                    }
                }
            }
        }

        return resolveInfos;
    }


    /**
     * 根据 Intent 和 flag
     * 查询该 Apk 中是否有对应的插件 BroadcastReceiver 信息
     *
     * @param intent intent
     * @param flags flags
     * @return List<ResolveInfo>
     */
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        ComponentName component = intent.getComponent();
        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
        ContentResolver resolver = this.mPluginContext.getContentResolver();

        for (PackageParser.Activity receiver : this.mPackage.receivers) {
            if (receiver.getComponentName().equals(component)) {
                ResolveInfo resolveInfo = new ResolveInfo();
                resolveInfo.activityInfo = receiver.info;
                resolveInfos.add(resolveInfo);
            } else if (component == null) {
                // only match implicit intent
                for (PackageParser.ActivityIntentInfo intentInfo : receiver.intents) {
                    if (intentInfo.match(resolver, intent, true, TAG) >= 0) {
                        ResolveInfo resolveInfo = new ResolveInfo();
                        resolveInfo.activityInfo = receiver.info;
                        resolveInfos.add(resolveInfo);
                        break;
                    }
                }
            }
        }

        return resolveInfos;
    }


    /**
     * 根据 name，查询对应的 Apk ContentProvider 缓存信息 ProviderInfo
     *
     * @param name name
     * @param flags flags
     * @return ProviderInfo
     */
    public ProviderInfo resolveContentProvider(String name, int flags) {
        return this.mProviders.get(name);
    }


    /**
     * 用于匹配 PackageParser.Component 对应的 ComponentName
     * PackageParser.Component 的子类有
     * PackageParser.Activity
     * PackageParser.Service
     *
     * @param component component
     * @param target target
     * @return boolean
     */
    private boolean match(PackageParser.Component component, ComponentName target) {
        ComponentName source = component.getComponentName();
        if (source == target) return true;
        if (source != null && target != null
            && source.getClassName().equals(target.getClassName())
            && (source.getPackageName().equals(target.getPackageName())
            || mHostContext.getPackageName().equals(target.getPackageName()))) {
            return true;
        }
        return false;
    }


    /**
     * 自定义的 插件 PackageManager
     *
     * 因为
     * 有些 Api 得通过 插件 数据调用
     * 也有 Api 得通过 宿主 数据条用
     *
     * @author johnsonlee
     */
    private class PluginPackageManager extends PackageManager {

        private PackageManager mHostPackageManager = mHostContext.getPackageManager();


        /**
         * 获取 PackageInfo
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param packageName packageName
         * @param flags flags
         * @return PackageInfo
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public PackageInfo getPackageInfo(String packageName, int flags)
            throws NameNotFoundException {

            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(packageName);
            if (null != plugin) {
                return plugin.mPackageInfo;
            }

            return this.mHostPackageManager.getPackageInfo(packageName, flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param names names
         * @return String[]
         */
        @Override
        public String[] currentToCanonicalPackageNames(String[] names) {
            return this.mHostPackageManager.currentToCanonicalPackageNames(names);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param names names
         * @return String[]
         */
        @Override
        public String[] canonicalToCurrentPackageNames(String[] names) {
            return this.mHostPackageManager.canonicalToCurrentPackageNames(names);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param packageName packageName
         * @return Intent
         */
        @Override
        public Intent getLaunchIntentForPackage(String packageName) {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(packageName);
            if (null != plugin) {
                return plugin.getLaunchIntent();
            }

            return this.mHostPackageManager.getLaunchIntentForPackage(packageName);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param packageName packageName
         * @return Intent
         */
        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public Intent getLeanbackLaunchIntentForPackage(String packageName) {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(packageName);
            if (null != plugin) {
                return plugin.getLeanbackLaunchIntent();
            }

            return this.mHostPackageManager.getLeanbackLaunchIntentForPackage(packageName);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param packageName packageName
         * @return int[]
         */
        @Override
        public int[] getPackageGids(String packageName) throws NameNotFoundException {
            return this.mHostPackageManager.getPackageGids(packageName);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param name name
         * @param flags flags
         * @return PermissionInfo
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public PermissionInfo getPermissionInfo(String name, int flags)
            throws NameNotFoundException {
            return this.mHostPackageManager.getPermissionInfo(name, flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param group group
         * @param flags flags
         * @return List<PermissionInfo>
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public List<PermissionInfo> queryPermissionsByGroup(String group, int flags)
            throws NameNotFoundException {
            return this.mHostPackageManager.queryPermissionsByGroup(group, flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param name name
         * @param flags flags
         * @return PermissionGroupInfo
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public PermissionGroupInfo getPermissionGroupInfo(String name, int flags)
            throws NameNotFoundException {
            return this.mHostPackageManager.getPermissionGroupInfo(name, flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param flags flags
         * @return List<PermissionGroupInfo>
         */
        @Override
        public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
            return this.mHostPackageManager.getAllPermissionGroups(flags);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param packageName packageName
         * @param flags flags
         * @return ApplicationInfo
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public ApplicationInfo getApplicationInfo(String packageName, int flags)
            throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(packageName);
            if (null != plugin) {
                return plugin.getApplicationInfo();
            }

            return this.mHostPackageManager.getApplicationInfo(packageName, flags);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param component component
         * @param flags flags
         * @return ActivityInfo
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public ActivityInfo getActivityInfo(ComponentName component, int flags)
            throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
            if (null != plugin) {
                return plugin.mActivityInfos.get(component);
            }

            return this.mHostPackageManager.getActivityInfo(component, flags);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param component component
         * @param flags flags
         * @return ActivityInfo
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public ActivityInfo getReceiverInfo(ComponentName component, int flags)
            throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
            if (null != plugin) {
                return plugin.mReceiverInfos.get(component);
            }

            return this.mHostPackageManager.getReceiverInfo(component, flags);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param component component
         * @param flags flags
         * @return ActivityInfo
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public ServiceInfo getServiceInfo(ComponentName component, int flags)
            throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
            if (null != plugin) {
                return plugin.mServiceInfos.get(component);
            }

            return this.mHostPackageManager.getServiceInfo(component, flags);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param component component
         * @param flags flags
         * @return ActivityInfo
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public ProviderInfo getProviderInfo(ComponentName component, int flags)
            throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
            if (null != plugin) {
                return plugin.mProviderInfos.get(component);
            }

            return this.mHostPackageManager.getProviderInfo(component, flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param flags flags
         * @return List<PackageInfo>
         */
        @Override
        public List<PackageInfo> getInstalledPackages(int flags) {
            return this.mHostPackageManager.getInstalledPackages(flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param permissions permissions
         * @param flags flags
         * @return List<PackageInfo>
         */
        @Override
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
            return this.mHostPackageManager.getPackagesHoldingPermissions(permissions, flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param permName permName
         * @param pkgName pkgName
         * @return int
         */
        @Override
        public int checkPermission(String permName, String pkgName) {
            return this.mHostPackageManager.checkPermission(permName, pkgName);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param info info
         * @return boolean
         */
        @Override
        public boolean addPermission(PermissionInfo info) {
            return this.mHostPackageManager.addPermission(info);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param info info
         * @return boolean
         */
        @Override
        public boolean addPermissionAsync(PermissionInfo info) {
            return this.mHostPackageManager.addPermissionAsync(info);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param name name
         */
        @Override
        public void removePermission(String name) {
            this.mHostPackageManager.removePermission(name);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param pkg1 pkg1
         * @param pkg2 pkg2
         * @return int
         */
        @Override
        public int checkSignatures(String pkg1, String pkg2) {
            return this.mHostPackageManager.checkSignatures(pkg1, pkg2);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param uid1 uid1
         * @param uid2 uid2
         * @return int
         */
        @Override
        public int checkSignatures(int uid1, int uid2) {
            return this.mHostPackageManager.checkSignatures(uid1, uid2);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param uid uid
         * @return String[]
         */
        @Override
        public String[] getPackagesForUid(int uid) {
            return this.mHostPackageManager.getPackagesForUid(uid);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param uid uid
         * @return String
         */
        @Override
        public String getNameForUid(int uid) {
            return this.mHostPackageManager.getNameForUid(uid);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param flags flags
         * @return List<ApplicationInfo>
         */
        @Override
        public List<ApplicationInfo> getInstalledApplications(int flags) {
            return this.mHostPackageManager.getInstalledApplications(flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @return String[]
         */
        @Override
        public String[] getSystemSharedLibraryNames() {
            return this.mHostPackageManager.getSystemSharedLibraryNames();
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @return FeatureInfo[]
         */
        @Override
        public FeatureInfo[] getSystemAvailableFeatures() {
            return this.mHostPackageManager.getSystemAvailableFeatures();
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param name name
         * @return boolean
         */
        @Override
        public boolean hasSystemFeature(String name) {
            return this.mHostPackageManager.hasSystemFeature(name);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param intent intent
         * @param flags flags
         * @return ResolveInfo
         */
        @Override
        public ResolveInfo resolveActivity(Intent intent, int flags) {
            ResolveInfo resolveInfo = mPluginManager.resolveActivity(intent, flags);
            if (null != resolveInfo) {
                return resolveInfo;
            }

            return this.mHostPackageManager.resolveActivity(intent, flags);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param intent intent
         * @param flags flags
         * @return List<ResolveInfo>
         */
        @Override
        public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
            ComponentName component = intent.getComponent();
            if (null == component) {
                if (intent.getSelector() != null) {
                    intent = intent.getSelector();
                    component = intent.getComponent();
                }
            }

            if (null != component) {
                LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
                if (null != plugin) {
                    ActivityInfo activityInfo = plugin.getActivityInfo(component);
                    if (activityInfo != null) {
                        ResolveInfo resolveInfo = new ResolveInfo();
                        resolveInfo.activityInfo = activityInfo;
                        return Arrays.asList(resolveInfo);
                    }
                }
            }

            List<ResolveInfo> all = new ArrayList<ResolveInfo>();

            List<ResolveInfo> pluginResolveInfos = mPluginManager.queryIntentActivities(intent,
                flags);
            if (null != pluginResolveInfos && pluginResolveInfos.size() > 0) {
                all.addAll(pluginResolveInfos);
            }

            List<ResolveInfo> hostResolveInfos = this.mHostPackageManager.queryIntentActivities(
                intent, flags);
            if (null != hostResolveInfos && hostResolveInfos.size() > 0) {
                all.addAll(hostResolveInfos);
            }

            return all;
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param caller caller
         * @param specifics specifics
         * @param intent intent
         * @param flags flags
         * @return List<ResolveInfo>
         */
        @Override
        public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
            return this.mHostPackageManager.queryIntentActivityOptions(caller, specifics, intent,
                flags);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param intent intent
         * @param flags flags
         * @return List<ResolveInfo>
         */
        @Override
        public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
            ComponentName component = intent.getComponent();
            if (null == component) {
                if (intent.getSelector() != null) {
                    intent = intent.getSelector();
                    component = intent.getComponent();
                }
            }

            if (null != component) {
                LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
                if (null != plugin) {
                    ActivityInfo activityInfo = plugin.getReceiverInfo(component);
                    if (activityInfo != null) {
                        ResolveInfo resolveInfo = new ResolveInfo();
                        resolveInfo.activityInfo = activityInfo;
                        return Arrays.asList(resolveInfo);
                    }
                }
            }

            List<ResolveInfo> all = new ArrayList<ResolveInfo>();

            List<ResolveInfo> pluginResolveInfos = mPluginManager.queryBroadcastReceivers(intent,
                flags);
            if (null != pluginResolveInfos && pluginResolveInfos.size() > 0) {
                all.addAll(pluginResolveInfos);
            }

            List<ResolveInfo> hostResolveInfos = this.mHostPackageManager.queryBroadcastReceivers(
                intent, flags);
            if (null != hostResolveInfos && hostResolveInfos.size() > 0) {
                all.addAll(hostResolveInfos);
            }

            return all;
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param intent intent
         * @param flags flags
         * @return ResolveInfo
         */
        @Override
        public ResolveInfo resolveService(Intent intent, int flags) {
            ResolveInfo resolveInfo = mPluginManager.resolveService(intent, flags);
            if (null != resolveInfo) {
                return resolveInfo;
            }

            return this.mHostPackageManager.resolveService(intent, flags);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param intent intent
         * @param flags flags
         * @return List<ResolveInfo>
         */
        @Override
        public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
            ComponentName component = intent.getComponent();
            if (null == component) {
                if (intent.getSelector() != null) {
                    intent = intent.getSelector();
                    component = intent.getComponent();
                }
            }

            if (null != component) {
                LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
                if (null != plugin) {
                    ServiceInfo serviceInfo = plugin.getServiceInfo(component);
                    if (serviceInfo != null) {
                        ResolveInfo resolveInfo = new ResolveInfo();
                        resolveInfo.serviceInfo = serviceInfo;
                        return Arrays.asList(resolveInfo);
                    }
                }
            }

            List<ResolveInfo> all = new ArrayList<ResolveInfo>();

            List<ResolveInfo> pluginResolveInfos = mPluginManager.queryIntentServices(intent,
                flags);
            if (null != pluginResolveInfos && pluginResolveInfos.size() > 0) {
                all.addAll(pluginResolveInfos);
            }

            List<ResolveInfo> hostResolveInfos = this.mHostPackageManager.queryIntentServices(
                intent, flags);
            if (null != hostResolveInfos && hostResolveInfos.size() > 0) {
                all.addAll(hostResolveInfos);
            }

            return all;
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param intent intent
         * @param flags flags
         * @return List<ResolveInfo>
         */
        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
            return this.mHostPackageManager.queryIntentContentProviders(intent, flags);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param name name
         * @param flags flags
         * @return ProviderInfo
         */
        @Override
        public ProviderInfo resolveContentProvider(String name, int flags) {
            ProviderInfo providerInfo = mPluginManager.resolveContentProvider(name, flags);
            if (null != providerInfo) {
                return providerInfo;
            }

            return this.mHostPackageManager.resolveContentProvider(name, flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param processName processName
         * @param uid uid
         * @param flags flags
         * @return List<ProviderInfo>
         */
        @Override
        public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
            return this.mHostPackageManager.queryContentProviders(processName, uid, flags);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param component component
         * @param flags flags
         * @return InstrumentationInfo
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public InstrumentationInfo getInstrumentationInfo(ComponentName component, int flags)
            throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
            if (null != plugin) {
                return plugin.mInstrumentationInfos.get(component);
            }

            return this.mHostPackageManager.getInstrumentationInfo(component, flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param targetPackage targetPackage
         * @param flags flags
         * @return List<InstrumentationInfo>
         */
        @Override
        public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
            return this.mHostPackageManager.queryInstrumentation(targetPackage, flags);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param packageName packageName
         * @param resid resid
         * @param appInfo appInfo
         * @return Drawable
         */
        @Override
        public Drawable getDrawable(String packageName, int resid, ApplicationInfo appInfo) {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(packageName);
            if (null != plugin) {
                return plugin.mResources.getDrawable(resid);
            }

            return this.mHostPackageManager.getDrawable(packageName, resid, appInfo);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param component component
         * @return Drawable
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public Drawable getActivityIcon(ComponentName component) throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
            if (null != plugin) {
                return plugin.mResources.getDrawable(plugin.mActivityInfos.get(component).icon);
            }

            return this.mHostPackageManager.getActivityIcon(component);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param intent intent
         * @return Drawable
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
            ResolveInfo ri = mPluginManager.resolveActivity(intent);
            if (null != ri) {
                LoadedPlugin plugin = mPluginManager.getLoadedPlugin(ri.resolvePackageName);
                return plugin.mResources.getDrawable(ri.activityInfo.icon);
            }

            return this.mHostPackageManager.getActivityIcon(intent);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param component component
         * @return Drawable
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        public Drawable getActivityBanner(ComponentName component) throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
            if (null != plugin) {
                return plugin.mResources.getDrawable(plugin.mActivityInfos.get(component).banner);
            }

            return this.mHostPackageManager.getActivityBanner(component);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param intent intent
         * @return Drawable
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
            ResolveInfo ri = mPluginManager.resolveActivity(intent);
            if (null != ri) {
                LoadedPlugin plugin = mPluginManager.getLoadedPlugin(ri.resolvePackageName);
                return plugin.mResources.getDrawable(ri.activityInfo.banner);
            }

            return this.mHostPackageManager.getActivityBanner(intent);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @return Drawable
         */
        @Override
        public Drawable getDefaultActivityIcon() {
            return this.mHostPackageManager.getDefaultActivityIcon();
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param info info
         * @return Drawable
         */
        @Override
        public Drawable getApplicationIcon(ApplicationInfo info) {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(info.packageName);
            if (null != plugin) {
                return plugin.mResources.getDrawable(info.icon);
            }

            return this.mHostPackageManager.getApplicationIcon(info);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param packageName packageName
         * @return Drawable
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(packageName);
            if (null != plugin) {
                return plugin.mResources.getDrawable(plugin.mPackage.applicationInfo.icon);
            }

            return this.mHostPackageManager.getApplicationIcon(packageName);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param info info
         * @return Drawable
         */
        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        public Drawable getApplicationBanner(ApplicationInfo info) {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(info.packageName);
            if (null != plugin) {
                return plugin.mResources.getDrawable(info.banner);
            }

            return this.mHostPackageManager.getApplicationBanner(info);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param packageName packageName
         * @return Drawable
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(packageName);
            if (null != plugin) {
                return plugin.mResources.getDrawable(plugin.mPackage.applicationInfo.banner);
            }

            return this.mHostPackageManager.getApplicationBanner(packageName);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param component component
         * @return Drawable
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public Drawable getActivityLogo(ComponentName component) throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
            if (null != plugin) {
                return plugin.mResources.getDrawable(plugin.mActivityInfos.get(component).logo);
            }

            return this.mHostPackageManager.getActivityLogo(component);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param intent intent
         * @return Drawable
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
            ResolveInfo ri = mPluginManager.resolveActivity(intent);
            if (null != ri) {
                LoadedPlugin plugin = mPluginManager.getLoadedPlugin(ri.resolvePackageName);
                return plugin.mResources.getDrawable(ri.activityInfo.logo);
            }

            return this.mHostPackageManager.getActivityLogo(intent);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param info info
         * @return Drawable
         */
        @Override
        public Drawable getApplicationLogo(ApplicationInfo info) {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(info.packageName);
            if (null != plugin) {
                return plugin.mResources.getDrawable(
                    0 != info.logo ? info.logo : android.R.drawable.sym_def_app_icon);
            }

            return this.mHostPackageManager.getApplicationLogo(info);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param packageName packageName
         * @return Drawable
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(packageName);
            if (null != plugin) {
                return plugin.mResources.getDrawable(0 != plugin.mPackage.applicationInfo.logo
                                                     ? plugin.mPackage.applicationInfo.logo
                                                     : android.R.drawable.sym_def_app_icon);
            }

            return this.mHostPackageManager.getApplicationLogo(packageName);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param icon icon
         * @param user user
         * @return Drawable
         */
        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
            return this.mHostPackageManager.getUserBadgedIcon(icon, user);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param user user
         * @param density density
         * @return Drawable
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        public Drawable getUserBadgeForDensity(UserHandle user, int density) {
            try {
                Method method = PackageManager.class.getMethod("getUserBadgeForDensity",
                    UserHandle.class, int.class);
                return (Drawable) method.invoke(this.mHostPackageManager, user, density);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param drawable drawable
         * @param user user
         * @param badgeLocation badgeLocation
         * @param badgeDensity badgeDensity
         * @return Drawable
         */
        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
            return this.mHostPackageManager.getUserBadgedDrawableForDensity(drawable, user,
                badgeLocation, badgeDensity);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param label label
         * @param user user
         * @return CharSequence
         */
        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
            return this.mHostPackageManager.getUserBadgedLabel(label, user);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param packageName packageName
         * @param resid resid
         * @param appInfo appInfo
         * @return CharSequence
         */
        @Override
        public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(packageName);
            if (null != plugin) {
                return plugin.mResources.getText(resid);
            }

            return this.mHostPackageManager.getText(packageName, resid, appInfo);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param packageName packageName
         * @param resid resid
         * @param appInfo appInfo
         * @return XmlResourceParser
         */
        @Override
        public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(packageName);
            if (null != plugin) {
                return plugin.mResources.getXml(resid);
            }

            return this.mHostPackageManager.getXml(packageName, resid, appInfo);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param info info
         * @return CharSequence
         */
        @Override
        public CharSequence getApplicationLabel(ApplicationInfo info) {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(info.packageName);
            if (null != plugin) {
                try {
                    return plugin.mResources.getText(info.labelRes);
                } catch (Resources.NotFoundException e) {
                    // ignored.
                }
            }

            return this.mHostPackageManager.getApplicationLabel(info);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param component component
         * @return Resources
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public Resources getResourcesForActivity(ComponentName component)
            throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(component);
            if (null != plugin) {
                return plugin.mResources;
            }

            return this.mHostPackageManager.getResourcesForActivity(component);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param app app
         * @return Resources
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public Resources getResourcesForApplication(ApplicationInfo app)
            throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(app.packageName);
            if (null != plugin) {
                return plugin.mResources;
            }

            return this.mHostPackageManager.getResourcesForApplication(app);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param appPackageName appPackageName
         * @return Resources
         * @throws NameNotFoundException NameNotFoundException
         */
        @Override
        public Resources getResourcesForApplication(String appPackageName)
            throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(appPackageName);
            if (null != plugin) {
                return plugin.mResources;
            }

            return this.mHostPackageManager.getResourcesForApplication(appPackageName);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param id id
         * @param verificationCode verificationCode
         */
        @Override
        public void verifyPendingInstall(int id, int verificationCode) {
            this.mHostPackageManager.verifyPendingInstall(id, verificationCode);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param id id
         * @param verificationCodeAtTimeout verificationCodeAtTimeout
         * @param millisecondsToDelay millisecondsToDelay
         */
        @Override
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
            this.mHostPackageManager.extendVerificationTimeout(id, verificationCodeAtTimeout,
                millisecondsToDelay);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param targetPackage targetPackage
         * @param installerPackageName installerPackageName
         */
        @Override
        public void setInstallerPackageName(String targetPackage, String installerPackageName) {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(targetPackage);
            if (null != plugin) {
                return;
            }

            this.mHostPackageManager.setInstallerPackageName(targetPackage, installerPackageName);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param packageName packageName
         * @return String
         */
        @Override
        public String getInstallerPackageName(String packageName) {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(packageName);
            if (null != plugin) {
                return mHostContext.getPackageName();
            }

            return this.mHostPackageManager.getInstallerPackageName(packageName);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param packageName packageName
         */
        @Override
        public void addPackageToPreferred(String packageName) {
            this.mHostPackageManager.addPackageToPreferred(packageName);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param packageName packageName
         */
        @Override
        public void removePackageFromPreferred(String packageName) {
            this.mHostPackageManager.removePackageFromPreferred(packageName);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param flags flags
         */
        @Override
        public List<PackageInfo> getPreferredPackages(int flags) {
            return this.mHostPackageManager.getPreferredPackages(flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param filter filter
         * @param match match
         * @param set set
         * @param activity activity
         */
        @Override
        public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
            this.mHostPackageManager.addPreferredActivity(filter, match, set, activity);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param packageName packageName
         */
        @Override
        public void clearPackagePreferredActivities(String packageName) {
            this.mHostPackageManager.clearPackagePreferredActivities(packageName);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param outFilters outFilters
         * @param outActivities outActivities
         * @param packageName packageName
         * @return int
         */
        @Override
        public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
            return this.mHostPackageManager.getPreferredActivities(outFilters, outActivities,
                packageName);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param component component
         * @param newState newState
         * @param flags flags
         */
        @Override
        public void setComponentEnabledSetting(ComponentName component, int newState, int flags) {
            this.mHostPackageManager.setComponentEnabledSetting(component, newState, flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param component component
         */
        @Override
        public int getComponentEnabledSetting(ComponentName component) {
            return this.mHostPackageManager.getComponentEnabledSetting(component);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param packageName packageName
         * @param newState newState
         * @param flags flags
         */
        @Override
        public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
            this.mHostPackageManager.setApplicationEnabledSetting(packageName, newState, flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param packageName packageName
         * @return int
         */
        @Override
        public int getApplicationEnabledSetting(String packageName) {
            return this.mHostPackageManager.getApplicationEnabledSetting(packageName);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @return boolean
         */
        @Override
        public boolean isSafeMode() {
            return this.mHostPackageManager.isSafeMode();
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @return PackageInstaller
         */
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public PackageInstaller getPackageInstaller() {
            return this.mHostPackageManager.getPackageInstaller();
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param s s
         * @param i i
         * @return int[]
         * @throws NameNotFoundException NameNotFoundException
         */
        @TargetApi(24)
        public int[] getPackageGids(String s, int i) throws NameNotFoundException {
            return mHostPackageManager.getPackageGids(s);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param s s
         * @param i i
         * @return int
         * @throws NameNotFoundException NameNotFoundException
         */
        public int getPackageUid(String s, int i) throws NameNotFoundException {
            Object uid = ReflectUtil.invokeNoException(PackageManager.class, mHostPackageManager,
                "getPackageUid",
                new Class[] { String.class, int.class }, s, i);
            if (uid != null) {
                return (int) uid;
            } else {
                throw new NameNotFoundException(s);
            }
        }


        @TargetApi(23)
        public boolean isPermissionRevokedByPolicy(String s, String s1) {
            return false;
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param s s
         * @param i i
         * @return boolean
         */
        @TargetApi(24)
        public boolean hasSystemFeature(String s, int i) {
            return mHostPackageManager.hasSystemFeature(s);
        }


        /**
         * 有 插件 就返回插件的
         * 没 插件 就返回宿主的
         *
         * @param versionedPackage versionedPackage
         * @param flags flags
         * @return PackageInfo
         * @throws NameNotFoundException NameNotFoundException
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public PackageInfo getPackageInfo(VersionedPackage versionedPackage, int flags)
            throws NameNotFoundException {
            LoadedPlugin plugin = mPluginManager.getLoadedPlugin(versionedPackage.getPackageName());
            if (null != plugin) {
                return plugin.mPackageInfo;
            }
            return this.mHostPackageManager.getPackageInfo(versionedPackage, flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @return boolean
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public boolean isInstantApp() {
            return this.mHostPackageManager.isInstantApp();
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param packageName packageName
         * @return boolean
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public boolean isInstantApp(String packageName) {
            return this.mHostPackageManager.isInstantApp(packageName);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @return int
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public int getInstantAppCookieMaxBytes() {
            return this.mHostPackageManager.getInstantAppCookieMaxBytes();
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @return byte[]
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @NonNull
        @Override
        public byte[] getInstantAppCookie() {
            return this.mHostPackageManager.getInstantAppCookie();
        }


        /**
         * 接入 宿主 的调用逻辑
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void clearInstantAppCookie() {
            this.mHostPackageManager.clearInstantAppCookie();
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param cookie cookie
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void updateInstantAppCookie(@Nullable byte[] cookie) {
            this.mHostPackageManager.updateInstantAppCookie(cookie);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param flags flags
         * @return List<SharedLibraryInfo>
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @NonNull
        @Override
        public List<SharedLibraryInfo> getSharedLibraries(int flags) {
            return this.mHostPackageManager.getSharedLibraries(flags);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param sequenceNumber sequenceNumber
         * @return ChangedPackages
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Nullable
        @Override
        public ChangedPackages getChangedPackages(int sequenceNumber) {
            return this.mHostPackageManager.getChangedPackages(sequenceNumber);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @param packageName packageName
         * @param categoryHint categoryHint
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void setApplicationCategoryHint(@NonNull String packageName, int categoryHint) {
            this.mHostPackageManager.setApplicationCategoryHint(packageName, categoryHint);
        }


        /**
         * 接入 宿主 的调用逻辑
         *
         * @return boolean
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public boolean canRequestPackageInstalls() {
            return this.mHostPackageManager.canRequestPackageInstalls();
        }

    }

}
