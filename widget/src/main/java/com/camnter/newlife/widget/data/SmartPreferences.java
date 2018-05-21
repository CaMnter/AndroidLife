package com.camnter.newlife.widget.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.camnter.newlife.widget.data.PreferencesProvider.MIME_PREFERENCES_STRING;
import static com.camnter.newlife.widget.data.PreferencesProvider.URI_PAIR_BOOLEAN;
import static com.camnter.newlife.widget.data.PreferencesProvider.URI_PAIR_FLOAT;
import static com.camnter.newlife.widget.data.PreferencesProvider.URI_PAIR_INT;
import static com.camnter.newlife.widget.data.PreferencesProvider.URI_PAIR_LONG;
import static com.camnter.newlife.widget.data.PreferencesProvider.URI_PAIR_STRING;

/**
 * 跨进程 SharedPreferences
 *
 * @author CaMnter
 */

public class SmartPreferences {

    private static volatile SmartPreferences instance;


    private SmartPreferences() {

    }


    private static SmartPreferences get() {
        // 减少不必要的同步，volatile 能拿到最新的
        if (instance == null) {
            // 锁 class
            synchronized (SmartPreferences.class) {
                // 单个线程 初始化
                if (instance == null) {
                    instance = new SmartPreferences();
                }
            }
        }
        return instance;
    }


    public void save(@NonNull Context context,
                     @NonNull final String key,
                     @NonNull final String value) {
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(key, value);
        resolver.insert(URI_PAIR_STRING.second, values);
    }


    public void save(@NonNull Context context,
                     @NonNull final String key,
                     final int value) {
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(key, value);
        resolver.insert(URI_PAIR_INT.second, values);
    }


    public void save(@NonNull Context context,
                     @NonNull final String key,
                     final float value) {
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(key, value);
        resolver.insert(URI_PAIR_FLOAT.second, values);
    }


    public void save(@NonNull Context context,
                     @NonNull final String key,
                     final long value) {
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(key, value);
        resolver.insert(URI_PAIR_LONG.second, values);
    }


    public void save(@NonNull Context context,
                     @NonNull final String key,
                     final boolean value) {
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(key, value);
        resolver.insert(URI_PAIR_BOOLEAN.second, values);
    }


    public String load(@NonNull Context context,
                       @NonNull final String key,
                       @Nullable final String defaultValue) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor result = resolver.query(URI_PAIR_STRING.second, null, MIME_PREFERENCES_STRING,
            new String[] { key }, null);
        if (result != null) {
            if (result instanceof PreferencesCursor) {
                final PreferencesCursor preferencesCursor = (PreferencesCursor) result;
                final String value = preferencesCursor.getStringValue();
                result.close();
                return value == null ? defaultValue : value;
            }
            result.close();
            return defaultValue;
        } else {
            return defaultValue;
        }
    }


    public int load(@NonNull Context context,
                    @NonNull final String key,
                    final int defaultValue) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor result = resolver.query(URI_PAIR_INT.second, null, MIME_PREFERENCES_STRING,
            new String[] { key }, null);
        if (result != null) {
            if (result instanceof PreferencesCursor) {
                final PreferencesCursor preferencesCursor = (PreferencesCursor) result;
                final int value = preferencesCursor.getIntValue();
                result.close();
                return value == 0 ? defaultValue : value;
            }
            result.close();
            return defaultValue;
        } else {
            return defaultValue;
        }
    }


    public float load(@NonNull Context context,
                      @NonNull final String key,
                      final float defaultValue) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor result = resolver.query(URI_PAIR_FLOAT.second, null, MIME_PREFERENCES_STRING,
            new String[] { key }, null);
        if (result != null) {
            if (result instanceof PreferencesCursor) {
                final PreferencesCursor preferencesCursor = (PreferencesCursor) result;
                final float value = preferencesCursor.getFloatValue();
                result.close();
                return value == 0.0f ? defaultValue : value;
            }
            result.close();
            return defaultValue;
        } else {
            return defaultValue;
        }
    }


    public long load(@NonNull Context context,
                     @NonNull final String key,
                     final long defaultValue) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor result = resolver.query(URI_PAIR_LONG.second, null, MIME_PREFERENCES_STRING,
            new String[] { key }, null);
        if (result != null) {
            if (result instanceof PreferencesCursor) {
                final PreferencesCursor preferencesCursor = (PreferencesCursor) result;
                final long value = preferencesCursor.getLongValue();
                result.close();
                return value == 0L ? defaultValue : value;
            }
            result.close();
            return defaultValue;
        } else {
            return defaultValue;
        }
    }


    public boolean load(@NonNull Context context,
                        @NonNull final String key,
                        final boolean defaultValue) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor result = resolver.query(URI_PAIR_BOOLEAN.second, null, MIME_PREFERENCES_STRING,
            new String[] { key }, null);
        if (result != null) {
            if (result instanceof PreferencesCursor) {
                final PreferencesCursor preferencesCursor = (PreferencesCursor) result;
                final boolean value = preferencesCursor.isBooleanValue();
                result.close();
                // noinspection ConstantConditions
                return value ? defaultValue : value;
            }
            result.close();
            return defaultValue;
        } else {
            return defaultValue;
        }
    }

}
