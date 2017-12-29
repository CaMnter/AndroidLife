package com.camnter.load.plugin.resources.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public class PluginActivity extends AppCompatActivity {

    private Resources resources;


    /**
     * ---------------------------------------------------------------------------------------------
     *
     * 有两种选择：
     *
     * - 拿 Plugin Resource 直接加载资源，但是无法加载 layout
     * - Hook ContextImpl 中的 mResource. 然后下面可以加载 layout
     *
     * ---------------------------------------------------------------------------------------------
     *
     * 自定义 View 或者 Fragment：
     *
     * 需要 持有一个 Inflate
     *
     * final View selfView = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
     * this.setContentView(selfView);
     *
     * ---------------------------------------------------------------------------------------------
     *
     * @param savedInstanceState Bundle
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        try {
            final Field field = newBase.getClass().getDeclaredField("mResources");
            field.setAccessible(true);
            if (this.resources == null) {
                this.resources = this.getPluginResources(newBase);
            }
            field.set(newBase, this.resources);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.attachBaseContext(newBase);
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

            // assets 的 load-plugin-resource-plugin.apk 拷贝到 /storage/sdcard0/Android/data/[package name]/cache
            // 或者  /storage/sdcard0/Android/data/[package name]/files
            final File dexPath = new File(dir + File.separator + "load-plugin-resource-plugin.apk");

            if (!dexPath.exists()) {
                AssetsUtils.copyAssets(newBase, "load-plugin-resource-plugin.apk",
                    dexPath.getAbsolutePath());
            }

            final AssetManager assetManager = AssetManager.class.newInstance();
            final Method addAssetPath = assetManager.getClass()
                .getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, dexPath.getAbsolutePath());

            /**
             * 如果要 独立使用 Resources，而不 Hook mResources :
             *
             * final Resources originResources = super.getResources();
             * final Resources pluginResources = new Resources(assetManager, originResources.getDisplayMetrics(),
             * originResources.getConfiguration());
             */
            return new Resources(
                assetManager,
                newBase.getResources().getDisplayMetrics(),
                newBase.getResources().getConfiguration()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
