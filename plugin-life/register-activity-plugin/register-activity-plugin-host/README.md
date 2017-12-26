# register-activity-plugin-host

<br>
<br>

## set plugin apk

**register-activity-plugin.apk** add to **assets** of **register-activity-plugin-host module**.
 
<br>

## HOOK

<br>

**SmartApplication**

```java
/**
 * @author CaMnter
 */

public class SmartApplication extends Application {

    private DexClassLoader dexClassLoader;


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

        this.loadDex();
        this.hookInstrumentation();
    }


    private void loadDex() {
        try {
            String dir = null;
            final File cacheDir = this.getExternalCacheDir();
            final File filesDir = this.getExternalFilesDir("");
            if (cacheDir != null) {
                dir = cacheDir.getAbsolutePath();
            } else {
                if (filesDir != null) {
                    dir = filesDir.getAbsolutePath();
                }
            }
            if (TextUtils.isEmpty(dir)) return;

            // assets 的 register-activity-plugin.apk 拷贝到 /storage/sdcard0/Android/data/[package name]/cache
            // 或者  /storage/sdcard0/Android/data/[package name]/files
            final File dexPath = new File(dir + File.separator + "register-activity-plugin.apk");
            AssetsUtils.copyAssets(this, "register-activity-plugin.apk", dexPath.getAbsolutePath());

            // /data/data/[package name]/app_register-activity-plugin
            final File optimizedDirectory = this.getDir("register-activity-plugin", MODE_PRIVATE);

            this.dexClassLoader = new DexClassLoader(
                dexPath.getAbsolutePath(),
                optimizedDirectory.getAbsolutePath(),
                null,
                this.getClassLoader()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void hookInstrumentation() {
        try {
            /*
             * 反射 ActivityThread
             * 通过 currentActivityThread 方法
             * 获取存放的 ActivityThread 实例
             */
            @SuppressLint("PrivateApi") final Class<?> activityThreadClass = Class.forName(
                "android.app.ActivityThread");
            final Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod(
                "currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            final Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            /*
             * ActivityThread 实例获取 Field mInstrumentation
             */
            final Field mInstrumentationField = activityThreadClass.getDeclaredField(
                "mInstrumentation");
            mInstrumentationField.setAccessible(true);
            final Instrumentation mInstrumentation = (Instrumentation) mInstrumentationField.get(
                currentActivityThread);

            // 是否 hook 过
            if (!(mInstrumentation instanceof SmartInstrumentation)) {
                final SmartInstrumentation pluginInstrumentation = new SmartInstrumentation(
                    dexClassLoader, mInstrumentation);
                mInstrumentationField.set(currentActivityThread, pluginInstrumentation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public DexClassLoader getDexClassLoader() {
        return this.dexClassLoader;
    }

}
```

<br>

**SmartInstrumentation**

```java
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
```

