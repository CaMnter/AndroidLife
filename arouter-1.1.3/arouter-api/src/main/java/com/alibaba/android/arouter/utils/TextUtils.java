package com.alibaba.android.arouter.utils;

import android.net.Uri;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Text utils
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/9/9 14:40
 *
 * Text 工具类
 *
 * {@link TextUtils#isEmpty(CharSequence)}
 * 判断 CharSequence 空内容或者 空
 *
 * {@link TextUtils#formatStackTrace(StackTraceElement[])}
 * 打印 堆栈信息
 *
 * {@link TextUtils#splitQueryParameters(Uri)}
 * 从 Uri 中查找参数的键值对，返回一个 map
 *
 * 如果用 uri.getQueryParameter(String key) 的话，会很快就完成
 * 但是会调用 N 次 getQueryParameter，一次 getQueryParameter 就得 while 一趟
 * 就会有 N 次 while
 * 这样会浪费性能
 *
 * 这里一次 while 拿到所有 key value，部分源码参考了 getQueryParameter
 *
 * {@link TextUtils#getLeft(String)}
 * 拿到 | 左边的内容
 *
 * {@link TextUtils#getRight(String)}
 * 拿到 | 右边的内容
 */
public class TextUtils {

    /**
     * 判断 CharSequence 空内容或者 空
     *
     * @param cs cs
     * @return boolean
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }


    /**
     * Print thread stack
     *
     * 打印 堆栈信息
     */
    public static String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            sb.append("    at ").append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }


    /**
     * Split query parameters
     *
     * 从 Uri 中查找参数的键值对，返回一个 map
     *
     * 如果用 uri.getQueryParameter(String key) 的话，会很快就完成
     * 但是会调用 N 次 getQueryParameter，一次 getQueryParameter 就得 while 一趟
     * 就会有 N 次 while
     * 这样会浪费性能
     *
     * 这里一次 while 拿到所有 key value，部分源码参考了 getQueryParameter
     *
     * @param rawUri raw uri
     * @return map with params
     */
    public static Map<String, String> splitQueryParameters(Uri rawUri) {
        String query = rawUri.getEncodedQuery();

        if (query == null) {
            return Collections.emptyMap();
        }

        Map<String, String> paramMap = new LinkedHashMap<>();
        int start = 0;
        /*
         * 找到 & 的位置
         * 找到 = 的位置
         *
         * 确定 end 标记，value 的结束
         * 确定 separator 标记，key 的结束
         *
         * 拿到 key 和 value
         *
         * 一直循环下去，拿到全部 key value
         */
        do {
            int next = query.indexOf('&', start);
            int end = (next == -1) ? query.length() : next;

            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            String name = query.substring(start, separator);

            if (!android.text.TextUtils.isEmpty(name)) {
                String value = (separator == end ? "" : query.substring(separator + 1, end));
                paramMap.put(Uri.decode(name), Uri.decode(value));
            }

            // Move start to end of name.
            start = end + 1;
        } while (start < query.length());

        return Collections.unmodifiableMap(paramMap);
    }


    /**
     * Split key with |
     *
     * 拿到 | 左边的内容
     *
     * @param key raw key
     * @return left key
     */
    public static String getLeft(String key) {
        if (key.contains("|") && !key.endsWith("|")) {
            return key.substring(0, key.indexOf("|"));
        } else {
            return key;
        }
    }


    /**
     * Split key with |
     *
     * 拿到 | 右边的内容
     *
     * @param key raw key
     * @return right key
     */
    public static String getRight(String key) {
        if (key.contains("|") && !key.startsWith("|")) {
            return key.substring(key.indexOf("|") + 1);
        } else {
            return key;
        }
    }

}
