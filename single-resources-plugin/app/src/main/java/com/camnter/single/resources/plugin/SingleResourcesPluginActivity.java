package com.camnter.single.resources.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public class SingleResourcesPluginActivity extends AppCompatActivity {

    private static final String HOST_PACKAGE_NAME = "com.camnter.single.resources.host";

    private Resources singleResources;


    /**
     * ---------------------------------------------------------------------------------------------
     *
     * // 加载 插件资源
     *
     * this.setContentView(R.layout.single_resources_plugin);
     *
     * ---------------------------------------------------------------------------------------------
     *
     * // 加载 宿主资源
     *
     * final TextView hostText = (TextView) this.findViewById(R.id.host_text);
     * final Resources resources = this.getResources();
     * final String hostInfo = resources.getString(resources.getIdentifier("host_info", "string",
     * HOST_PACKAGE_NAME));
     * final int hostInfoColor = resources.getColor(resources.getIdentifier("host_info_color",
     * "color", HOST_PACKAGE_NAME));
     * hostText.setText(hostInfo);
     * hostText.setTextColor(hostInfoColor);
     *
     * ---------------------------------------------------------------------------------------------
     *
     * @param savedInstanceState Bundle
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 加载 插件资源
        this.setContentView(R.layout.single_resources_plugin);

        // 加载 宿主资源
        final TextView hostText = (TextView) this.findViewById(R.id.host_text);
        final Resources resources = this.getResources();
        final String hostInfo = resources.getString(
            resources.getIdentifier("host_info", "string", HOST_PACKAGE_NAME));
        final int hostInfoColor = resources.getColor(
            resources.getIdentifier("host_info_color", "color", HOST_PACKAGE_NAME));
        hostText.setText(hostInfo);
        hostText.setTextColor(hostInfoColor);
    }


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

}
