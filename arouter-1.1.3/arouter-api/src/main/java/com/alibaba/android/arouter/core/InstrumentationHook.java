package com.alibaba.android.arouter.core;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.android.arouter.utils.Consts;
import com.alibaba.android.arouter.utils.TextUtils;
import java.lang.reflect.Field;

/**
 * Use ARouter.getInstance().inject(this) now!
 *
 * Hook the instrumentation, inject values for activity's field.
 * Support normal activity only, not contain unit test.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 2016/11/24 16:42
 *
 * 自定义了 Instrumentation 类，覆写了 newActivity 方法
 * 除了，执行原 newActivity 的 cl.loadClass(className).newInstance() 外
 *
 * 仅仅为了拿到该 Activity 的实例，然后反射 field，获取 intent 内传过来的规定结构的 String[]
 * 进行反射 field 赋值
 *
 * 即使是 private 的 field 也会被设置为 public，然后赋值
 *
 * 该类是一个 hook 类，之前版本被用来 hook 掉 ActivityThread 中的 field mInstrumentation
 * 然后，在每次 Activity 被打开的时候，会自动反射 field 赋值
 */
@Deprecated
public class InstrumentationHook extends Instrumentation {
    /**
     * Hook the instrumentation's newActivity, inject
     * <p>
     * Perform instantiation of the process's {@link Activity} object.  The
     * default implementation provides the normal system behavior.
     *
     * @param cl The ClassLoader with which to instantiate the object.
     * @param className The name of the class implementing the Activity
     * object.
     * @param intent The Intent object that specified the activity class being
     * instantiated.
     * @return The newly instantiated Activity object.
     *
     * 覆写了父类的 newActivity 方法
     * 同样也执行了父类中 newActivity 方法的 cl.loadClass(className).newInstance()
     *
     * 通过自动复制的通用 key ，从 intent 中获取 String[]，这里的每个 String 内容，都有 | 隔开
     * | 左边是 该 Activity 的 field name，用于反射 和 从 intent extra 取出 field 需要的 value
     * 最后给 field 赋值
     */
    @Override
    public Activity newActivity(ClassLoader cl, String className,
                                Intent intent)
        throws InstantiationException, IllegalAccessException,
               ClassNotFoundException {

        //        return (Activity)cl.loadClass(className).newInstance();

        Class<?> targetActivity = cl.loadClass(className);
        Object instanceOfTarget = targetActivity.newInstance();

        if (ARouter.canAutoInject()) {
            String[] autoInjectParams = intent.getStringArrayExtra(ARouter.AUTO_INJECT);
            if (null != autoInjectParams && autoInjectParams.length > 0) {
                for (String paramsName : autoInjectParams) {
                    Object value = intent.getExtras().get(TextUtils.getLeft(paramsName));
                    if (null != value) {
                        try {
                            Field injectField = targetActivity.getDeclaredField(
                                TextUtils.getLeft(paramsName));
                            injectField.setAccessible(true);
                            injectField.set(instanceOfTarget, value);
                        } catch (Exception e) {
                            ARouter.logger.error(Consts.TAG,
                                "Inject values for activity error! [" + e.getMessage() + "]");
                        }
                    }
                }
            }
        }

        return (Activity) instanceOfTarget;
    }
}
