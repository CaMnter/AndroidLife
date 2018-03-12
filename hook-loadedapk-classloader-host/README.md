# hook-loadedapk-classloader-host

## LoadedApk
 
<br>

> LoadedApk对象是APK文件在内存中的表示

<br>
<br>

# Hook LoadedApk for activity plugin

<br>

## Hook LoadedApk

<br>

**LoadedApkHooker**

```java
/**
 * Hook ActivityThread # ArrayMap<String, WeakReference<LoadedApk>> mPackages
 * 添加插件 LoadedApk
 *
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public final class LoadedApkHooker {

    /**
     * 缓存插件 LoadedApk
     * 因为 ActivityThread # ArrayMap<String, WeakReference<LoadedApk>> mPackages 是弱引用
     *
     * 所以，需要这个强引用去保存，防止回收
     */
    public static Map<String, Object> LOADEDAPK_MAP = new HashMap<>();


    /**
     * Hook ActivityThread # ArrayMap<String, WeakReference<LoadedApk>> mPackages
     *
     * @param apkFile apkFile
     * @param context context
     * @throws Exception Exception
     */
    @SuppressWarnings("unchecked")
    public static void hookLoadedApkForActivityThread(@NonNull final File apkFile,
                                                      @NonNull final Context context)
        throws Exception {

        /**
         * *****************************************************************************************
         *
         * ActivityThread 部分源码
         *
         * public final class ActivityThread {
         *
         *      ...
         *
         *      private static volatile ActivityThread sCurrentActivityThread;
         *
         *      ...
         *
         *      final ArrayMap<String, WeakReference<LoadedApk>> mPackages = new ArrayMap<String, WeakReference<LoadedApk>>();
         *
         *      ...
         *
         *      public final LoadedApk getPackageInfoNoCheck(ApplicationInfo ai, CompatibilityInfo compatInfo){
         *
         *          return getPackageInfo(ai, compatInfo, null, false, true, false);
         *
         *      }
         *
         *      ...
         *
         * }
         *
         * *****************************************************************************************
         *
         * CompatibilityInfo 部分源码
         *
         * public class CompatibilityInfo implements Parcelable {
         *
         *     ...
         *
         *     public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = new CompatibilityInfo() {};
         *
         *     ...
         *
         * }
         *
         * *****************************************************************************************
         *
         * LoadedApk 部分源码
         *
         * public final class LoadedApk {
         *
         *     ...
         *
         *     private ClassLoader mClassLoader;
         *
         *     ...
         *
         * }
         *
         * *****************************************************************************************
         */

        /**
         * 获取 ActivityThread 实例
         */
        final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        final Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod(
            "currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        final Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        /**
         * 获取 ActivityThread # ArrayMap<String, WeakReference<LoadedApk>> mPackages
         */
        final Field mPackagesField = activityThreadClass.getDeclaredField("mPackages");
        mPackagesField.setAccessible(true);
        final Map mPackages = (Map) mPackagesField.get(currentActivityThread);

        /**
         * 获取 CompatibilityInfo # CompatibilityInfo DEFAULT_COMPATIBILITY_INFO
         */
        final Class<?> compatibilityInfoClass = Class.forName(
            "android.content.res.CompatibilityInfo");
        final Field defaultCompatibilityInfoField = compatibilityInfoClass.getDeclaredField(
            "DEFAULT_COMPATIBILITY_INFO");
        defaultCompatibilityInfoField.setAccessible(true);
        final Object defaultCompatibilityInfo = defaultCompatibilityInfoField.get(null);

        /**
         * 获取 ApplicationInfo
         */
        final ApplicationInfo applicationInfo = getApplicationInfo(apkFile, context);

        /**
         * 调用 ActivityThread # getPackageInfoNoCheck
         * 获取插件 LoadedApk
         */
        final Method getPackageInfoNoCheckMethod = activityThreadClass.getDeclaredMethod(
            "getPackageInfoNoCheck", ApplicationInfo.class, compatibilityInfoClass);
        final Object loadedApk = getPackageInfoNoCheckMethod.invoke(currentActivityThread,
            applicationInfo, defaultCompatibilityInfo);

        /**
         * 创建一个 Classloader
         */
        final String odexPath = AssetsUtils.getPluginOptDexDir(context, applicationInfo.packageName)
            .getPath();
        final String libDir = AssetsUtils.getPluginLibDir(context, applicationInfo.packageName)
            .getPath();
        final ClassLoader classLoader = new SmartClassloader(
            apkFile.getPath(),
            odexPath,
            libDir,
            ClassLoader.getSystemClassLoader()
        );

        /**
         * Hook LoadedApk # ClassLoader mClassLoader
         */
        final Field mClassLoaderField = loadedApk.getClass().getDeclaredField("mClassLoader");
        mClassLoaderField.setAccessible(true);
        mClassLoaderField.set(loadedApk, classLoader);

        /**
         * 强引用缓存一份 插件 LoadedApk
         */
        LOADEDAPK_MAP.put(applicationInfo.packageName, loadedApk);

        /**
         * Hook ActivityThread # ArrayMap<String, WeakReference<LoadedApk>> mPackages
         */
        final WeakReference<Object> weakReference = new WeakReference<>(loadedApk);
        mPackages.put(applicationInfo.packageName, weakReference);
    }


    /**
     * 解析 Apk 文件中的 application
     * 并缓存
     *
     * 主要 调用 PackageParser 类的 generateApplicationInfo 方法
     *
     * @param apkFile apkFile
     * @throws Exception exception
     */
    @SuppressLint("PrivateApi")
    public static ApplicationInfo getApplicationInfo(@NonNull final File apkFile,
                                                     @NonNull final Context context)
        throws Exception {

        final ApplicationInfo applicationInfo;

        /**
         * 反射 获取 PackageParser # parsePackage(File packageFile, int flags)
         */
        final Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");

        /**
         * <= 4.0.0
         *
         * Don't deal with
         *
         * >= 4.0.0
         *
         * parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags)
         *
         * ---
         *
         * >= 5.0.0
         *
         * parsePackage(File packageFile, int flags)
         *
         */
        final int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            throw new RuntimeException(
                "[ApkUtils]   the sdk version must >= 14 (4.0.0)");
        }

        final Object packageParser;
        final Object packageObject;
        final Method parsePackageMethod;

        if (sdkVersion >= Build.VERSION_CODES.LOLLIPOP) {
            // >= 5.0.0
            // parsePackage(File packageFile, int flags)
            /**
             * 反射创建 PackageParser 对象，无参数构造
             *
             * 反射 调用 PackageParser # parsePackage(File packageFile, int flags)
             * 获取 apk 文件对应的 Package 对象
             */
            packageParser = packageParserClass.newInstance();

            parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage",
                File.class, int.class);
            packageObject = parsePackageMethod.invoke(
                packageParser,
                apkFile,
                PackageManager.GET_SERVICES
            );
        } else {
            // >= 4.0.0
            // parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags)
            /**
             * 反射创建 PackageParser 对象，PackageParser(String archiveSourcePath)
             *
             * 反射 调用 PackageParser # parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags)
             * 获取 apk 文件对应的 Package 对象
             */
            final String apkFileAbsolutePath = apkFile.getAbsolutePath();
            packageParser = packageParserClass.getConstructor(String.class)
                .newInstance(apkFileAbsolutePath);

            parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage",
                File.class, String.class, DisplayMetrics.class, int.class);
            packageObject = parsePackageMethod.invoke(
                packageParser,
                apkFile,
                apkFile.getAbsolutePath(),
                context.getResources().getDisplayMetrics(),
                PackageManager.GET_SERVICES
            );
        }

        final Class<?> packageParser$Package = Class.forName(
            "android.content.pm.PackageParser$Package");

        if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // >= 4.2.0
            // generateApplicationInfo(Package p, int flags, PackageUserState state)

            /**
             * 反射创建 PackageUserState 对象
             */
            final Class<?> packageUserStateClass = Class.forName(
                "android.content.pm.PackageUserState");
            final Object defaultUserState = packageUserStateClass.newInstance();

            // 需要调用 android.content.pm.PackageParser#generateApplicationInfo(Package p, int flags, PackageUserState state)
            final Method generateApplicationInfo = packageParserClass.getDeclaredMethod(
                "generateApplicationInfo",
                packageParser$Package, int.class, packageUserStateClass);

            applicationInfo = (ApplicationInfo) generateApplicationInfo.invoke(
                packageParser,
                packageObject,
                0 /*解析全部信息*/,
                defaultUserState
            );

        } else if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            // >= 4.1.0
            // generateApplicationInfo(Package p, int flags, boolean stopped, int enabledState, int userId)

            // 需要调用 android.content.pm.PackageParser#generateApplicationInfo(Package p, int flags, boolean stopped, int enabledState, int userId)
            final Class<?> userHandler = Class.forName("android.os.UserId");
            final Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            final int userId = (Integer) getCallingUserIdMethod.invoke(null);
            Method generateApplicationInfo = packageParserClass.getDeclaredMethod(
                "generateApplicationInfo",
                packageParser$Package, int.class, boolean.class, int.class);

            /**
             * 反射调用 PackageParser # generateApplicationInfo(Package p, int flags, boolean stopped, int enabledState, int userId)
             *
             * 在之前版本的 4.0.0 中 存在着
             * public class PackageParser {
             *     public final static class Package {
             *         // User set enabled state.
             *         public int mSetEnabled = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
             *
             *         // Whether the package has been stopped.
             *         public boolean mSetStopped = false;
             *     }
             * }
             *
             * 然后保存
             */
            applicationInfo = (ApplicationInfo) generateApplicationInfo.invoke(
                packageParser,
                packageObject,
                0 /*解析全部信息*/,
                false,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                userId
            );
        } else if (sdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // >= 4.0.0
            // generateApplicationInfo(Package p, int flags)

            // 需要调用 android.content.pm.PackageParser#generateApplicationInfo(Package p, int flags)
            Method generateApplicationInfo = packageParserClass.getDeclaredMethod(
                "generateApplicationInfo",
                packageParser$Package, int.class);

            /**
             * 反射调用 PackageParser # generateApplicationInfo(Package p, int flags)
             *
             * 然后保存
             */
            applicationInfo = (ApplicationInfo) generateApplicationInfo.invoke(
                packageParser,
                packageObject,
                0 /*解析全部信息*/
            );
        } else {
            applicationInfo = null;
        }

        return applicationInfo;

    }

}
```

