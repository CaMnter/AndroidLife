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

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import com.didi.virtualapk.PluginManager;
import com.didi.virtualapk.utils.ReflectUtil;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * Created by renyugang on 16/8/9.
 */
class ResourcesManager {

    /**
     * 反射创建 一个 复合资源
     *
     * --------------------------------------------------------------------------------------------
     *
     * 通过反射调用
     * AssetManager # addAssetPath(String path)
     *
     * 然后得到的 AssetManager
     * 直接 new Resources
     *
     * --------------------------------------------------------------------------------------------
     *
     * 1. 拿到宿主上的 AssetManager，低与 5.0.0 直接反射创建出来
     * 2. 反射调用 AssetManager # addAssetPath(String path)，添加 当前插件 Apk
     * -  这样 AssetManager 生成的 Resources 就有 当前插件 和 宿主 的资源
     * 3. 获取之前加载过的 Apk 内存形式 - LoadedPlugin，然后循环调用 AssetManager # addAssetPath(String path)
     * -  这样 AssetManager 生成的 Resources 就有 当前插件 、之前加载过的插件 和 宿主 的资源
     * 4. 由于 MiUi, Vivo, Nubia 等的资源实现类不一样，在进行 new Resources 的时候，需要适配
     *
     * --------------------------------------------------------------------------------------------
     *
     * @param hostContext hostContext
     * @param apk apk
     * @return Resources
     */
    public static synchronized Resources createResources(Context hostContext, String apk) {
        Resources hostResources = hostContext.getResources();
        Resources newResources = null;
        AssetManager assetManager;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                assetManager = AssetManager.class.newInstance();
                ReflectUtil.invoke(AssetManager.class, assetManager, "addAssetPath",
                    hostContext.getApplicationInfo().sourceDir);
            } else {
                assetManager = hostResources.getAssets();
            }
            ReflectUtil.invoke(AssetManager.class, assetManager, "addAssetPath", apk);
            List<LoadedPlugin> pluginList = PluginManager.getInstance(hostContext)
                .getAllLoadedPlugins();
            for (LoadedPlugin plugin : pluginList) {
                ReflectUtil.invoke(AssetManager.class, assetManager, "addAssetPath",
                    plugin.getLocation());
            }
            if (isMiUi(hostResources)) {
                newResources = MiUiResourcesCompat.createResources(hostResources, assetManager);
            } else if (isVivo(hostResources)) {
                newResources = VivoResourcesCompat.createResources(hostContext, hostResources,
                    assetManager);
            } else if (isNubia(hostResources)) {
                newResources = NubiaResourcesCompat.createResources(hostResources, assetManager);
            } else if (isNotRawResources(hostResources)) {
                newResources = AdaptationResourcesCompat.createResources(hostResources,
                    assetManager);
            } else {
                // is raw android resources
                newResources = new Resources(assetManager, hostResources.getDisplayMetrics(),
                    hostResources.getConfiguration());
            }
            // lastly, sync all LoadedPlugin to newResources
            for (LoadedPlugin plugin : pluginList) {
                plugin.updateResources(newResources);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newResources;

    }


