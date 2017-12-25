package com.camnter.register.activity.plugin.host;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

/**
 * @author CaMnter
 */

public class SmartInstrumentation extends Instrumentation {

    private final ClassLoader classLoader;
    /**
     * 如果不是注册类型的 插件 Activity
     * 需要 hook Instrumentation#execStartActivity 等方法
     * 这个 baseInstrumentation 就会用得着了
     */
    private final Instrumentation baseInstrumentation;


    public SmartInstrumentation(ClassLoader classLoader, Instrumentation baseInstrumentation) {
        this.classLoader = classLoader;
        this.baseInstrumentation = baseInstrumentation;
    }


    /**
     * Perform instantiation of the process's {@link Activity} object.  The
     * default implementation provides the normal system behavior.
     *
     * @param cl The ClassLoader with which to instantiate the object.
     * @param className The name of the class implementing the Activity
     * object.
     * @param intent The Intent object that specified the activity class being
     * instantiated.
     * @return The newly instantiated Activity object.
     */
    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        //  hook 结果 其他 dex 中，相同 className 的 Activity
        return super.newActivity(this.classLoader, className, intent);
    }

}
