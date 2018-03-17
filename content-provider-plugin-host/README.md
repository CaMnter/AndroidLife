# content-provider-plugin-host

<br>
<br>

## set plugin apk

**content-provider-plugin-plugin.apk** add to **assets** of **content-provider-plugin-host module**.

<br>

## provider plugin

```java
/**
 * @author CaMnter
 */

public class SmartApplication extends Application {

    final Map<ComponentName, ProviderInfo> providerInfoMap = new HashMap<>();


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
        try {
            // assets 的 content-provider-plugin-plugin.apk 拷贝到 /data/data/files/[package name]
            AssetsUtils.extractAssets(base, "content-provider-plugin-plugin.apk");
            final File apkFile = getFileStreamPath("content-provider-plugin-plugin.apk");
            final File odexFile = getFileStreamPath("content-provider-plugin-plugin.odex");
            // Hook ClassLoader, 让插件中的类能够被成功加载
            BaseDexClassLoaderHooker.patchClassLoader(this.getClassLoader(), apkFile, odexFile);
            // 解析 provider
            providerInfoMap.putAll(ProviderInfoUtils.getProviderInfos(apkFile, base));
            // 该进程安装 ContentProvider
            this.installProviders(base, providerInfoMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 反射 调用 ActivityThread # installContentProviders(Context context, List<ProviderInfo> providers)
     * 安装 ContentProvider
     *
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    private void installProviders(@NonNull final Context context,
                                  @NonNull final Map<ComponentName, ProviderInfo> providerInfoMap)
        throws NoSuchMethodException,
               ClassNotFoundException,
               IllegalAccessException,
               InvocationTargetException {

        List<ProviderInfo> providerInfos = new ArrayList<>();

        // 修改 ProviderInfo # String packageName
        for (Map.Entry<ComponentName, ProviderInfo> entry : providerInfoMap.entrySet()) {
            final ProviderInfo providerInfo = entry.getValue();
            providerInfo.applicationInfo.packageName = context.getPackageName();
            providerInfos.add(providerInfo);
        }

        if (providerInfos.isEmpty()) {
            return;
        }

        final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        final Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod(
            "currentActivityThread");
        final Object currentActivityThread = currentActivityThreadMethod.invoke(null);
        final Method installProvidersMethod = activityThreadClass.getDeclaredMethod(
            "installContentProviders", Context.class, List.class);

        installProvidersMethod.setAccessible(true);
        installProvidersMethod.invoke(currentActivityThread, context, providerInfos);
    }

}
```

<br>

# use

```java
/**
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public class ContentProviderPluginActivity extends BaseAppCompatActivity
    implements View.OnClickListener {

    private int count = 0;
    private Uri pluginUri;
    private ContentResolver resolver;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    @Override
    protected void initViews(Bundle savedInstanceState) {
        this.findView(R.id.query_plugin_content_provider).setOnClickListener(this);
        this.findView(R.id.insert_plugin_content_provider).setOnClickListener(this);
        this.findView(R.id.delete_plugin_content_provider).setOnClickListener(this);

        /**
         * 插件 uri
         * content://com.camnter.content.provider.plugin.plugin.PluginContentProvider
         *
         * 实际 uri 得写成
         * content://com.camnter.content.provider.plugin.host.StubContentProvider/com.camnter.content.provider.plugin.plugin.PluginContentProvider
         */
        this.pluginUri = Uri.parse(StubContentProvider.STUB_URI.toString() + '/' +
            "com.camnter.content.provider.plugin.plugin.PluginContentProvider");
        this.resolver = this.getContentResolver();
    }


    @Override
    protected void initListeners() {

    }


    @Override
    protected void initData() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.query_plugin_content_provider:
                final Cursor result = this.resolver.query(this.pluginUri, null, null, null, null);
                if (result == null) return;
                final StringBuilder stringBuilder = new StringBuilder();
                for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
                    final int id = result.getInt(result.getColumnIndex("_id"));
                    final String content = result.getString(result.getColumnIndex("content"));
                    stringBuilder.append("_id = ")
                        .append(id)
                        .append(", content = ")
                        .append(content)
                        .append(";");
                }
                result.close();
                if (stringBuilder.length() > 0) {
                    final String info = stringBuilder.substring(0, stringBuilder.length() - 1);
                    ToastUtils.show(this, info, Toast.LENGTH_LONG);
                }
                break;
            case R.id.insert_plugin_content_provider:
                final ContentValues values = new ContentValues();
                values.put("content", "Save you from anything - " + ++this.count);
                this.resolver.insert(this.pluginUri, values);
                ToastUtils.show(this, "insert successfully", Toast.LENGTH_LONG);
                break;
            case R.id.delete_plugin_content_provider:
                this.resolver.delete(this.pluginUri, null, null);
                ToastUtils.show(this, "delete successfully", Toast.LENGTH_LONG);
                break;
        }
    }

}
```

