package com.camnter.newlife.widget.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.text.TextUtils;
import android.util.Pair;

/**
 * PreferencesProvider
 *
 * @author CaMnter
 */

public class PreferencesProvider extends ContentProvider {

    // 单一数据的 MIME 类型字符串应该以 vnd.android.cursor.item/ 开头
    protected static final String MIME_SINGLE = "vnd.android.cursor.item/";

    private static final String AUTHORITY = "com.camnter.preferences.provider";

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int CODE_PREFERENCES_STRING = 0x01;
    private static final String KET_PREFERENCES_STRING = "preferences/string";
    public static final String MIME_PREFERENCES_STRING = MIME_SINGLE + KET_PREFERENCES_STRING;
    public static final String URI_PATH_PREFERENCES_STRING = "content://" + AUTHORITY + "/" +
        KET_PREFERENCES_STRING;
    public static final Pair<String, Uri> URI_PAIR_STRING = new Pair<>(
        URI_PATH_PREFERENCES_STRING,
        Uri.parse(URI_PATH_PREFERENCES_STRING)
    );

    private static final int CODE_PREFERENCES_INT = 0x02;
    private static final String KET_PREFERENCES_INT = "preferences/int";
    public static final String MIME_PREFERENCES_INT = MIME_SINGLE + KET_PREFERENCES_INT;
    public static final String URI_PATH_PREFERENCES_INT = "content://" + AUTHORITY + "/" +
        KET_PREFERENCES_INT;
    public static final Pair<String, Uri> URI_PAIR_INT = new Pair<>(
        URI_PATH_PREFERENCES_INT,
        Uri.parse(URI_PATH_PREFERENCES_INT)
    );

    private static final int CODE_PREFERENCES_FLOAT = 0x03;
    private static final String KET_PREFERENCES_FLOAT = "preferences/float";
    public static final String MIME_PREFERENCES_FLOAT = MIME_SINGLE + KET_PREFERENCES_FLOAT;
    public static final String URI_PATH_PREFERENCES_FLOAT =
        "content://" + AUTHORITY + "/" + KET_PREFERENCES_FLOAT;
    public static final Pair<String, Uri> URI_PAIR_FLOAT = new Pair<>(
        URI_PATH_PREFERENCES_FLOAT,
        Uri.parse(URI_PATH_PREFERENCES_FLOAT)
    );

    private static final int CODE_PREFERENCES_LONG = 0x04;
    private static final String KET_PREFERENCES_LONG = "preferences/long";
    public static final String MIME_PREFERENCES_LONG = MIME_SINGLE + KET_PREFERENCES_LONG;
    public static final String URI_PATH_PREFERENCES_LONG =
        "content://" + AUTHORITY + "/" + KET_PREFERENCES_LONG;
    public static final Pair<String, Uri> URI_PAIR_LONG = new Pair<>(
        URI_PATH_PREFERENCES_LONG,
        Uri.parse(URI_PATH_PREFERENCES_LONG)
    );

    private static final int CODE_PREFERENCES_BOOLEAN = 0x05;
    private static final String KET_PREFERENCES_BOOLEAN = "preferences/boolean";
    public static final String MIME_PREFERENCES_BOOLEAN = MIME_SINGLE + KET_PREFERENCES_BOOLEAN;
    public static final String URI_PATH_PREFERENCES_BOOLEAN =
        "content://" + AUTHORITY + "/" + KET_PREFERENCES_BOOLEAN;
    public static final Pair<String, Uri> URI_PAIR_BOOLEAN = new Pair<>(
        URI_PATH_PREFERENCES_BOOLEAN,
        Uri.parse(URI_PATH_PREFERENCES_BOOLEAN)
    );


    @StringDef({ MIME_PREFERENCES_STRING, MIME_PREFERENCES_INT, MIME_PREFERENCES_FLOAT,
                   MIME_PREFERENCES_LONG, MIME_PREFERENCES_BOOLEAN })
    public @interface PreferencesMime {

    }


    @StringDef({ URI_PATH_PREFERENCES_STRING, URI_PATH_PREFERENCES_INT, URI_PATH_PREFERENCES_FLOAT,
                   URI_PATH_PREFERENCES_LONG, URI_PATH_PREFERENCES_BOOLEAN })
    public @interface UriPath {

    }


    static {
        URI_MATCHER.addURI(AUTHORITY, KET_PREFERENCES_STRING, CODE_PREFERENCES_STRING);
        URI_MATCHER.addURI(AUTHORITY, KET_PREFERENCES_INT, CODE_PREFERENCES_INT);
        URI_MATCHER.addURI(AUTHORITY, KET_PREFERENCES_FLOAT, CODE_PREFERENCES_FLOAT);
        URI_MATCHER.addURI(AUTHORITY, KET_PREFERENCES_LONG, CODE_PREFERENCES_LONG);
        URI_MATCHER.addURI(AUTHORITY, KET_PREFERENCES_BOOLEAN, CODE_PREFERENCES_BOOLEAN);
    }


