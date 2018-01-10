# load-service-plugin-plugin

<br>
<br>

## set plugin apk

**load-service-plugin.apk** add to **assets** of **load-plugin-resource-host module**.
 
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
        this.injectAboveEqualApiLevel14();
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

            // assets 的 load-service-plugin.apk 拷贝到 /storage/sdcard0/Android/data/[package name]/cache
            // 或者  /storage/sdcard0/Android/data/[package name]/files
            final File dexPath = new File(dir + File.separator + "load-service-plugin.apk");
            final String dexAbsolutePath = dexPath.getAbsolutePath();
            AssetsUtils.copyAssets(this, "load-service-plugin.apk",
                dexAbsolutePath);

            // /data/data/[package name]/app_single-resources-plugin
            final File optimizedDirectory = this.getDir("load-service-plugin",
                MODE_PRIVATE);

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


    private synchronized void injectAboveEqualApiLevel14() {
        final PathClassLoader pathClassLoader
            = (PathClassLoader) SmartApplication.class.getClassLoader();
        try {
            // 插桩
            final Object dexElements = combineArray(
                getDexElements(getPathList(pathClassLoader)),
                getDexElements(getPathList(this.dexClassLoader))
            );
            final Object pathList = getPathList(pathClassLoader);
            setField(pathList, pathList.getClass(), "dexElements", dexElements);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private static Object getPathList(@NonNull  final Object baseDexClassLoader)
        throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
               ClassNotFoundException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"),
            "pathList");
    }


    private static Object getDexElements(@NonNull  final Object paramObject)
        throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");
    }


    private static Object getField(@NonNull final Object object,
                                   @NonNull final Class<?> classloader,
                                   @NonNull final String field)
        throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Field localField = classloader.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(object);
    }


    @SuppressWarnings("SameParameterValue")
    private static void setField(@NonNull final Object object,
                                 @NonNull  final Class<?> classloader,
                                 @NonNull final String field,
                                 final Object value)
        throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Field localField = classloader.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(object, value);
    }


    private static Object combineArray(@NonNull final Object arrayLhs,
                                       @NonNull final Object arrayRhs) {
        final Class<?> localClass = arrayLhs.getClass().getComponentType();
        final int i = Array.getLength(arrayLhs);
        final int j = i + Array.getLength(arrayRhs);
        final Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }


    public DexClassLoader getDexClassLoader() {
        return this.dexClassLoader;
    }

}
```

<br>

## Host activity

<br>

**LoadServicePluginHostActivity**

```java
/**
 * @author CaMnter
 */

public class LoadServicePluginHostActivity extends BaseAppCompatActivity {

    private static final int ACTIVITY_MESSAGE = 0x2331;
    private static final int PLUGIN_SERVICE_MESSAGE = 0x2332;

    private Messenger serviceMessenger;
    private Messenger activityMessenger;
    private ServiceConnection connection;

    View startText;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


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
                requestUploadService();
            }
        });
    }


    private void requestUploadService() {
        try {
            if (this.connection == null) {
                this.initCallbackMessenger();
                this.connection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        serviceMessenger = new Messenger(service);
                        try {
                            serviceMessenger.send(createRequestMessage());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }


                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        connection = null;
                    }
                };
                final Intent intent = new Intent(this,
                    Class.forName("com.camnter.load.service.plugin.plugin.PluginService"));
                intent.setAction("com.camnter.load.service.plugin");
                this.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
            } else {
                try {
                    serviceMessenger.send(this.createRequestMessage());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Message createRequestMessage() {
        Message message = Message.obtain(null, ACTIVITY_MESSAGE);
        final Bundle bundle = new Bundle();
        bundle.putString("ACTIVITY_INFO", "There is Host Activity");

        message.setData(bundle);
        message.replyTo = this.activityMessenger;
        return message;
    }


    private void initCallbackMessenger() {
        this.activityMessenger = new Messenger(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                startText.setEnabled(true);
                final Bundle bundle = msg.getData();
                final String info = bundle == null ? null : bundle.getString("SERVICE_INFO");
                switch (msg.what) {
                    case PLUGIN_SERVICE_MESSAGE:
                        ToastUtils.show(LoadServicePluginHostActivity.this, String.valueOf(info),
                            Toast.LENGTH_LONG);
                        break;
                }
            }
        });

    }


    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override
    protected void initData() {

    }


    @Override
    protected void onDestroy() {
        if (this.connection != null) {
            this.unbindService(this.connection);
        }
        super.onDestroy();
    }

}
```

