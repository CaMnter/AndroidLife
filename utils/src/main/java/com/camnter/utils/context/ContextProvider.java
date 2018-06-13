package com.camnter.utils.context;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * 方便全局取 Context (IoDH 单例)
 *
 * @author CaMnter
 */

public class ContextProvider {

    private volatile Context context;


    /**
     * ContextProvider 自身被加载的时候没实例化单例
     */

    private ContextProvider() {

    }


    /**
     * 控制一个内部类，在 getInstance 的时候让内部类加载，实例化单例
     * 由 Java 虚拟机来保证其线程安全性，确保该成员变量只能初始化一次
     */
    public static class HolderClass {
        private static final ContextProvider instance = new ContextProvider();
    }


    public static ContextProvider getInstance() {
        return HolderClass.instance;
    }


    public static void setContext(@NonNull Context context) {
        getInstance().context = context.getApplicationContext();
    }


    public static Context getContext() {
        return getInstance().context;
    }

}
