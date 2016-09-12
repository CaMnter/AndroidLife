package com.camnter.newlife.utils;

import android.util.Log;
import java.lang.reflect.Field;

/**
 * Description：ClassUtils
 * Created by：CaMnter
 */

public class ClassUtils {

    private Field field;


    public ClassUtils(String className, String fieldName) {
        try {
            this.field = Class.forName(className).getDeclaredField(fieldName);
            this.field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static boolean replaceField(String className, String fieldName,
                                       Object desObj, Object fieldObj) {
        Field tempField;
        try {
            tempField = Class.forName(className).getDeclaredField(fieldName);
            tempField.setAccessible(true);
            tempField.set(desObj, fieldObj);
            return true;
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            Log.i("DEX", "" + e.getMessage());
            // e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }


    public Object get(Object desObj) {
        if (field == null) {
            return null;
        }
        try {
            return field.get(desObj);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


    public void set(Object desObj, Object fieldObj) {
        try {
            field.set(desObj, fieldObj);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
