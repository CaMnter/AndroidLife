package com.camnter.smartrouter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.camnter.smartrouter.core.Filter;
import com.camnter.smartrouter.core.Router;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CaMnter
 */

public final class SmartRouters {

    private static final Map<String, Router> ROUTER_MAP = new HashMap<>();
    private static final Map<Class<? extends Activity>, Router> REGISTER_MAP = new HashMap<>();
    private static final Map<String, Class<? extends Activity>> ACTIVITY_CLASS_MAP
        = new HashMap<>();
    private static boolean LOADING_HISTORY_MARK = false;
    private static Class MANAGER_CLASS;
    private static String SCHEME = "routers";
    private static String HOST = "";
    private static Filter FILTER;


    public static String getScheme() {
        return SCHEME;
    }


    public static void setScheme(@NonNull final String scheme) {
        SCHEME = scheme;
    }


    public static String getHost() {
        return HOST;
    }


    public static void setHost(String host) {
        HOST = host;
    }


    public static Filter getFilter() {
        return FILTER;
    }


    public static void setFilter(Filter getFilter) {
        FILTER = getFilter;
    }


    public static void running(@Nullable final String scheme) {
        SCHEME = scheme;
        if (MANAGER_CLASS == null) {
            try {
                MANAGER_CLASS = Class.forName(
                    "com.camnter.smartrouter.MainRouterManagerClass");
                LOADING_HISTORY_MARK = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @SuppressWarnings("unchecked")
    public static void setFieldValue(@NonNull final Activity activity) {
        final String targetFullName = activity.getClass().getName();
        Router router = ROUTER_MAP.get(targetFullName);
        try {
            // finding by _SmartRouter
            if (router == null) {
                Class<?> routerClass = Class.forName(targetFullName + "_SmartRouter");
                Constructor constructor = routerClass.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);
                router = (Router) constructor.newInstance("");
                ROUTER_MAP.put(targetFullName, router);
            }
            router.setFieldValue(activity);
        } catch (Exception e) {
            // finding by register map
            router = REGISTER_MAP.get(activity.getClass());
            if (router != null) {
                router.setFieldValue(activity);
                return;
            }
            new Throwable("[SmartRouters]   [running]   " + targetFullName, e).printStackTrace();
        }
    }


    @SuppressWarnings("unchecked")
    public static void register(@NonNull final Router register) {
        REGISTER_MAP.put(register.getTargetActivityClass(), register);
        register.register(ACTIVITY_CLASS_MAP);
    }


    @SuppressWarnings("unchecked")
    private static Class<? extends Activity> getActivityClass(@NonNull final String url,
                                                              @NonNull final Uri uri) {
        final int index = url.indexOf('?');
        // scheme:host?
        final String schemeAndHost = index > 0 ? url.substring(0, index) : url;

        // check loading history
        if (!LOADING_HISTORY_MARK) {
            running(SCHEME);
        }
        final Class<? extends Activity> clazz = ACTIVITY_CLASS_MAP.get(schemeAndHost);
        if (clazz != null) {
            return clazz;
        }
        String host;
        if (SCHEME.equals(uri.getScheme())) {
            host = uri.getHost();
            return ACTIVITY_CLASS_MAP.get(host);
        }
        return null;
    }


    public static boolean start(@NonNull final Context context,
                                @NonNull final String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (FILTER != null) {
            final String mapUrl = FILTER.map(url);
            if (FILTER.start(context, mapUrl)) {
                return false;
            }
        }

        final Uri uri = Uri.parse(url);
        Class clazz = getActivityClass(url, uri);
        if (clazz == null) {
            new Throwable(url + " can't start").printStackTrace();
            return false;
        }

        Intent intent = new Intent(context, clazz);
        intent.setData(uri);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
        return true;
    }


    public static boolean startForResult(@NonNull final Activity activity,
                                         @NonNull final String url,
                                         final int requestCode) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (FILTER != null) {
            final String mapUrl = FILTER.map(url);
            if (FILTER.startForResult(activity, mapUrl, requestCode)) {
                return false;
            }
        }

        final Uri uri = Uri.parse(url);
        Class clazz = getActivityClass(url, uri);
        if (clazz == null) {
            new Throwable(url + "can't startForResult").printStackTrace();
            return false;
        }

        Intent intent = new Intent(activity, clazz);
        intent.setData(uri);
        activity.startActivityForResult(intent, requestCode);
        return true;
    }


    public static boolean startForResult(@NonNull final Fragment fragment,
                                         @NonNull final String url,
                                         final int requestCode) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (FILTER != null) {
            final String mapUrl = FILTER.map(url);
            if (FILTER.startForResult(fragment, mapUrl, requestCode)) {
                return false;
            }
        }

        final Uri uri = Uri.parse(url);
        Class clazz = getActivityClass(url, uri);
        if (clazz == null) {
            new Throwable(url + "can't startForResult").printStackTrace();
            return false;
        }

        Intent intent = new Intent(fragment.getActivity(), clazz);
        intent.setData(uri);
        fragment.startActivityForResult(intent, requestCode);
        return true;
    }


    public static boolean startForResult(@NonNull final android.support.v4.app.Fragment fragment,
                                         @NonNull final String url,
                                         final int requestCode) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (FILTER != null) {
            final String mapUrl = FILTER.map(url);
            if (FILTER.startForResult(fragment, mapUrl, requestCode)) {
                return false;
            }
        }

        final Uri uri = Uri.parse(url);
        Class clazz = getActivityClass(url, uri);
        if (clazz == null) {
            new Throwable(url + "can't startForResult").printStackTrace();
            return false;
        }

        Intent intent = new Intent(fragment.getActivity(), clazz);
        intent.setData(uri);
        fragment.startActivityForResult(intent, requestCode);
        return true;
    }

}
