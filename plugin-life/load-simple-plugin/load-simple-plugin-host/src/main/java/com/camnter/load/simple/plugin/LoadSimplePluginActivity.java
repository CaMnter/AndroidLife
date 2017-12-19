package com.camnter.load.simple.plugin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import dalvik.system.DexClassLoader;
import java.io.File;

/**
 * @author CaMnter
 */

public class LoadSimplePluginActivity extends BaseAppCompatActivity {

    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.load_simple_plugin;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
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

}