    private static final String XML_PREFIX_NAME = "preferences_provider";
    private static final String XML_STRING = XML_PREFIX_NAME + "_string";
    private static final String XML_INT = XML_PREFIX_NAME + "_int";
    private static final String XML_FLOAT = XML_PREFIX_NAME + "_float";
    private static final String XML_LONG = XML_PREFIX_NAME + "_long";
    private static final String XML_BOOLEAN = XML_PREFIX_NAME + "_boolean";

    private SharedPreferences stringPreferences;
    private SharedPreferences intPreferences;
    private SharedPreferences floatPreferences;
    private SharedPreferences longPreferences;
    private SharedPreferences booleanPreferences;

    private static final String FIELD_KEY = "field_key";
    private static final String FIELD_VALUE = "field_value";


    @Override
    public boolean onCreate() {
        final Context context = this.getContext();
        if (context == null) return false;
        this.stringPreferences = context.getSharedPreferences(XML_STRING, Context.MODE_PRIVATE);
        this.intPreferences = context.getSharedPreferences(XML_INT, Context.MODE_PRIVATE);
        this.floatPreferences = context.getSharedPreferences(XML_FLOAT, Context.MODE_PRIVATE);
        this.longPreferences = context.getSharedPreferences(XML_LONG, Context.MODE_PRIVATE);
        this.booleanPreferences = context.getSharedPreferences(XML_BOOLEAN, Context.MODE_PRIVATE);
        return true;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case CODE_PREFERENCES_STRING:
                return MIME_PREFERENCES_STRING;
            case CODE_PREFERENCES_INT:
                return MIME_PREFERENCES_INT;
            case CODE_PREFERENCES_FLOAT:
                return MIME_PREFERENCES_FLOAT;
            case CODE_PREFERENCES_LONG:
                return MIME_PREFERENCES_LONG;
            case CODE_PREFERENCES_BOOLEAN:
                return MIME_PREFERENCES_BOOLEAN;
            default:
                return null;
        }
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values == null) return null;
        final String key = values.getAsString(FIELD_KEY);
        switch (URI_MATCHER.match(uri)) {
            case CODE_PREFERENCES_STRING:
                final String value = values.getAsString(FIELD_VALUE);
                final SharedPreferences.Editor editor = this.stringPreferences.edit();
                if ((value != null) && !"".equals(value.trim())) {
                    editor.putString(key, value);
                    editor.apply();
                }
                break;
            case CODE_PREFERENCES_INT: {
                final int intValue = values.getAsInteger(FIELD_VALUE);
                this.intPreferences.edit().putInt(key, intValue).apply();
                break;
            }
            case CODE_PREFERENCES_FLOAT: {
                final float floatValue = values.getAsFloat(FIELD_VALUE);
                this.floatPreferences.edit().putFloat(key, floatValue).apply();
                break;
            }
            case CODE_PREFERENCES_LONG: {
                final long longValue = values.getAsLong(FIELD_VALUE);
                this.longPreferences.edit().putLong(key, longValue).apply();
                break;
            }
            case CODE_PREFERENCES_BOOLEAN: {
                final boolean booleanValue = values.getAsBoolean(FIELD_VALUE);
                this.booleanPreferences.edit().putBoolean(key, booleanValue).apply();
                break;
            }
            default:
                break;
        }
        return null;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (TextUtils.isEmpty(selection)) return null;
        if (selectionArgs == null || selectionArgs.length == 0) return null;
        final PreferencesCursor cursor = new PreferencesCursor();
        final String key = selectionArgs[0];
        switch (selection) {
            case MIME_PREFERENCES_STRING:
                cursor.setStringValue(this.stringPreferences.getString(key, null));
                break;
            case MIME_PREFERENCES_INT:
                cursor.setIntValue(this.intPreferences.getInt(key, 0));
                break;
            case MIME_PREFERENCES_FLOAT:
                cursor.setFloatValue(this.floatPreferences.getFloat(key, 0.0f));
                break;
            case MIME_PREFERENCES_LONG:
                cursor.setLongValue(this.longPreferences.getLong(key, 0L));
                break;
            case MIME_PREFERENCES_BOOLEAN:
                cursor.setBooleanValue(this.booleanPreferences.getBoolean(key, false));
                break;
        }
        return cursor;
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (TextUtils.isEmpty(selection)) return 0;
        if (selectionArgs == null || selectionArgs.length == 0) return 0;
        final String key = selectionArgs[0];
        switch (selection) {
            case MIME_PREFERENCES_STRING:
                this.stringPreferences.edit().remove(key).apply();
                break;
            case MIME_PREFERENCES_INT:
                this.intPreferences.edit().remove(key).apply();
                break;
            case MIME_PREFERENCES_FLOAT:
                this.floatPreferences.edit().remove(key).apply();
                break;
            case MIME_PREFERENCES_LONG:
                this.longPreferences.edit().remove(key).apply();
                break;
            case MIME_PREFERENCES_BOOLEAN:
                this.booleanPreferences.edit().remove(key).apply();
                break;
        }
        return 0;
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

}