    /**
     * Hook 掉宿主内的 Resources
     *
     * 用 createResources(Context hostContext, String apk) 生成的 复合资源
     *
     * 这样的话，插件 class 和 宿主 class 都访问这个 复合资源话，就没问题了
     *
     * Hook 点
     *
     * ContextImpl # Resources mResources
     * LoadedApk # Resources mResources
     *
     * >=7.0.0
     * ResourcesManager # ArrayMap<ResourcesKey, WeakReference<ResourcesImpl>> mResourceImpls
     * <7.0.0
     * ResourcesManager # ArrayMap<ResourcesKey, WeakReference<Resources> > mActiveResources
     *
     * @param base base
     * @param resources resources
     */
    public static void hookResources(Context base, Resources resources) {
        try {
            ReflectUtil.setField(base.getClass(), base, "mResources", resources);
            Object loadedApk = ReflectUtil.getPackageInfo(base);
            ReflectUtil.setField(loadedApk.getClass(), loadedApk, "mResources", resources);

            Object activityThread = ReflectUtil.getActivityThread(base);
            Object resManager = ReflectUtil.getField(activityThread.getClass(), activityThread,
                "mResourcesManager");
            if (Build.VERSION.SDK_INT < 24) {
                Map<Object, WeakReference<Resources>> map
                    = (Map<Object, WeakReference<Resources>>) ReflectUtil.getField(
                    resManager.getClass(), resManager, "mActiveResources");
                Object key = map.keySet().iterator().next();
                map.put(key, new WeakReference<>(resources));
            } else {
                // still hook Android N Resources, even though it's unnecessary, then nobody will be strange.
                Map map = (Map) ReflectUtil.getFieldNoException(resManager.getClass(), resManager,
                    "mResourceImpls");
                Object key = map.keySet().iterator().next();
                Object resourcesImpl = ReflectUtil.getFieldNoException(Resources.class, resources,
                    "mResourcesImpl");
                map.put(key, new WeakReference<>(resourcesImpl));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断 小米 机型
     *
     * @param resources resources
     * @return boolean
     */
    private static boolean isMiUi(Resources resources) {
        return resources.getClass().getName().equals("android.content.res.MiuiResources");
    }


    /**
     * 判断 Vivo 机型
     *
     * @param resources resources
     * @return boolean
     */
    private static boolean isVivo(Resources resources) {
        return resources.getClass().getName().equals("android.content.res.VivoResources");
    }


    /**
     * 判断 Nubia 机型
     *
     * @param resources resources
     * @return boolean
     */
    private static boolean isNubia(Resources resources) {
        return resources.getClass().getName().equals("android.content.res.NubiaResources");
    }


    /**
     * 判断其他机型
     *
     * @param resources resources
     * @return boolean
     */
    private static boolean isNotRawResources(Resources resources) {
        return !resources.getClass().getName().equals("android.content.res.Resources");
    }


    /**
     * 适配 小米 机型
     *
     * 反射 android.content.res.MiuiResources 创建 Resources
     */
    private static final class MiUiResourcesCompat {
        private static Resources createResources(Resources hostResources, AssetManager assetManager)
            throws Exception {
            Class resourcesClazz = Class.forName("android.content.res.MiuiResources");
            Resources newResources = (Resources) ReflectUtil.invokeConstructor(resourcesClazz,
                new Class[] { AssetManager.class, DisplayMetrics.class, Configuration.class },
                new Object[] { assetManager, hostResources.getDisplayMetrics(),
                    hostResources.getConfiguration() });
            return newResources;
        }
    }


    /**
     * 适配 Vivo 机型
     *
     * 反射 android.content.res.VivoResources 创建 Resources
     */
    private static final class VivoResourcesCompat {
        private static Resources createResources(Context hostContext, Resources hostResources, AssetManager assetManager)
            throws Exception {
            Class resourcesClazz = Class.forName("android.content.res.VivoResources");
            Resources newResources = (Resources) ReflectUtil.invokeConstructor(resourcesClazz,
                new Class[] { AssetManager.class, DisplayMetrics.class, Configuration.class },
                new Object[] { assetManager, hostResources.getDisplayMetrics(),
                    hostResources.getConfiguration() });
            ReflectUtil.invokeNoException(resourcesClazz, newResources, "init",
                new Class[] { String.class }, hostContext.getPackageName());
            Object themeValues = ReflectUtil.getFieldNoException(resourcesClazz, hostResources,
                "mThemeValues");
            ReflectUtil.setFieldNoException(resourcesClazz, newResources, "mThemeValues",
                themeValues);
            return newResources;
        }
    }


    /**
     * 适配 Nubia 机型
     *
     * 反射 android.content.res.NubiaResources 创建 Resources
     */
    private static final class NubiaResourcesCompat {
        private static Resources createResources(Resources hostResources, AssetManager assetManager)
            throws Exception {
            Class resourcesClazz = Class.forName("android.content.res.NubiaResources");
            Resources newResources = (Resources) ReflectUtil.invokeConstructor(resourcesClazz,
                new Class[] { AssetManager.class, DisplayMetrics.class, Configuration.class },
                new Object[] { assetManager, hostResources.getDisplayMetrics(),
                    hostResources.getConfiguration() });
            return newResources;
        }
    }


    /**
     * 其他类型
     *
     * 尝试反射调用其 具体类型 的构造方法
     * 不行，就用 new Resources
     */
    private static final class AdaptationResourcesCompat {
        private static Resources createResources(Resources hostResources, AssetManager assetManager)
            throws Exception {
            Resources newResources;
            try {
                Class resourcesClazz = hostResources.getClass();
                newResources = (Resources) ReflectUtil.invokeConstructor(resourcesClazz,
                    new Class[] { AssetManager.class, DisplayMetrics.class, Configuration.class },
                    new Object[] { assetManager, hostResources.getDisplayMetrics(),
                        hostResources.getConfiguration() });
            } catch (Exception e) {
                newResources = new Resources(assetManager, hostResources.getDisplayMetrics(),
                    hostResources.getConfiguration());
            }

            return newResources;
        }
    }

}
