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

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * 自定义一个 Context，用于存放在 LoadedPlugin
 *
 * 但是，这个 Context 的 base 是 宿主
 * 意味着这个 Context 的环境属于 宿主
 *
 * 唯一不同的地方在于
 *
 * 从 宿主 中获取
 * getApplicationContext()
 * getHostContext()
 * getContentResolver()
 * getSystemService(String name)
 *
 * 非宿主 中获取
 *
 * 插件 Classloader
 * getClassLoader()
 *
 * 插件 PackageManager，用的是 LoadedPlugin 内的自定义 PackageManager
 * getPackageManager()
 *
 * 插件 Resources
 * getResources()
 *
 * 插件 AssetManager
 * getAssets()
 *
 * 插件 Theme
 * getTheme()
 *
 * Hook 了 startActivity(Intent intent)
 * 手动设置 intent 的 ComponentName
 *
 * Created by renyugang on 16/8/12.
 */
class PluginContext extends ContextWrapper {

    private final LoadedPlugin mPlugin;


    public PluginContext(LoadedPlugin plugin) {
        super(plugin.getPluginManager().getHostContext());
        this.mPlugin = plugin;
    }


    /**
     * 从 宿主 中获取 Application
     *
     * @return Context
     */
    @Override
    public Context getApplicationContext() {
        return this.mPlugin.getApplication();
    }

    //    @Override
    //    public ApplicationInfo getApplicationInfo() {
    //        return this.mPlugin.getApplicationInfo();
    //    }


    /**
     * 从 宿主 中获取 Context
     *
     * @return Context
     */
    private Context getHostContext() {
        return getBaseContext();
    }


    /**
     * 从 宿主 中获取 ContentResolver
     *
     * @return ContentResolver
     */
    @Override
    public ContentResolver getContentResolver() {
        return new PluginContentResolver(getHostContext());
    }


    /**
     * 从 插件 中获取 插件 Classloader
     *
     * @return ClassLoader
     */
    @Override
    public ClassLoader getClassLoader() {
        return this.mPlugin.getClassLoader();
    }

    //    @Override
    //    public String getPackageName() {
    //        return this.mPlugin.getPackageName();
    //    }

    //    @Override
    //    public String getPackageResourcePath() {
    //        return this.mPlugin.getPackageResourcePath();
    //    }

    //    @Override
    //    public String getPackageCodePath() {
    //        return this.mPlugin.getCodePath();
    //    }


    /**
     * 从 插件 中获取 插件 PackageManager
     *
     * @return PackageManager
     */
    @Override
    public PackageManager getPackageManager() {
        return this.mPlugin.getPackageManager();
    }


    /**
     * 从 宿主 中获取 SystemService
     *
     * @param name name
     * @return Object
     */
    @Override
    public Object getSystemService(String name) {
        // intercept CLIPBOARD_SERVICE,NOTIFICATION_SERVICE
        if (name.equals(Context.CLIPBOARD_SERVICE)) {
            return getHostContext().getSystemService(name);
        } else if (name.equals(Context.NOTIFICATION_SERVICE)) {
            return getHostContext().getSystemService(name);
        }

        return super.getSystemService(name);
    }


    /**
     * 从 插件 中获取 插件 Resources
     *
     * @return Resources
     */
    @Override
    public Resources getResources() {
        return this.mPlugin.getResources();
    }


    /**
     * 从 插件 中获取 插件 AssetManager
     *
     * @return AssetManager
     */
    @Override
    public AssetManager getAssets() {
        return this.mPlugin.getAssets();
    }


    /**
     * 从 插件 中获取 插件 Theme
     *
     * @return Theme
     */
    @Override
    public Resources.Theme getTheme() {
        return this.mPlugin.getTheme();
    }


    /**
     * Hook 了 startActivity(Intent intent)
     * 手动设置 intent 的 ComponentName
     *
     * @param intent intent
     */
    @Override
    public void startActivity(Intent intent) {
        ComponentsHandler componentsHandler = mPlugin.getPluginManager().getComponentsHandler();
        componentsHandler.transformIntentToExplicitAsNeeded(intent);
        super.startActivity(intent);
    }

}
