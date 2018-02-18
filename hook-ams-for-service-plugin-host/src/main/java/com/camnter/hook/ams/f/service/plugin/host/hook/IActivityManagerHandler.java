package com.camnter.hook.ams.f.service.plugin.host.hook;

import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.camnter.hook.ams.f.service.plugin.host.ProxyService;
import com.camnter.hook.ams.f.service.plugin.host.ProxyServiceManager;
import com.camnter.hook.ams.f.service.plugin.host.SmartApplication;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Refer form http://weishu.me/2016/05/11/understand-plugin-framework-service/
 *
 * 动态代理 AMS
 *
 * @author CaMnter
 */

public class IActivityManagerHandler implements InvocationHandler {

    private static final String TAG = IActivityManagerHandler.class.getSimpleName();

    Object base;


    public IActivityManagerHandler(Object base) {
        this.base = base;
    }


    @SuppressWarnings("DanglingJavadoc")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        if ("startService".equals(methodName)) {
            /**
             * 只拦截这个方法
             *
             * API 23:
             * public ComponentName startService(IApplicationThread caller,
             *                                   Intent service,
             *                                   String resolvedType,
             *                                   int userId) throws RemoteException
             */
            final Pair<Integer, Intent> integerIntentPair = foundFirstIntentOfArgs(args);

            // 代理 service 包名
            final String proxyServicePackageName = SmartApplication.getContext().getPackageName();

            // 启动的 Service 替换为 ProxyService
            final Intent intent = new Intent();
            final ComponentName componentName = new ComponentName(proxyServicePackageName,
                ProxyService.class.getName());
            intent.setComponent(componentName);
            // 保存原始 要启动的 TargetService
            intent.putExtra(AMSHooker.EXTRA_TARGET_INTENT, integerIntentPair.second);

            // 替换掉 Intent, 欺骗 AMS
            args[integerIntentPair.first] = intent;
            Log.v(TAG,
                "[IActivityManagerHandler]   [startService]   hook method startService success");
            return method.invoke(base, args);
        }

        /**
         * public int stopService(IApplicationThread caller,
         *                        Intent service,
         *                        String resolvedType,
         *                        int userId) throws RemoteException
         */
        if ("stopService".equals(methodName)) {
            final Intent rawIntent = foundFirstIntentOfArgs(args).second;
            final ComponentName rawComponentName = rawIntent.getComponent();
            final String rawPackageName = rawComponentName == null
                                          ? ""
                                          : rawComponentName.getPackageName();
            // 是否是 插件 的 intent
            if (TextUtils.equals(SmartApplication.getContext().getPackageName(),
                rawPackageName)) {
                Log.v(TAG,
                    "[IActivityManagerHandler]   [stopService]   hook method stopService success");
                ProxyServiceManager.getInstance().stopService(rawIntent);
            }
        }

        return method.invoke(base, args);
    }


    /**
     * 寻找 参数里面的第一个Intent 对象
     *
     * @param args args
     * @return Pair<Integer, Intent>
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
