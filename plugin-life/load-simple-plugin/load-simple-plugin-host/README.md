# load-simple-plugin-host

## load dex

set .dex into assets.


```java
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

    // assets 的 simple-plugin.dex 拷贝到 /storage/sdcard0/Android/data/[package name]/cache
    // 或者  /storage/sdcard0/Android/data/[package name]/files
    final File dexPath = new File(dir + File.separator + "simple-plugin.dex");
    AssetsUtils.copyAssets(this, "simple-plugin.dex", dexPath.getAbsolutePath());

    // /data/data/[package name]/app_simple-plugin
    final File optimizedDirectory = this.getDir("simple-plugin", MODE_PRIVATE);

    final DexClassLoader dexClassLoader = new DexClassLoader(
        dexPath.getAbsolutePath(),
        optimizedDirectory.getAbsolutePath(),
        null,
        this.getClassLoader()
    );

    final Class clazz = dexClassLoader.loadClass(
        "com.camnter.load.simple.plugin.LoadSimplePlugin");
    final LoadSimplePluginInterface pluginInterface
        = (LoadSimplePluginInterface) clazz.newInstance();

    final TextView textView = this.findView(R.id.text);
    textView.setText(pluginInterface.getInfo());
} catch (Exception e) {
    e.printStackTrace();
}
```
