package com.camnter.hook.ams.f.activity.plugin.host.hook;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import com.camnter.hook.ams.f.activity.plugin.host.SmartApplication;
import java.lang.reflect.Field;

/**
 * 启动 Activity 时
 *
 * ContextImpl # execStartActivity -> Instrumentation # execStartActivity -> IActivityManager #
 * startActivity
 *
 * 进入了 AMS 所在的进程 system_server
 *
 * 然后，在该进程一直辗转与 ActivityStackSupervisor <-> ActivityStack
 *
 * 要回到 App 进程的时候，system_server 通过 ApplicationThread 这个 Binder proxy 对象回到 App 进程
 * 然后通过 H（ Handler ）类分发消息
 *
 * -------------------------------------------------------------------------------------------------
 *
 * public final class ActivityThread {
 *
 * -    ...
 *
 * -    private class H extends Handler {
 *
 * -        ...
 *
 * -        public static final int LAUNCH_ACTIVITY         = 100;
 *
 * -        ...
 *
 * -        public void handleMessage(Message msg) {
 * -            switch (msg.what) {
 * -                case LAUNCH_ACTIVITY: {
 * -                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
 * -                    final ActivityClientRecord r = (ActivityClientRecord) msg.obj;
 *
 * -                    r.packageInfo = getPackageInfoNoCheck(r.activityInfo.applicationInfo,
 * -                                                          r.compatInfo);
 * -                    handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
 * -                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
 * -            } break;
 *
 * -            ...
 *
 * -        }
 *
 * -        ...
 *
 * -    }
 *
 * -    ...
 *
 * }
 *
 * -------------------------------------------------------------------------------------------------
 *
 * 得在 system_server 的消息回来的时候，拦截这个 ApplicationThread 发出的  LAUNCH_ACTIVITY 消息
 *
 * -------------------------------------------------------------------------------------------------
 *
 * public class Handler {
 *
 * -    ...
 *
 * -    public void dispatchMessage(Message msg) {
 * -        if (msg.callback != null) {
 * -            handleCallback(msg);
 * -        } else {
 * -            if (mCallback != null) {
 * -                if (mCallback.handleMessage(msg)) {
 * -                    return;
 * -                }
 * -            }
 * -            handleMessage(msg);
 * -        }
 * -    }
 *
 * -    ...
 *
 * }
 *
 * -------------------------------------------------------------------------------------------------
 *
 * 庆幸的是，H 是选择 handleMessage(msg) 这种最低优先级的消息分发策略
 * 所以可以选择，在 H 添加 mCallback。这样就比 handleMessage(msg) 优先级高
 * 可以拦截到 system_server 回到 App 进程后，ApplicationThread 发出的 LAUNCH_ACTIVITY 消息
 *
 * -------------------------------------------------------------------------------------------------
 *
 * HCallback 就是为了填补 H # mCallback 而存在的
 *
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public class HCallback implements Handler.Callback {

    private static final int LAUNCH_ACTIVITY = 100;

    private Handler h;


    public HCallback(Handler h) {
        this.h = h;
    }


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case LAUNCH_ACTIVITY:
                this.handleLaunchActivity(msg);
                break;
        }

        this.h.handleMessage(msg);
        return true;
    }


    private void handleLaunchActivity(@NonNull final Message msg) {
        /**
         * public final class ActivityThread {
         *
         * -    ...
         *
         * -    private class H extends Handler {
         *
         * -        ...
         *
         * -        public static final int LAUNCH_ACTIVITY         = 100;
         *
         * -        ...
         *
         * -        public void handleMessage(Message msg) {
         * -            switch (msg.what) {
         * -                case LAUNCH_ACTIVITY: {
         * -                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
         * -                    final ActivityClientRecord r = (ActivityClientRecord) msg.obj;
         *
         * -                    r.packageInfo = getPackageInfoNoCheck(r.activityInfo.applicationInfo, r.compatInfo);
         * -                    handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
         * -                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
         * -            } break;
         *
         * -            ...
         *
         * -        }
         *
         * -        ...
         *
         * -    }
         *
         * -    ...
         *
         * }
         */

        /**
         * final ActivityClientRecord r = (ActivityClientRecord) msg.obj
         */
        try {
            final Object r = msg.obj;
            final Field intentField = r.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            final Intent intent = (Intent) intentField.get(r);

            /**
             * 取出 AMS 动态代理对象放入的 启动插件的 Activity Intent
             */
            final Intent rawIntent = intent.getParcelableExtra(AMSHooker.EXTRA_TARGET_INTENT);

            /**
             * 判断是否是插件 Activity
             */
            if (rawIntent != null) {
                final ActivityInfo activityInfo = ActivityInfoUtils.selectPluginActivity(
                    SmartApplication.getActivityInfoMap(), rawIntent);
                if (activityInfo != null) {
                    /**
                     * 替换启动的插件 Activity
                     *
                     * system_server 那边只知道的是 StubActivity
                     * 回到 App 进程时，附带的信息也是 StubActivity
                     *
                     * 这里将 StubActivity 换为 插件 Activity
                     *
                     * 保证了 App 进程这边一直都是 插件 Activity
                     */
                    intent.setComponent(rawIntent.getComponent());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