<br>

**SmartClassloader**

```java
/**
 * @author CaMnter
 */

public class SmartClassloader extends DexClassLoader {

    public SmartClassloader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

}

```

<br>

## Hook PMS

<br>

**IPackageManagerHandler**

```java
/**
 * 动态代理 PMS
 *
 * @author CaMnter
 */

public class IPackageManagerHandler implements InvocationHandler {

    private Object base;


    IPackageManagerHandler(@NonNull final Object base) {
        this.base = base;
    }


    @SuppressWarnings("DanglingJavadoc")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        /**
         * *****************************************************************************************
         *
         * LoadedApk 部分源码
         *
         * public final class LoadedApk {
         *
         *      ...
         *
         *      public Application makeApplication(boolean forceDefaultAppClass, Instrumentation instrumentation) {
         *
         *          ...
         *
         *          try {
         *              java.lang.ClassLoader cl = getClassLoader();
         *              if (!mPackageName.equals("android")) {
         *                  Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER,
         *                          "initializeJavaContextClassLoader");
         *                  initializeJavaContextClassLoader();
         *                  Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
         *              }
         *              ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);
         *              app = mActivityThread.mInstrumentation.newApplication(
         *                      cl, appClass, appContext);
         *              appContext.setOuterContext(app);
         *          } catch (Exception e) {
         *              ...
         *          }
         *
         *          ...
         *
         *      }
         *
         *      ...
         *
         *      private void initializeJavaContextClassLoader() {
         *           IPackageManager pm = ActivityThread.getPackageManager();
         *           android.content.pm.PackageInfo pi;
         *           try {
         *               pi = pm.getPackageInfo(mPackageName, PackageManager.MATCH_DEBUG_TRIAGED_MISSING,
         *                       UserHandle.myUserId());
         *           } catch (RemoteException e) {
         *               throw e.rethrowFromSystemServer();
         *           }
         *           if (pi == null) {
         *               throw new IllegalStateException("Unable to get package info for "
         *                       + mPackageName + "; is package not installed?");
         *           }
         *
         *           ...
         *
         *      }
         *
         *      ...
         *
         * }
         *
         * *****************************************************************************************
         */

        /**
         * LoadedApk # initializeJavaContextClassLoader 中的 IPackageManager # getPackageInfo
         *
         * 如果是插件 LoadedApk
         * 那么是我们通过 hook 和 反射创建出来的，内部的 PMS 也是取 ActivityThread 中的 PMS
         * 就是宿主 PMS
         *
         * 这里开始就是加载非 android 开头的 package
         * 如果用宿主的 PMS 去加载自己的 PackageInfo 是 OK 的
         *
         * 但是加载 插件的 PackageInfo 是 null
         *
         * 为了欺骗 PMS，为了不抛出上述源码中的 "package not installed?" 异常
         *
         * 选择 检查 IPackageManager # getPackageInfo 的值
         * 如果为 null，暂且视为 宿主 PMS 加载插件 PackageInfo
         * 强行 new 一个 PackageInfo
         *
         * 骗过 PMS，跳过验证
         */

        if ("getPackageInfo".equals(method.getName())) {
            final Object tryToValue = method.invoke(this.base, args);
            return null == tryToValue ? new PackageInfo() : tryToValue;
        }
        return method.invoke(this.base, args);
    }

}
```

