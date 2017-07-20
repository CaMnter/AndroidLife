package com.camnter.smartrouter;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author CaMnter
 */

public class RouterBundle {

    @NonNull
    private final Uri uri;

    @NonNull
    private final Bundle bundle;


    public RouterBundle(@NonNull final Uri uri, @Nullable final Bundle bundle) {
        this.uri = uri;
        this.bundle = bundle == null ? new Bundle() : bundle;
    }


    @NonNull
    public Uri getUri() {
        return this.uri;
    }


    @NonNull
    public Bundle getBundle() {
        return this.bundle;
    }


    public String getString(@NonNull final String key) {
        return this.getString(key, "");
    }


    public int getInt(@NonNull final String key) {
        return this.getInt(key, 0);
    }


    public float getFloat(@NonNull final String key) {
        return this.getFloat(key, 0.0f);
    }


    public double getDouble(@NonNull final String key) {
        return this.getDouble(key, 0.0d);
    }


    public long getLong(@NonNull final String key) {
        return this.getLong(key, 0L);
    }


    public boolean getBoolean(@NonNull final String key) {
        return this.getBoolean(key, false);
    }


    public boolean containsKey(@NonNull final String key) {
        return this.bundle.containsKey(key) ||
            this.uri.getQueryParameter(key) != null;
    }


    private Object getCheckedValue(@NonNull final String key) {
        Object value = this.bundle.get(key);
        if (value == null) {
            value = this.uri.getQueryParameter(key);
        }
        return value;
    }


    private String getString(@NonNull final String key, @NonNull final String defaultValue) {
        Object value = this.getCheckedValue(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }


    private int getInt(@NonNull final String key, final int defaultValue) {
        Object value = this.getCheckedValue(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }


    private float getFloat(@NonNull final String key, final float defaultValue) {
        Object value = this.getCheckedValue(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value.toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }


    private double getDouble(@NonNull final String key, final double defaultValue) {
        Object value = this.getCheckedValue(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }


    private long getLong(@NonNull final String key, final long defaultValue) {
        Object value = this.getCheckedValue(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }


    private boolean getBoolean(@NonNull final String key, final boolean defaultValue) {
        Object value = this.getCheckedValue(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(value.toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

}
