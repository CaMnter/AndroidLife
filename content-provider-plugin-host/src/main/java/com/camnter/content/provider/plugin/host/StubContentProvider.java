package com.camnter.content.provider.plugin.host;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author CaMnter
 */

public class StubContentProvider extends ContentProvider {

    public static final Uri STUB_URI = Uri.parse(
        "com.camnter.content.provider.plugin.host.StubContentProvider");

    public static final String AUTHORITY
        = "com.camnter.content.provider.plugin.host.StubContentProvider";


    @Override
    public boolean onCreate() {
        return true;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final Uri pluginUri = this.getPluginUri(uri);
        final Context context = this.getContext();
        if (pluginUri == null || context == null) {
            return null;
        }
        return context.getContentResolver()
            .query(pluginUri, projection, selection, selectionArgs, sortOrder);
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final Uri pluginUri = this.getPluginUri(uri);
        final Context context = this.getContext();
        if (pluginUri == null || context == null) {
            return null;
        }
        return context.getContentResolver().insert(pluginUri, values);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }


    /**
     * 插件 uri
     * content.provider.plugin.PluginContentProvider
     *
     * 实际 uri 得写成
     * com.camnter.content.provider.plugin.host.StubContentProvider/content.provider.plugin.PluginContentProvider
     *
     * 发给插桩 ContentProvider
     *
     * @param rawUri rawUri
     * @return plugin uri
     */
    @Nullable
    private Uri getPluginUri(@NonNull final Uri rawUri) {
        final String rawAuthority = rawUri.getAuthority();
        if (!AUTHORITY.equals(rawAuthority)) {
            return null;
        }

        return Uri.parse(rawUri.toString().replaceAll(rawAuthority + '/', ""));
    }

}