<br>

**PMSHooker**

```java
/**
 * @author CaMnter
 */

public final class PMSHooker {

    /**
     * 1. Hook ActivityThread 中的 sPackageManager
     * 2. Hook Context.getPackageManager() 后得到的 ApplicationPackageManager 中的 mPM
     *
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     * @throws NoSuchFieldException NoSuchFieldException
     */
    @SuppressWarnings("DanglingJavadoc")
    @SuppressLint("PrivateApi")
    public static void hookPackageManagerService(@NonNull final Context context)
        throws ClassNotFoundException,
               NoSuchMethodException,
               InvocationTargetException,
               IllegalAccessException,
               NoSuchFieldException {

        /**
         * class ContextImpl extends Context {
         *
         * -    ...
         *
         * -    @Override
         * -    public PackageManager getPackageManager() {
         * -        if (mPackageManager != null) {
         * -            return mPackageManager;
         * -        }
         *
         * -        IPackageManager pm = ActivityThread.getPackageManager();
         * -        if (pm != null) {
         * -            // Doesn't matter if we make more than one instance.
         * -            return (mPackageManager = new ApplicationPackageManager(this, pm));
         * -        }
         *
         * -        return null;
         * -    }
         *
         * -    ...
         *
         * }
         *
         * public final class ActivityThread {
         *
         * -    ...
         *
         * -    static volatile IPackageManager sPackageManager;
         *
         * -    public static IPackageManager getPackageManager() {
         * -        if (sPackageManager != null) {
         * -            //Slog.v("PackageManager", "returning cur default = " + sPackageManager);
         * -            return sPackageManager;
         * -        }
         * -        IBinder b = ServiceManager.getService("package");
         * -        //Slog.v("PackageManager", "default service binder = " + b);
         * -        sPackageManager = IPackageManager.Stub.asInterface(b);
         * -        //Slog.v("PackageManager", "default service = " + sPackageManager);
         * -        return sPackageManager;
         * -    }
         *
         * -    ...
         *
         * }
         *
         * 1. 会先从 ActivityThread.getPackageManager() 获取 PMS。所以得 hook ActivityThread 内的 sPackageManager。
         * 2. 然后下面会进行 PMS 的包装，变为 ApplicationPackageManager。
         *
         * public class ApplicationPackageManager extends PackageManager {
         *
         * -    ...
         *
         * -    private final IPackageManager mPM;
         *
         * -    protected ApplicationPackageManager(ContextImpl context, IPackageManager pm) {
         * -        mContext = context;
         * -        mPM = pm;
         * -    }
         *
         * -    ...
         *
         * }
         *
         * 3. 可以看得出，还需要 hook ApplicationPackageManager 内的 mPM。
         */

        /**
         * 获取 ActivityThread # ActivityThread currentActivityThread
         */
        final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        final Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod(
            "currentActivityThread");
        final Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        /**
         * 获取 ActivityThread # IPackageManager sPackageManager
         */
        final Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
        sPackageManagerField.setAccessible(true);
        final Object sPackageManager = sPackageManagerField.get(currentActivityThread);

        /**
         * 动态代理 创建一个 IPackageManager 代理类
         */
        final Class<?> iPackageManagerInterface = Class.forName(
            "android.content.pm.IPackageManager");
        final Object proxy = Proxy.newProxyInstance(
            iPackageManagerInterface.getClassLoader(),
            new Class<?>[] { iPackageManagerInterface },
            new IPackageManagerHandler(sPackageManager)
        );

        /**
         * hook ActivityThread # IPackageManager sPackageManager
         */
        sPackageManagerField.set(currentActivityThread, proxy);

        /**
         * hook Context.getPackageManager() 后得到的 ApplicationPackageManager # IPackageManager mPM
         */
        final PackageManager packageManager = context.getPackageManager();
        final Field mPmField = packageManager.getClass().getDeclaredField("mPM");
        mPmField.setAccessible(true);
        mPmField.set(packageManager, proxy);
    }

}
```

