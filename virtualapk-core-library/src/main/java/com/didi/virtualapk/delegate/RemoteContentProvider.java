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

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import com.didi.virtualapk.PluginManager;
import com.didi.virtualapk.internal.LoadedPlugin;
import com.didi.virtualapk.utils.RunUtil;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 插桩 ContentProvider
 *
 * Created by renyugang on 16/12/7.
 */

public class RemoteContentProvider extends ContentProvider {
    private static final String TAG = "RemoteContentProvider";

    public static final String KEY_PKG = "pkg";
    public static final String KEY_PLUGIN = "plugin";
    public static final String KEY_URI = "uri";

    public static final String KEY_WRAPPER_URI = "wrapper_uri";

    private static Map<String, ContentProvider> sCachedProviders = new HashMap<>();


    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate, current thread:"
            + Thread.currentThread().getName());
        return true;
    }


    /**
     * 获取插件 ContentProvider
     *
     * 1. 解析复合 Uri 协议，获取其中包含的插件 Uri 协议
     * 2. 获取插件 插件 Uri 协议 中的 Authority
     * 3. 根据 插件 Uri Authority，拿出插件 ContentProvider 缓存
     *
     * 4. 当前面没获取到插件 ContentProvider 缓存的时候，根据 插件 Uri 协议包含的该 ContentProvider 所在
     * -  apk file 的绝对路径，获取到对应的 LoadedPlugin。如果还没有 LoadedPlugin，需要根据 apk file path
     * -  加载出 LoadedPlugin
     *
     * 5. 在保证有 LoadedPlugin 缓存的情况下。去获取该 插件 ContentProvider 对应的 ProviderInfo 信息
     * 6. 给 主线程 发消息，主线程获取对应的 LoadedPlugin 缓存，然后加载出对应的 插件 ContentProvider class
     * -  手动调用 ContentProvider # attachInfo(...)。最后加入到 插件 ContentProvider 集合内
     *
     * @param uri uri
     * @return ContentProvider
     */
    private ContentProvider getContentProvider(final Uri uri) {
        final PluginManager pluginManager = PluginManager.getInstance(getContext());
        Uri pluginUri = Uri.parse(uri.getQueryParameter(KEY_URI));
        final String auth = pluginUri.getAuthority();
        ContentProvider cachedProvider = sCachedProviders.get(auth);
        if (cachedProvider != null) {
            return cachedProvider;
        }

        synchronized (sCachedProviders) {
            LoadedPlugin plugin = pluginManager.getLoadedPlugin(uri.getQueryParameter(KEY_PKG));
            if (plugin == null) {
                try {
                    pluginManager.loadPlugin(new File(uri.getQueryParameter(KEY_PLUGIN)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            final ProviderInfo providerInfo = pluginManager.resolveContentProvider(auth, 0);
            if (providerInfo != null) {
                RunUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LoadedPlugin loadedPlugin = pluginManager.getLoadedPlugin(
                                uri.getQueryParameter(KEY_PKG));
                            ContentProvider contentProvider = (ContentProvider) Class.forName(
                                providerInfo.name).newInstance();
                            contentProvider.attachInfo(loadedPlugin.getPluginContext(),
                                providerInfo);
                            sCachedProviders.put(auth, contentProvider);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, true);
                return sCachedProviders.get(auth);
            }
        }

        return null;
    }


    /**
     * 获取插件 ContentProvider 后
     * 解析复合 Uri 协议，获取其中包含的插件 Uri 协议
     *
     * 手动调用 插件 ContentProvider # getType(...)
     *
     * @param uri uri
     * @return String
     */
    @Override
    public String getType(Uri uri) {
        ContentProvider provider = getContentProvider(uri);
        Uri pluginUri = Uri.parse(uri.getQueryParameter(KEY_URI));
        if (provider != null) {
            return provider.getType(pluginUri);
        }

        return null;
    }


    /**
     * 获取插件 ContentProvider 后
     * 解析复合 Uri 协议，获取其中包含的插件 Uri 协议
     *
     * 手动调用 插件 ContentProvider # query(...)
     *
     * @param uri uri
     * @param projection projection
     * @param selection selection
     * @param selectionArgs selectionArgs
     * @param sortOrder sortOrder
     * @return Cursor
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ContentProvider provider = getContentProvider(uri);
        Uri pluginUri = Uri.parse(uri.getQueryParameter(KEY_URI));
        if (provider != null) {
            return provider.query(pluginUri, projection, selection, selectionArgs, sortOrder);
        }

        return null;
    }


    /**
     * 获取插件 ContentProvider 后
     * 解析复合 Uri 协议，获取其中包含的插件 Uri 协议
     *
     * 手动调用 插件 ContentProvider # insert(...)
     *
     * @param uri uri
     * @param values values
     * @return Uri
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        ContentProvider provider = getContentProvider(uri);
        Uri pluginUri = Uri.parse(uri.getQueryParameter(KEY_URI));
        if (provider != null) {
            return provider.insert(pluginUri, values);
        }

        return uri;
    }


    /**
     * 获取插件 ContentProvider 后
     * 解析复合 Uri 协议，获取其中包含的插件 Uri 协议
     *
     * 手动调用 插件 ContentProvider # delete(...)
     *
     * @param uri uri
     * @param selection selection
     * @param selectionArgs selectionArgs
     * @return int
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        ContentProvider provider = getContentProvider(uri);
        Uri pluginUri = Uri.parse(uri.getQueryParameter(KEY_URI));
        if (provider != null) {
            return provider.delete(pluginUri, selection, selectionArgs);
        }

        return 0;
    }


    /**
     * 获取插件 ContentProvider 后
     * 解析复合 Uri 协议，获取其中包含的插件 Uri 协议
     *
     * 手动调用 插件 ContentProvider # update(...)
     *
     * @param uri uri
     * @param values values
     * @param selection selection
     * @param selectionArgs selectionArgs
     * @return int
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        ContentProvider provider = getContentProvider(uri);
        Uri pluginUri = Uri.parse(uri.getQueryParameter(KEY_URI));
        if (provider != null) {
            return provider.update(pluginUri, values, selection, selectionArgs);
        }

        return 0;
    }


    /**
     * 获取插件 ContentProvider 后
     * 解析复合 Uri 协议，获取其中包含的插件 Uri 协议
     *
     * 手动调用 插件 ContentProvider # bulkInsert(...)
     *
     * @param uri uri
     * @param values values
     * @return int
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        ContentProvider provider = getContentProvider(uri);
        Uri pluginUri = Uri.parse(uri.getQueryParameter(KEY_URI));
        if (provider != null) {
            return provider.bulkInsert(pluginUri, values);
        }

        return 0;
    }


    /**
     * 反射替换 ContentProviderOperation 集合中所有的 ContentProviderOperation # Uri mUri
     *
     * 在 ContentProviderOperation 集合存在数据的情况下
     * 取出每个 ContentProviderOperation 对应的插件 ContentProvider
     * 手动调用 ContentProvider # applyBatch(...)
     *
     * @param operations operations
     * @return ContentProviderResult[]
     * @throws OperationApplicationException OperationApplicationException
     */
    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
        throws OperationApplicationException {
        try {
            Field uriField = ContentProviderOperation.class.getDeclaredField("mUri");
            uriField.setAccessible(true);
            for (ContentProviderOperation operation : operations) {
                Uri pluginUri = Uri.parse(operation.getUri().getQueryParameter(KEY_URI));
                uriField.set(operation, pluginUri);
            }
        } catch (Exception e) {
            return new ContentProviderResult[0];
        }

        if (operations.size() > 0) {
            ContentProvider provider = getContentProvider(operations.get(0).getUri());
            if (provider != null) {
                return provider.applyBatch(operations);
            }
        }

        return new ContentProviderResult[0];
    }


    /**
     * 获取插件 ContentProvider 后
     * 解析复合 Uri 协议，获取其中包含的插件 Uri 协议
     *
     * 手动调用 插件 ContentProvider # call(...)
     *
     * @param method method
     * @param arg arg
     * @param extras extras
     * @return Bundle
     */
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        Log.d(TAG, "call " + method + " with extras : " + extras);

        if (extras == null || extras.getString(KEY_WRAPPER_URI) == null) {
            return null;
        }

        Uri uri = Uri.parse(extras.getString(KEY_WRAPPER_URI));
        ContentProvider provider = getContentProvider(uri);
        if (provider != null) {
            return provider.call(method, arg, extras);
        }

        return null;
    }

}
