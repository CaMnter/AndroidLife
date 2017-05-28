package com.camnter.utils;

import android.content.Context;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Description：ReflectionUtils
 * Created by：CaMnter
 */
public class ReflectionUtils {

    /**
     * 获取一个类的属性值
     *
     * @param clazz clazz
     * @param fieldName 属性名
     * @param object 该类对象
     * @return 属性
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static Object getField(Class<?> clazz, String fieldName, Object object)
        throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }


    /**
     * 设置一个类的属性值
     *
     * @param clazz clazz
     * @param fieldName 属性名
     * @param object 该类对象
     * @param value 要设置的属性值
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static void setField(Class<?> clazz, String fieldName, Object object, Object value)
        throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }


    /**
     * 合并两个数组
     *
     * @param firstArray firstArray
     * @param secondArray secondArray
     * @return 合并后的数组
     */
    public static Object combineArray(Object firstArray, Object secondArray) {
        int firstLength = Array.getLength(firstArray);
        int secondLength = Array.getLength(secondArray);
        int length = firstLength + secondLength;

        Class<?> componentType = firstArray.getClass().getComponentType();
        Object newArray = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            if (i < firstLength) {
                Array.set(newArray, i, Array.get(firstArray, i));
            } else {
                Array.set(newArray, i, Array.get(secondArray, i - firstLength));
            }
        }
        return newArray;
    }


    /**
     * 根据名字，反射取得资源
     *
     * @param context context
     * @param name resources name
     * @param type enum of ResourcesType
     * @return resources id
     */
    public static int getResourceId(Context context, String name, ResourcesType type) {
        String className = context.getPackageName() + ".R";
        try {
            Class<?> c = Class.forName(className);
            for (Class childClass : c.getClasses()) {
                String simpleName = childClass.getSimpleName();
                if (simpleName.equals(type.name())) {
                    for (Field field : childClass.getFields()) {
                        String fieldName = field.getName();
                        if (fieldName.equals(name)) {
                            try {
                                return (int) field.get(null);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public enum ResourcesType {
        styleable,
        style,
        string,
        mipmap,
        menu,
        layout,
        integer,
        id,
        drawable,
        dimen,
        color,
        bool,
        attr,
        anim
    }
}