<br>

## Hook AMS and ActivityThread # H # mCallback

<br>

**ActivityInfoUtils**

```java
/**
 * 收集 apk 内的 activity 信息
 *
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public final class ActivityInfoUtils {

    /**
     * 匹配 ActivityInfo 缓存
     *
     * @param pluginIntent 插件 Intent
     * @return 插件 intent 的 ActivityInfo
     */
    public static ActivityInfo selectPluginActivity(final Map<ComponentName, ActivityInfo> activityInfoMap, Intent pluginIntent) {
        for (ComponentName componentName : activityInfoMap.keySet()) {
            if (componentName.equals(pluginIntent.getComponent())) {
                return activityInfoMap.get(componentName);
            }
        }
        return null;
    }


    /**
     * 解析 Apk 文件中的 <activity>
     * 并缓存
     *
     * 主要 调用 PackageParser 类的 generateActivityInfo 方法
     *
     * @param apkFile apkFile
     * @throws Exception exception
     */
    @SuppressLint("PrivateApi")
    public static Map<ComponentName, ActivityInfo> preLoadActivities(@NonNull final File apkFile, final Context context)
        throws Exception {

        final Map<ComponentName, ActivityInfo> activityInfoMap = new HashMap<>();

        /**
         * 反射 获取 PackageParser # parsePackage(File packageFile, int flags)
         */
        final Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");

        /**
         * <= 4.0.0
         *
         * Don't deal with
         *
         * >= 4.0.0
         *
         * parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags)
         *
         * ---
         *
         * >= 5.0.0
         *
         * parsePackage(File packageFile, int flags)
         *
         */
        final int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            throw new RuntimeException(
                "[ActivityInfoUtils]   the sdk version must >= 14 (4.0.0)");
        }

        final Object packageParser;
        final Object packageObject;
        final Method parsePackageMethod;

        if (sdkVersion >= Build.VERSION_CODES.LOLLIPOP) {
            // >= 5.0.0
            // parsePackage(File packageFile, int flags)
            /**
             * 反射创建 PackageParser 对象，无参数构造
             *
             * 反射 调用 PackageParser # parsePackage(File packageFile, int flags)
             * 获取 apk 文件对应的 Package 对象
             */
            packageParser = packageParserClass.newInstance();

            parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage",
                File.class, int.class);
            packageObject = parsePackageMethod.invoke(
                packageParser,
                apkFile,
                PackageManager.GET_ACTIVITIES
            );
        } else {
            // >= 4.0.0
            // parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags)
            /**
             * 反射创建 PackageParser 对象，PackageParser(String archiveSourcePath)
             *
             * 反射 调用 PackageParser # parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags)
             * 获取 apk 文件对应的 Package 对象
             */
            final String apkFileAbsolutePath = apkFile.getAbsolutePath();
            packageParser = packageParserClass.getConstructor(String.class)
                .newInstance(apkFileAbsolutePath);

            parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage",
                File.class, String.class, DisplayMetrics.class, int.class);
            packageObject = parsePackageMethod.invoke(
                packageParser,
                apkFile,
                apkFile.getAbsolutePath(),
                context.getResources().getDisplayMetrics(),
                PackageManager.GET_ACTIVITIES
            );
        }

        if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // >= 4.2.0
            // generateActivityInfo(Activity a, int flags, PackageUserState state, int userId)
            /**
             * 读取 Package # ArrayList<Activity> activities
             * 通过 ArrayList<Activity> activities 获取 Activity 对应的 ActivityInfo
             */
            final Field activitiesField = packageObject.getClass().getDeclaredField("activities");
            final List activities = (List) activitiesField.get(packageObject);

            /**
             * 反射调用 UserHandle # static @UserIdInt int getCallingUserId()
             * 获取到 userId
             *
             * 反射创建 PackageUserState 对象
             */
            final Class<?> packageParser$ActivityClass = Class.forName(
                "android.content.pm.PackageParser$Activity");
            final Class<?> packageUserStateClass = Class.forName(
                "android.content.pm.PackageUserState");
            final Class<?> userHandler = Class.forName("android.os.UserHandle");
            final Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            final int userId = (Integer) getCallingUserIdMethod.invoke(null);
            final Object defaultUserState = packageUserStateClass.newInstance();

            // 需要调用 android.content.pm.PackageParser#generateActivityInfo(Activity a, int flags, PackageUserState state, int userId)
            final Method generateActivityInfo = packageParserClass.getDeclaredMethod(
                "generateActivityInfo",
                packageParser$ActivityClass, int.class, packageUserStateClass, int.class);

            /**
             * 反射调用 PackageParser # generateActivityInfo(Activity a, int flags, PackageUserState state, int userId)
             * 解析出 Activity 对应的 ActivityInfo
             *
             * 然后保存
             */
            for (Object activity : activities) {
                final ActivityInfo info = (ActivityInfo) generateActivityInfo.invoke(
                    packageParser,
                    activity,
                    0,
                    defaultUserState,
                    userId
                );
                activityInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        } else if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            // >= 4.1.0
            // generateActivityInfo(Activity a, int flags, boolean stopped, int enabledState, int userId)
            /**
             * 读取 Package # ArrayList<Activity> activities
             * 通过 ArrayList<Activity> activities 获取 Activity 对应的 ActivityInfo
             */
            final Field activitiesField = packageObject.getClass().getDeclaredField("activities");
            final List activities = (List) activitiesField.get(packageObject);

            // 需要调用 android.content.pm.PackageParser#generateActivityInfo(Activity a, int flags, boolean stopped, int enabledState, int userId)
            final Class<?> packageParser$ActivityClass = Class.forName(
                "android.content.pm.PackageParser$Activity");
            final Class<?> userHandler = Class.forName("android.os.UserId");
            final Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            final int userId = (Integer) getCallingUserIdMethod.invoke(null);
            final Method generateActivityInfo = packageParserClass.getDeclaredMethod(
                "generateActivityInfo",
                packageParser$ActivityClass, int.class, boolean.class, int.class, int.class);

            /**
             * 反射调用 PackageParser # generateActivityInfo(Activity a, int flags, boolean stopped, int enabledState, int userId)
             * 解析出 Activity 对应的 ActivityInfo
             *
             * 在之前版本的 4.0.0 中 存在着
             * public class PackageParser {
             *     public final static class Package {
             *         // User set enabled state.
             *         public int mSetEnabled = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
             *
             *         // Whether the package has been stopped.
             *         public boolean mSetStopped = false;
             *     }
             * }
             *
             * 然后保存
             */
            for (Object activity : activities) {
                final ActivityInfo info = (ActivityInfo) generateActivityInfo.invoke(
                    packageParser,
                    activity,
                    0,
                    false,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    userId
                );
                activityInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        } else if (sdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // >= 4.0.0
            // generateActivityInfo(Activity a, int flags)
            /**
             * 读取 Package # ArrayList<Activity> activities
             * 通过 ArrayList<Activity> activities 获取 Activity 对应的 ActivityInfo
             */
            final Field activitiesField = packageObject.getClass().getDeclaredField("activities");
            final List activities = (List) activitiesField.get(packageObject);

            // 需要调用 android.content.pm.PackageParser#generateActivityInfo(Activity a, int flags)
            final Class<?> packageParser$ActivityClass = Class.forName(
                "android.content.pm.PackageParser$Activity");
            final Method generateActivityInfo = packageParserClass.getDeclaredMethod(
                "generateActivityInfo",
                packageParser$ActivityClass, int.class);

            /**
             * 反射调用 PackageParser # generateActivityInfo(Activity a, int flags)
             * 解析出 Activity 对应的 ActivityInfo
             *
             * 然后保存
             */
            for (Object activity : activities) {
                final ActivityInfo info = (ActivityInfo) generateActivityInfo.invoke(
                    packageParser,
                    activity,
                    0
                );
                activityInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        }

        return activityInfoMap;

    }

}
```

