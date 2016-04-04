package com.camnter.newlife.utils;

import android.content.Context;
import java.lang.reflect.Field;

/**
 * Description：ReflectionUtil
 * Created by：CaMnter
 * Time：2015-11-20 12:08
 */
public class ReflectionUtils {

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
}
