package com.camnter.newlife.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * SharedPreferences Util
 * SharedPreferences工具类
 *
 * @author CaMnter
 *         2015-10-19
 */
public class SharedPreferencesUtils {

    public static final String TAG = "SharedPreferencesUtils";


    /**
     * --> String <--
     * Use SharedPreferences save the data
     * SharedPreferences保存数据
     */
    public static void save(Context context, String xmlName, String key, String value) {
        // 通过Activity自带的getSharedPreferences方法，得到SharedPreferences对象
        // 第一个参数表示保存后 xml 文件的名称(底层实现是将数据保存到xml文档中)。
        // 第二个参数表示xml文档的权限为私有，并且重新写的数据会覆盖掉原来的数据
        SharedPreferences preferences = context.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
        // 通过preferences得到它的编辑器对象edit
        SharedPreferences.Editor editor = preferences.edit();
        if ((value != null) && !"".equals(value.trim())) {
            // 通过编辑器将key属性和对应的value中输入的值写入到xml文档中
            editor.putString(key, value);
            // 添加数据完成后，提交编辑器的添加操作
            editor.apply();
        } else {
            Log.e(SharedPreferencesUtils.TAG, "The value parameter is invalid");
        }
    }


    /**
     * --> int <--
     * Use SharedPreferences save the data
     * SharedPreferences保存数据
     */
    public static void save(Context context, String xmlName, String key, int value) {
        // 通过Activity自带的getSharedPreferences方法，得到SharedPreferences对象
        // 第一个参数表示保存后 xml 文件的名称(底层实现是将数据保存到xml文档中)。
        // 第二个参数表示xml文档的权限为私有，并且重新写的数据会覆盖掉原来的数据
        SharedPreferences preferences = context.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
        // 通过preferences得到它的编辑器对象edit
        SharedPreferences.Editor editor = preferences.edit();
        // 通过编辑器将key属性和对应的value中输入的值写入到xml文档中
        editor.putInt(key, value);
        // 添加数据完成后，提交编辑器的添加操作
        editor.apply();
    }


    /**
     * --> int <--
     * Use SharedPreferences save the data
     * SharedPreferences保存数据
     */
    public static void save(Context context, String xmlName, String key, long value) {
        // 通过Activity自带的getSharedPreferences方法，得到SharedPreferences对象
        // 第一个参数表示保存后 xml 文件的名称(底层实现是将数据保存到xml文档中)。
        // 第二个参数表示xml文档的权限为私有，并且重新写的数据会覆盖掉原来的数据
        SharedPreferences preferences = context.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
        // 通过preferences得到它的编辑器对象edit
        SharedPreferences.Editor editor = preferences.edit();
        // 通过编辑器将key属性和对应的value中输入的值写入到xml文档中
        editor.putLong(key, value);
        // 添加数据完成后，提交编辑器的添加操作
        editor.apply();
    }


    /**
     * --> float <--
     * Use SharedPreferences save the data
     * SharedPreferences保存数据
     */
    public static void save(Context context, String xmlName, String key, float value) {
        // 通过Activity自带的getSharedPreferences方法，得到SharedPreferences对象
        // 第一个参数表示保存后 xml 文件的名称(底层实现是将数据保存到xml文档中)。
        // 第二个参数表示xml文档的权限为私有，并且重新写的数据会覆盖掉原来的数据
        SharedPreferences preferences = context.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
        // 通过preferences得到它的编辑器对象edit
        SharedPreferences.Editor editor = preferences.edit();
        // 通过编辑器将key属性和对应的value中输入的值写入到xml文档中
        editor.putFloat(key, value);
        // 添加数据完成后，提交编辑器的添加操作
        editor.apply();
    }


    /**
     * --> String <--
     * Use SharedPreferences load the data
     * SharedPreferences读取数据
     */
    public static String loadString(Context context, String xmlName, String key) {
        // 通过Activity自带的getSharedPreferences方法，得到SharedPreferences对象
        // 此时的第一个参数表示当前应用中的xmlName文件
        // 如果只读的话，第二个参数没有什么意义，但方法参数需要，可以随便写
        SharedPreferences preferences = context.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
        // 得到文件中的key标签值，第二个参数表示如果没有这个标签的话，返回的默认值
        return preferences.getString(key, null);
    }


    /**
     * --> int <--
     * Use SharedPreferences load the data
     * SharedPreferences读取数据
     */
    public static int loadInt(Context context, String xmlName, String key) {
        // 通过Activity自带的getSharedPreferences方法，得到SharedPreferences对象
        // 此时的第一个参数表示当前应用中的xmlName文件
        // 如果只读的话，第二个参数没有什么意义，但方法参数需要，可以随便写
        SharedPreferences preferences = context.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
        // 得到文件中的key标签值，第二个参数表示如果没有这个标签的话，返回的默认值
        return preferences.getInt(key, 0);
    }


    /**
     * --> long <--
     * Use SharedPreferences load the data
     * SharedPreferences读取数据
     */
    public static long loadLong(Context context, String xmlName, String key) {
        // 通过Activity自带的getSharedPreferences方法，得到SharedPreferences对象
        // 此时的第一个参数表示当前应用中的xmlName文件
        // 如果只读的话，第二个参数没有什么意义，但方法参数需要，可以随便写
        SharedPreferences preferences = context.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
        // 得到文件中的key标签值，第二个参数表示如果没有这个标签的话，返回的默认值
        return preferences.getLong(key, 0);
    }
}
