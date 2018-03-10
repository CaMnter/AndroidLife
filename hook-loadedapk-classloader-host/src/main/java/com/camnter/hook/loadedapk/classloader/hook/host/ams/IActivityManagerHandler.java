package com.camnter.hook.loadedapk.classloader.hook.host.ams;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.util.Pair;
import com.camnter.hook.loadedapk.classloader.host.ActivityInfoUtils;
import com.camnter.hook.loadedapk.classloader.host.SmartApplication;
import com.camnter.hook.loadedapk.classloader.host.StubActivity;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 动态代理 AMS
 *
 * @author CaMnter
 */

public class IActivityManagerHandler implements InvocationHandler {

    private static final String TAG = IActivityManagerHandler.class.getSimpleName();

    Object base;


    IActivityManagerHandler(Object base) {
        this.base = base;
    }


    @SuppressWarnings("DanglingJavadoc")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        /**
         * API 25:
         *
         * public interface IActivityManager extends IInterface {
         *
         *     ...
         *
         *     public int startActivity(IApplicationThread caller, String callingPackage, Intent intent,
         *             String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags,
         *             ProfilerInfo profilerInfo, Bundle options) throws RemoteException;
         *
         *     ...
         *
         * }
         */
        if ("startActivity".equals(method.getName())) {
            final Pair<Integer, Intent> integerIntentPair = foundFirstIntentOfArgs(args);
            final Intent rawIntent = integerIntentPair.second;

            /**
             * 判断是否是插件 Activity
             */
            if (rawIntent != null) {
                final ActivityInfo activityInfo = ActivityInfoUtils.selectPluginActivity(
                    SmartApplication.getActivityInfoMap(), rawIntent);
                if (activityInfo != null) {
                    // 插件 Activity
                    // 代理 StubActivity 包名
                    final String proxyServicePackageName = SmartApplication.getContext()
                        .getPackageName();

                    // 启动的 Activity 替换为 StubActivity
                    final Intent intent = new Intent();
                    final ComponentName componentName = new ComponentName(proxyServicePackageName,
                        StubActivity.class.getName());
                    intent.setComponent(componentName);

                    // 保存原始 启动插件的 Activity Intent
                    intent.putExtra(AMSHooker.EXTRA_TARGET_INTENT, rawIntent);

                    // 替换掉 Intent, 欺骗 AMS
                    args[integerIntentPair.first] = intent;
                    Log.v(TAG,
                        "[IActivityManagerHandler]   [startActivity]   hook method startActivity success");
                }
            }
            return method.invoke(this.base, args);
        }
        return method.invoke(this.base, args);
    }


    /**
     * 寻找 参数里面的第一个Intent 对象
     *
     * @param args args
     * @return Pair
     */
    private Pair<Integer, Intent> foundFirstIntentOfArgs(Object... args) {
        int index = 0;

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Intent) {
                index = i;
                break;
            }
        }
        return Pair.create(index, (Intent) args[index]);
    }

}
