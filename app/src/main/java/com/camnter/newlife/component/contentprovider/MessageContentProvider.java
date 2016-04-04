package com.camnter.newlife.component.contentprovider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Description：MessageContentProvider
 * Created by：CaMnter
 * Time：2015-11-12 16:24
 */
public class MessageContentProvider extends BaseContentProvider {

    // 主机名
    private static final String AUTHORITY = "com.camnter.content.provider";

    // Message uri
    public static final Uri MESSAGE_URI = Uri.parse("content://" + AUTHORITY + "/message");

    // 数据集的MIME类型字符串则应该以vnd.android.cursor.dir/开头
    private static final String TOPIC_SINGLE = MIME_SINGLE + "message";

    // 单一数据的MIME类型字符串应该以vnd.android.cursor.item/开头
    private static final String TOPIC_MULTIPLE = MIME_MULTIPLE + "message";

    // 有id匹配码
    private static final int MESSAGE = 6;
    // 有无匹配码
    public static final int MESSAGES = 7;

    // Message SQLite helper
    private MessageSQLiteHelper messageSQLiteHelper;

    private static final UriMatcher messageUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static {
        // content://com.camnter.content.provider/message
        messageUriMatcher.addURI(AUTHORITY, "message", MESSAGES);
        // content://com.camnter.content.provider/message/#
        messageUriMatcher.addURI(AUTHORITY, "message/#", MESSAGE);
    }


    @Override public boolean onCreate() {
        this.messageSQLiteHelper = MessageSQLiteHelper.getInstance(this.getContext());
        return true;
    }


    @Override public String getType(@NonNull Uri uri) {
        int match = messageUriMatcher.match(uri);
        switch (match) {
            case MESSAGE:
                return TOPIC_SINGLE;
            case MESSAGES:
                return TOPIC_MULTIPLE;
            default:
                return null;
        }
    }


    @Override public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = this.messageSQLiteHelper.getWritableDatabase();
        long id;
        switch (messageUriMatcher.match(uri)) {
            case MESSAGE:
                id = db.insert(MessageSQLiteHelper.TB_MESSAGE, "content", values);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);
            case MESSAGES:
                id = db.insert(MessageSQLiteHelper.TB_MESSAGE, "content", values);
                String path = uri.toString();
                this.getContext().getContentResolver().notifyChange(uri, null);
                // 新id的Uri替换旧id的Uri
                return Uri.parse(path.substring(0, path.lastIndexOf("/")) + "/" + id);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }


    @Override public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.messageSQLiteHelper.getWritableDatabase();
        int count;
        switch (messageUriMatcher.match(uri)) {
            case MESSAGE:
                long messageId = ContentUris.parseId(uri);
                // 指定id
                String where = "_id=" + messageId;
                // 把其它条件附加上
                where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";
                count = db.delete(MessageSQLiteHelper.TB_MESSAGE, where, selectionArgs);
                this.getContext().getContentResolver().notifyChange(uri, null);
                break;
            case MESSAGES:
                count = db.delete(MessageSQLiteHelper.TB_MESSAGE, selection, selectionArgs);
                this.getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return count;
    }


    @Override
    public int update(
            @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.messageSQLiteHelper.getWritableDatabase();
        int count;
        switch (messageUriMatcher.match(uri)) {
            case MESSAGE:
                long messageId = ContentUris.parseId(uri);
                // 指定id
                String where = "_id=" + messageId;
                // 把其它条件附加上
                where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";
                count = db.update(MessageSQLiteHelper.TB_MESSAGE, values, where, selectionArgs);
                this.getContext().getContentResolver().notifyChange(uri, null);
                break;
            case MESSAGES:
                count = db.update(MessageSQLiteHelper.TB_MESSAGE, values, selection, selectionArgs);
                this.getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return count;
    }


    @Override
    public Cursor query(@NonNull
                        Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = this.messageSQLiteHelper.getReadableDatabase();
        Cursor cursor;
        switch (messageUriMatcher.match(uri)) {
            case MESSAGE:
                long messageId = ContentUris.parseId(uri);
                // 指定id
                String where = "_id=" + messageId;
                // 把其它条件附加上
                where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";
                cursor = db.query(MessageSQLiteHelper.TB_MESSAGE, projection, where, selectionArgs,
                        null, null, sortOrder);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return cursor;
            case MESSAGES:
                cursor = db.query(MessageSQLiteHelper.TB_MESSAGE, projection, selection,
                        selectionArgs, null, null, sortOrder);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return cursor;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
}
