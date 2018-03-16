package com.camnter.content.provider.plugin.plugin;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * @author CaMnter
 */

public class PluginContentProvider extends ContentProvider {

    private MessageSQLiteHelper messageSQLiteHelper;


    @Override
    public boolean onCreate() {
        this.messageSQLiteHelper = MessageSQLiteHelper.getInstance(this.getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase database = this.messageSQLiteHelper.getReadableDatabase();

        final Cursor cursor = database.query(MessageSQLiteHelper.TB_MESSAGE, projection, selection,
            selectionArgs, null, null, sortOrder);
        final Context context = this.getContext();
        if (null != context) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return cursor;
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase database = this.messageSQLiteHelper.getWritableDatabase();

        long id;
        id = database.insert(MessageSQLiteHelper.TB_MESSAGE, "content", values);
        this.getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

}
