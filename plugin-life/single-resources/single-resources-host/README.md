# single-resources-host

<br>
<br>

## hook mInstrumentation of ActivityThread
 
<br>
    
```java
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

        // assets 的 single-resources-plugin.apk 拷贝到 /storage/sdcard0/Android/data/[package name]/cache
        // 或者  /storage/sdcard0/Android/data/[package name]/files
        final File dexPath = new File(dir + File.separator + "single-resources-plugin.apk");
        final String dexAbsolutePath = dexPath.getAbsolutePath();
        AssetsUtils.copyAssets(this, "single-resources-plugin.apk",
            dexAbsolutePath);

        // /data/data/[package name]/app_single-resources-plugin
        final File optimizedDirectory = this.getDir("single-resources-plugin",
            MODE_PRIVATE);

        this.dexClassLoader = new DexClassLoader(
            dexPath.getAbsolutePath(),
            optimizedDirectory.getAbsolutePath(),
            null,
            this.getClassLoader()
        );

        this.dexAbsolutePath = dexAbsolutePath;
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
```

<br>

## hook mResources of ContextImpl

```java
@Override
protected void attachBaseContext(Context newBase) {
    this.hookResources(newBase);
    super.attachBaseContext(newBase);
}


private void hookResources(@NonNull final Context newBase) {
    try {
        final Field field = newBase.getClass().getDeclaredField("mResources");
        field.setAccessible(true);
        if (this.singleResources == null) {
            this.singleResources = this.getPluginResources(newBase);
        }
        field.set(newBase, this.singleResources);
    } catch (Exception e) {
        e.printStackTrace();
    }
}


/**
 * 获取 插件 Resources
 *
 * @param newBase Context
 * @return Resources
 */
private Resources getPluginResources(final Context newBase) {
    try {
        String dir = null;
        final File cacheDir = newBase.getExternalCacheDir();
        final File filesDir = newBase.getExternalFilesDir("");
        if (cacheDir != null) {
            dir = cacheDir.getAbsolutePath();
        } else {
            if (filesDir != null) {
                dir = filesDir.getAbsolutePath();
            }
        }
        if (TextUtils.isEmpty(dir)) return null;

        // assets 的 single-resources-plugin.apk 拷贝到 /storage/sdcard0/Android/data/[package name]/cache
        // 或者  /storage/sdcard0/Android/data/[package name]/files
        final File dexPath = new File(dir + File.separator + "single-resources-plugin.apk");

        if (!dexPath.exists()) {
            AssetsUtils.copyAssets(newBase, "single-resources-plugin.apk",
                dexPath.getAbsolutePath());
        }

        final AssetManager assetManager = AssetManager.class.newInstance();
        @SuppressWarnings("RedundantArrayCreation") final Method addAssetPath
            = assetManager.getClass()
            .getMethod("addAssetPaths", new Class[] { String[].class });

        final String[] paths = new String[2];
        // 插件 Asset
        paths[0] = dexPath.getAbsolutePath();
        // 宿主 Asset
        paths[1] = newBase.getPackageResourcePath();
        addAssetPath.invoke(assetManager, new Object[] { paths });

        final Resources originalResources = newBase.getResources();
        return new Resources(
            assetManager,
            originalResources.getDisplayMetrics(),
            originalResources.getConfiguration()
        );
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
```

<br>

## load plugin activity
 
<br>
    
```java
/**
 * Initialize the view in the layout
 *
 * @param savedInstanceState savedInstanceState
 */
@Override
protected void initViews(Bundle savedInstanceState) {
    this.startText = this.findViewById(R.id.start_text);
    this.startText.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startText.setEnabled(false);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        // plugin activity
                        final SmartApplication smartApplication
                            = (SmartApplication) SingleResourcesActivity.this.getApplication();
                        final Intent intent = new Intent(SingleResourcesActivity.this,
                            smartApplication
                                .getDexClassLoader()
                                .loadClass(
                                    "com.camnter.single.resources.plugin.SingleResourcesPluginActivity"));
                        startActivity(intent);
                    } catch (Exception e) {
                        ToastUtils.show(SingleResourcesActivity.this, e.getMessage(),
                            Toast.LENGTH_LONG);
                        e.printStackTrace();
                    } finally {
                        startText.setEnabled(true);
                    }
                }
            });
        }
    });
}
```

<br>