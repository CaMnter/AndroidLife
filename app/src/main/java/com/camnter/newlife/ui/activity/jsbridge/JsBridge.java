package com.camnter.newlife.ui.activity.jsbridge;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 协议:  JsBridge://className:callbackAddress/methodName?jsonObject
 *
 * Js 层通过 WebChromeClient 的 onJsPrompt, onJsAlert, onJsConfirm 回调方法
 * 调用到 JsBridge 方法去反射调用 native 逻辑代码
 *
 * Description：JsBridge
 * Created by：CaMnter
 */

public final class JsBridge {

    private static Map<String, HashMap<String, Method>> exposedMethods = new HashMap<>();


    public static void register(String exposedName, Class<? extends IBridge> clazz) {
        if (!exposedMethods.containsKey(exposedName)) {
            try {
                exposedMethods.put(exposedName, getAllMethod(clazz));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 反射获取 一个类的 所有方法
     *
     * @param injectClass 目标类
     * @return 所有方法键值对 < 方法名, 方法 >
     */
    private static HashMap<String, Method> getAllMethod(Class injectClass) {
        HashMap<String, Method> methodHashMap = new HashMap<>();
        Method[] methods = injectClass.getDeclaredMethods();
        for (Method method : methods) {
            String methodName;
            if (method.getModifiers() != (Modifier.PUBLIC | Modifier.STATIC) ||
                (methodName = method.getName()) == null) {
                continue;
            }
            Class[] parameters = method.getParameterTypes();
            if (parameters != null && parameters.length == 3) {
                if (parameters[0] == WebView.class && parameters[1] == JSONObject.class &&
                    parameters[2] == JsCallback.class) {
                    methodHashMap.put(methodName, method);
                }
            }
        }
        return methodHashMap;
    }


    /**
     * 协议:  JsBridge://className:callbackAddress/methodName?jsonObject
     *
     * Js 层通过 WebChromeClient.onJsPrompt(...) 等方法调用到 native 层的 callJava 方法
     *
     * @param webView WebView
     * @param uriString 协议
     */
    public static String callJava(WebView webView, String uriString) {
        String methodName = "";
        String className = "";
        String param = "{}";
        String port = "";
        if (!TextUtils.isEmpty(uriString) && uriString.startsWith("JsBridge")) {
            Uri uri = Uri.parse(uriString);
            className = uri.getHost();
            port = uri.getPort() + "";
            param = uri.getQuery();
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path)) {
                methodName = path.replace("/", "");
            }
        }

        if (exposedMethods.containsKey(className)) {
            HashMap<String, Method> methodHashMap = exposedMethods.get(className);

            if (methodHashMap != null &&
                methodHashMap.size() != 0 &&
                methodHashMap.containsKey(methodName)) {
                Method method = methodHashMap.get(methodName);
                if (method == null) {
                    return null;
                }
                try {
                    method.invoke(null, webView, new JSONObject(param),
                        new JsCallback(webView, port));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
