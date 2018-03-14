# broadcast-receiver-plugin-host

<br>
<br>

## set plugin apk

**broadcast-receiver-plugin-plugin.apk** add to **assets** of **broadcast-receiver-plugin-host module**.

<br>

## receiver plugin

```java
/**
 * @author CaMnter
 */

public class SmartApplication extends Application {

    private static final Map<ActivityInfo, List<? extends IntentFilter>> receiverInfoMap
        = new HashMap<>();


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
            // assets 的 broadcast-receiver-plugin-plugin.apk 拷贝到 /data/data/files/[package name]
            AssetsUtils.extractAssets(base, "broadcast-receiver-plugin-plugin.apk");
            final File apkFile = getFileStreamPath("broadcast-receiver-plugin-plugin.apk");
            final File odexFile = getFileStreamPath("broadcast-receiver-plugin-plugin.odex");
            // Hook ClassLoader, 让插件中的类能够被成功加载
            BaseDexClassLoaderHooker.patchClassLoader(this.getClassLoader(), apkFile, odexFile);
            receiverInfoMap.putAll(ReceiverInfoUtils.getReceiverInfos(apkFile, base));
            // 插件广播注册成 动态广播
            this.registerPluginBroadcast(base, receiverInfoMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 注册 插件 Broadcast
     *
     * @param context context
     * @param receiverInfoMap receiverInfoMap
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InstantiationException InstantiationException
     */
    private void registerPluginBroadcast(@NonNull final Context context,
                                         @NonNull final Map<ActivityInfo, List<? extends IntentFilter>> receiverInfoMap)
        throws ClassNotFoundException,
               IllegalAccessException,
               InstantiationException {
        for (Map.Entry<ActivityInfo, List<? extends IntentFilter>> entry : receiverInfoMap.entrySet()) {
            final ActivityInfo activityInfo = entry.getKey();
            final List<? extends IntentFilter> intentFilters = entry.getValue();

            final ClassLoader classLoader = this.getClassLoader();
            for (IntentFilter intentFilter : intentFilters) {
                final BroadcastReceiver receiver = (BroadcastReceiver) classLoader.loadClass(
                    activityInfo.name).newInstance();
                context.registerReceiver(receiver, intentFilter);
            }
        }
    }

}
```

<br>

# use

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
                final Intent intent =
                    new Intent("com.camnter.broadcast.receiver.plugin.plugin.FirstReceiver");
                intent.putExtra("message", UUID.randomUUID().toString());
                this.sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.startFirstText.setEnabled(true);
            break;
        case R.id.start_second_text:
            this.startSecondText.setEnabled(false);
            try {
                final Intent intent =
                    new Intent("com.camnter.broadcast.receiver.plugin.plugin.SecondReceiver");
                intent.putExtra("message", UUID.randomUUID().toString());
                this.sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.startSecondText.setEnabled(true);
            break;
    }
}
```

