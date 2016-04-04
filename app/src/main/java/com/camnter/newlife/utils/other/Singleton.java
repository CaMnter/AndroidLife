package com.camnter.newlife.utils.other;

/**
 * Description：Singleton
 * Created by：CaMnter
 * Time：2016-02-27 22:07
 */
public class Singleton {

    private volatile static Singleton instance = null;


    private Singleton() {
    }


    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) instance = new Singleton();
            }
        }
        return instance;
    }
}
