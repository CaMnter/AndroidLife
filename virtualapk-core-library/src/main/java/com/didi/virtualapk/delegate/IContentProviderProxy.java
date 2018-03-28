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

import android.content.Context;
import android.content.IContentProvider;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.didi.virtualapk.PluginManager;
import com.didi.virtualapk.internal.LoadedPlugin;
import com.didi.virtualapk.internal.PluginContentResolver;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import static com.didi.virtualapk.delegate.RemoteContentProvider.KEY_WRAPPER_URI;

/**
 * 用于动态代理 RemoteContentProvider 的远程 Binder proxy
 *
 * hook 点是
 *
 * ActivityThread # ArrayMap<ProviderKey, ProviderClientRecord> mProviderMap
 * ActivityThread # ProviderClientRecord # IContentProvider mProvider
 *
 * 中寻找 RemoteContentProvider
 *
 * Created by renyugang on 16/12/8.
 */

public class IContentProviderProxy implements InvocationHandler {
    private static final String TAG = "IContentProviderProxy";

    private IContentProvider mBase;
    private Context mContext;


    private IContentProviderProxy(Context context, IContentProvider iContentProvider) {
        mBase = iContentProvider;
        mContext = context;
    }


    public static IContentProvider newInstance(Context context, IContentProvider iContentProvider) {
        return (IContentProvider) Proxy.newProxyInstance(
            iContentProvider.getClass().getClassLoader(),
            new Class[] { IContentProvider.class },
            new IContentProviderProxy(context, iContentProvider));
    }


    /**
     * IContentProvider 的全局方法 hook
     *
     * 1. 所有方法中的 Uri，如果是插件 ContentProvider 的 Uri，就会 hook 成指向 插桩 ContentProvider
     * -  的复合 Uri 协议
     *
     * 2. 当 IContentProvider # call(...) 的时候，先抽出 Bundle，再寻找其中的 Uri
     * -  再判断是否是插件 ContentProvider 的 Uri，就会 hook 成指向 插桩 ContentProvider
     * -  的复合 Uri 协议
     *
     * @param proxy proxy
     * @param method method
     * @param args args
     * @return Object
     * @throws Throwable Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.v(TAG, method.toGenericString() + " : " + Arrays.toString(args));
        wrapperUri(method, args);

        try {
            return method.invoke(mBase, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }


    /**
     * 1. 在 IContentProvider 调用任何方法的时候，都去找这个方法中的 Uri 参数
     * -  当 IContentProvider # call(...) 的时候，先抽出 Bundle，再寻找其中的 Uri
     * 2. 校验 url 是否是插件 ContentProvider
     * 3. 是的话，获取 插件 ContentProvider 对应的 ProviderInfo 和 LoadedPlugin
     * -  构造 插桩 ContentProvider 的 Uri
     * -  该协议是复合 Uri 协议，包含了最初的 插件 ContentProvider Uri 信息
     * 4. 根据 复合 Uri 协议，会 call 向 插桩 ContentProvider，在 插桩 ContentProvider
     * -  内，再次分发 插件 ContentProvider Uri
     *
     * 5. 将复合 Uri 协议，替换 1. 中找到的 Uri 参数，IContentProvider # call(...) 的时
     * -  候，先抽出 Bundle，替换其中的 Uri
     */
    private void wrapperUri(Method method, Object[] args) {
        Uri uri = null;
        int index = 0;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Uri) {
                    uri = (Uri) args[i];
                    index = i;
                    break;
                }
            }
        }

        Bundle bundleInCallMethod = null;
        if (method.getName().equals("call")) {
            bundleInCallMethod = getBundleParameter(args);
            if (bundleInCallMethod != null) {
                String uriString = bundleInCallMethod.getString(KEY_WRAPPER_URI);
                if (uriString != null) {
                    uri = Uri.parse(uriString);
                }
            }
        }

        if (uri == null) {
            return;
        }

        PluginManager pluginManager = PluginManager.getInstance(mContext);
        ProviderInfo info = pluginManager.resolveContentProvider(uri.getAuthority(), 0);
        if (info != null) {
            String pkg = info.packageName;
            LoadedPlugin plugin = pluginManager.getLoadedPlugin(pkg);
            String pluginUri = Uri.encode(uri.toString());
            StringBuilder builder = new StringBuilder(PluginContentResolver.getUri(mContext));
            builder.append("/?plugin=" + plugin.getLocation());
            builder.append("&pkg=" + pkg);
            builder.append("&uri=" + pluginUri);
            Uri wrapperUri = Uri.parse(builder.toString());
            if (method.getName().equals("call")) {
                bundleInCallMethod.putString(KEY_WRAPPER_URI, wrapperUri.toString());
            } else {
                args[index] = wrapperUri;
            }
        }
    }


    /**
     * 寻找方法中的 Bundle 参数
     *
     * 这里，主要用于搜寻
     *
     * IContentProvider # call(String callingPkg, String method, @Nullable String arg, @Nullable
     * -                       Bundle extras)
     *
     * 中的 Bundle extras
     *
     * @param args 方法参数组
     * @return Bundle 参数
     */
    private Bundle getBundleParameter(Object[] args) {
        Bundle bundle = null;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Bundle) {
                    bundle = (Bundle) args[i];
                    break;
                }
            }
        }

        return bundle;
    }

}
