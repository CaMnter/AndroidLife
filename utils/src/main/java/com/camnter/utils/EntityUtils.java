package com.camnter.utils;

import android.support.annotation.NonNull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author CaMnter
 */

public final class EntityUtils {

    /**
     * 利用反射实现对象之间属性复制
     *
     * @param from from
     * @param to to
     */
    public static void copyProperties(Object from, Object to) throws Exception {
        copyPropertiesExclude(from, to, null);
    }


    /**
     * 复制对象属性
     *
     * @param from from
     * @param to to
     * @param excludeArray 排除属性列表
     * @throws Exception exception
     */
    public static void copyPropertiesExclude(Object from, Object to, String[] excludeArray) throws
                                                                                            Exception {
        List<String> excludesList = null;
        if (excludeArray != null && excludeArray.length > 0) {
            excludesList = Arrays.asList(excludeArray);
        }
        Class fromClass = from.getClass();
        Class toClass = to.getClass();
        Field[] fromFields = fromClass.getDeclaredFields();
        Field[] toFields = toClass.getDeclaredFields();

        for (Field fromField : fromFields) {
            fromField.setAccessible(true);
            String fromFieldName = fromField.getName();
            Field toField;
            if ((excludesList != null && excludesList.contains(fromFieldName)) ||
                (toField = findFieldByName(toFields, fromFieldName)) == null) {
                continue;
            }
            toField.setAccessible(true);
            toField.set(to, fromField.get(from));
        }
    }


    /**
     * 对象属性值复制，仅复制指定名称的属性值
     *
     * @param from from
     * @param to to
     * @param includeArray includeArray
     * @throws Exception exception
     */
    @SuppressWarnings("unchecked")
    public static void copyPropertiesInclude(Object from, Object to, String[] includeArray) throws
                                                                                            Exception {
        List<String> includesList;
        if (includeArray != null && includeArray.length > 0) {
            includesList = Arrays.asList(includeArray);
        } else {
            return;
        }

        Class fromClass = from.getClass();
        Class toClass = to.getClass();
        Field[] fromFields = fromClass.getDeclaredFields();
        Field[] toFields = toClass.getDeclaredFields();

        for (Field fromField : fromFields) {
            fromField.setAccessible(true);
            String fromFieldName = fromField.getName();
            Field toField;
            if (!includesList.contains(fromFieldName) ||
                (toField = findFieldByName(toFields, fromFieldName)) == null) {
                continue;
            }
            // 集合类判空处理
            Object fromValue = fromField.get(from);
            if (fromValue instanceof Collection) {
                Collection newValue = (Collection) fromValue;
                if (newValue.size() <= 0) {
                    continue;
                }
            }
            toField.setAccessible(true);
            toField.set(to, fromField.get(from));
        }
    }


    /**
     * 从方法数组中获取指定名称的 方法
     *
     * @param methods methods
     * @param name name
     * @return Method
     */
    private static Method findMethodByName(Method[] methods, String name) {
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }


    /**
     * 从方法数组中获取指定名称的 属性
     *
     * @param fields fields
     * @param name name
     * @return Field
     */
    private static Field findFieldByName(@NonNull final Field[] fields,
                                         @NonNull final String name) {
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

}
