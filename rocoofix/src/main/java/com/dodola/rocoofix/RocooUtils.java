/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.dodola.rocoofix;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sunpengfei on 16/7/30.
 *
 * https://github.com/dodola/RocooFix/blob/master/rocoo/src/main/java/com/dodola/rocoofix/RocooUtils.java
 *
 * https://android.googlesource.com/platform/frameworks/multidex/+/master/library/src/android/support/multidex/MultiDex.java
 */
public class RocooUtils {

    /**
     * 反射获取 目标类的 目标属性
     *
     * Locates a given field anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the field into.
     * @param name field name
     * @return a field object
     * @throws NoSuchFieldException if the field cannot be located
     */
    static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }


    /**
     * 反射获取 目标类的 目标方法
     *
     * Locates a given method anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the method into.
     * @param name method name
     * @param parameterTypes method parameter types
     * @return a method object
     * @throws NoSuchMethodException if the method cannot be located
     */
    static Method findMethod(Object instance, String name, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);

                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }

        throw new NoSuchMethodException(
            "Method " + name + " with parameters " + Arrays.asList(parameterTypes)
                + " not found in " + instance.getClass());
    }


    /**
     * 反射合并 两个数组
     * 要合并的数据(补丁)  插到 合并后数组的 头部
     *
     * Replace the value of a field containing a non null array, by a new array containing the
     * elements of the original
     * array plus the elements of extraElements.
     *
     * @param instance the instance whose field is to be modified.
     * @param fieldName the field to modify.
     * @param extraElements elements to append at the end of the array.
     */
    static void expandFieldArray(Object instance, String fieldName, Object[] extraElements)
        throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        // 反射获取 旧数组 数据
        Field jlrField = findField(instance, fieldName);
        // 转换为 旧 Object[]
        Object[] original = (Object[]) jlrField.get(instance);
        // 实例化一个 新 Object[],容纳 旧 Object[] 和 此次要合并的数组数据
        Object[] combined = (Object[]) Array.newInstance(
            original.getClass().getComponentType(), original.length + extraElements.length);
        // 先放入 此次要合并的数组数据
        System.arraycopy(extraElements, 0, combined, 0, extraElements.length);
        // 再 放入 就 Object[] 数据
        System.arraycopy(original, 0, combined, extraElements.length, original.length);
        // 修改 旧数组地址 内容
        jlrField.set(instance, combined);
    }


    public static Object[] makePathElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory,
                                            ArrayList<IOException> suppressedExceptions)
        throws IllegalAccessException, InvocationTargetException,
               NoSuchMethodException {
        return (Object[]) findMethod(dexPathList, "makePathElements", List.class, File.class,
            List.class).invoke(
            dexPathList, files, optimizedDirectory, suppressedExceptions);
    }

}