<br>

**IActivityManagerHandler**

```java
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
```

<br>

**HCallback**

```java
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


    HCallback(Handler h) {
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
                     * 替换 ActivityThread # ActivityClientRecord # ActivityInfo activityInfo
                     * 不然在生成 获取 LoadedApk 缓存的时候，误拿了 插桩 Activity 的 LoadedApk
                     * 导致 LoadedApk 内的 classloader 加载不到 插件 activity
                     *
                     * r.packageInfo = getPackageInfoNoCheck(r.activityInfo.applicationInfo, r.compatInfo);
                     *
                     * 这里导致，会拿错 LoadedApk 缓存
                     */
                    final Field activityInfoField = r.getClass().getDeclaredField("activityInfo");
                    activityInfoField.setAccessible(true);
                    activityInfoField.set(r, activityInfo);

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
```

<br>

**AMSHooker**

```java
/**
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public final class AMSHooker {

    static final String EXTRA_TARGET_INTENT = "extra_target_intent";


    /**
     * Hook ActivityManagerNative 这个 Binder 中的 AMS
     *
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     * @throws NoSuchFieldException NoSuchFieldException
     */
    @SuppressLint("PrivateApi")
    public static void hookActivityManagerNative() throws ClassNotFoundException,
                                                          NoSuchMethodException,
                                                          InvocationTargetException,
                                                          IllegalAccessException,
                                                          NoSuchFieldException {

        /**
         * *****************************************************************************************
         *
         * ActivityManagerNative 部分源码
         *
         *
         * public abstract class ActivityManagerNative extends Binder implements IActivityManager{
         *
         *      ...
         *
         *      private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
         *          protected IActivityManager create() {
         *              IBinder b = ServiceManager.getService("activity");
         *              if (false) {
         *                  Log.v("ActivityManager", "default service binder = " + b);
         *              }
         *              IActivityManager am = asInterface(b);
         *              if (false) {
         *                  Log.v("ActivityManager", "default service = " + am);
         *              }
         *              return am;
         *          }
         *      };
         *
         *      ...
         *
         *
         * }
         *
         * *****************************************************************************************
         *
         * Singleton 的类结构
         *
         * package android.util;
         *
         *  **
         *  * Singleton helper class for lazily initialization.
         *  *
         *  * Modeled after frameworks/base/include/utils/Singleton.h
         *  *
         *  * @hide
         *  **
         *
         * public abstract class Singleton<T> {
         *      private T mInstance;
         *
         *      protected abstract T create();
         *
         *      public final T get() {
         *          synchronized (this) {
         *              if (mInstance == null) {
         *                  mInstance = create();
         *              }
         *                  return mInstance;
         *          }
         *      }
         * }
         *
         * *****************************************************************************************
         */

        final Field gDefaultField;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Class<?> activityManager = Class.forName("android.app.ActivityManager");
            gDefaultField = activityManager.getDeclaredField("IActivityManagerSingleton");
        } else {
            Class<?> activityManagerNativeClass = Class.forName(
                "android.app.ActivityManagerNative");
            gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
        }
        gDefaultField.setAccessible(true);

        // IActivityManager
        final Object gDefault = gDefaultField.get(null);

        // 反射获取 IActivityManager # android.util.Singleton 的实例
        final Class<?> singletonClass = Class.forName("android.util.Singleton");
        final Field mInstanceField = singletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);

        // 获取 ActivityManagerNative 中的 gDefault 对象里面原始的 IActivityManager 实例
        final Object rawIActivityManager = mInstanceField.get(gDefault);

        // 动态代理 创建一个 IActivityManager 代理类
        final Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
        final Object proxy = Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class<?>[] { iActivityManagerInterface },
            new IActivityManagerHandler(rawIActivityManager)
        );

        /**
         * 用 动态代理 创建的 IActivityManager 实现类
         * Hook 掉 IActivityManager # Singleton # mInstance
         */
        mInstanceField.set(gDefault, proxy);
    }


    /**
     * Hook ActivityThread 中 H 这个 Handler
     * 添加 mCallback
     *
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     * @throws NoSuchFieldException NoSuchFieldException
     */
    public static void hookActivityThreadH() throws ClassNotFoundException,
                                                    NoSuchMethodException,
                                                    InvocationTargetException,
                                                    IllegalAccessException,
                                                    NoSuchFieldException {
        /**
         * *****************************************************************************************
         *
         * ActivityThread 部分源码
         *
         * public final class ActivityThread {
         *
         *      ...
         *
         *      private class H extends Handler {
         *
         *          ...
         *
         *      }
         *
         *      ...
         *
         * }
         *
         * *****************************************************************************************
         *
         * Handler 部分源码
         *
         * public class Handler {
         *
         *     ...
         *
         *     final Callback mCallback;
         *
         *     public void dispatchMessage(Message msg) {
         *         if (msg.callback != null) {
         *             handleCallback(msg);
         *         } else {
         *            if (mCallback != null) {
         *                if (mCallback.handleMessage(msg)) {
         *                     return;
         *                }
         *            }
         *            handleMessage(msg);
         *         }
         *     }
         *
         *     ...
         *
         * }
         */

        /**
         * 获取 ActivityThread
         */
        final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        final Field currentActivityThreadField = activityThreadClass.getDeclaredField(
            "sCurrentActivityThread");
        currentActivityThreadField.setAccessible(true);
        final Object currentActivityThread = currentActivityThreadField.get(null);

        /**
         * 获取 ActivityThread # final H mH = new H()
         */
        final Field mHField = activityThreadClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        final Handler mH = (Handler) mHField.get(currentActivityThread);

        /**
         * Hook 掉 H # mCallback
         */
        final Field mCallBackField = Handler.class.getDeclaredField("mCallback");
        mCallBackField.setAccessible(true);
        mCallBackField.set(mH, new HCallback(mH));
    }

}
```

<br>

## Application 添加 Hook

<br>

```java
/**
 * @author CaMnter
 */

public class SmartApplication extends Application {

    private static Context BASE;

    private static Map<ComponentName, ActivityInfo> activityInfoMap;


    /**
     * Set the base context for this ContextWrapper.  All calls will then be
     * delegated to the base context.  Throws
     * IllegalStateException if a base context has already been set.
     *
     * @param base The new base context for this wrapper.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        BASE = base;
        try {
            // Hook AMS
            AMSHooker.hookActivityManagerNative();
            // Hook H
            AMSHooker.hookActivityThreadH();
            // assets 的 hook-loadedapk-classloader-plugin.apk 拷贝到 /data/data/files/[package name]
            AssetsUtils.extractAssets(base, "hook-loadedapk-classloader-plugin.apk");
            final File apkFile = getFileStreamPath("hook-loadedapk-classloader-plugin.apk");
            // Hook LoadedApk
            LoadedApkHooker.hookLoadedApkForActivityThread(apkFile, base);
            // Hook PMS
            PMSHooker.hookPackageManagerService(base);
            activityInfoMap = ActivityInfoUtils.preLoadActivities(apkFile, base);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static Context getContext() {
        return BASE;
    }


    public static Map<ComponentName, ActivityInfo> getActivityInfoMap() {
        return activityInfoMap;
    }

}
```

<br>
<br>

# 启动插件

<br>

```java
/**
 * Called when a view has been clicked.
 *
 * @param v The view that was clicked.
 */
@Override
public void onClick(View v) {
    switch (v.getId()) {
        case R.id.start_first_text:
            this.startFirstText.setEnabled(false);
            try {
                this.startActivity(new Intent().setComponent(
                    new ComponentName("com.camnter.hook.loadedapk.classloader.plugin",
                        "com.camnter.hook.loadedapk.classloader.plugin.FirstPluginActivity")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.startFirstText.setEnabled(true);
            break;
        case R.id.start_second_text:
            this.startSecondText.setEnabled(false);
            try {
                this.startActivity(new Intent().setComponent(
                    new ComponentName("com.camnter.hook.loadedapk.classloader.plugin",
                        "com.camnter.hook.loadedapk.classloader.plugin.SecondPluginActivity")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.startSecondText.setEnabled(true);
            break;
    }
}
```

<br>
<br>

# 参考资料

<br>

[Android 插件化原理解析——插件加载机制](http://weishu.me/2016/04/05/understand-plugin-framework-classloader/)  

<br>
<br>