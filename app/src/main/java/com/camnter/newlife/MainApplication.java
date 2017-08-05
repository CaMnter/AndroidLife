package com.camnter.newlife;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import com.camnter.newlife.ui.activity.smartrouter.CustomRouterActivity;
import com.camnter.smartrouter.SmartRouters;
import com.camnter.smartrouter.core.Router;
import com.camnter.utils.AssetsUtils;
import dodola.hotfix.HotFix;
import java.io.File;
import java.util.Map;

/**
 * Description：MainApplication
 * Created by：CaMnter
 * Time：2015-10-19 13:49
 */
public class MainApplication extends Application {

    private static final String SCHEME = "smart-routers";

    public static MainApplication instance;


    public static MainApplication getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        this.initRouter();
        this.insertPile();
        this.insertPatch();
    }


    private void initRouter() {
        SmartRouters.running(SCHEME);
        SmartRouters.register(new Router<CustomRouterActivity>() {

            @Override
            public void register(@NonNull Map<String, Class<? extends Activity>> routerMapping) {
                routerMapping.put(SmartRouters.getScheme() + "://" + "router-0x02",
                    CustomRouterActivity.class);
            }


            @Override
            public void setFieldValue(@NonNull CustomRouterActivity activity) {
                final Intent intent = activity.getIntent();
                final Uri uri = intent.getData();
                if (uri == null) return;

                activity.exampleBoxedString = uri.getQueryParameter("boxedString");
            }

        });
    }


    public static String getScheme() {
        return SCHEME;
    }


    /**
     * 插桩
     */
    private void insertPile() {
        File dexPath = new File(getDir("dex", Context.MODE_PRIVATE), "hack.jar");
        AssetsUtils.prepareDex(this.getApplicationContext(), dexPath, "hack.jar");
        HotFix.patch(this, dexPath.getAbsolutePath(), "com.camnter.hack.AntilazyLoad");
        try {
            this.getClassLoader().loadClass("com.camnter.hack.AntilazyLoad");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 打补丁
     */
    private void insertPatch() {
        //准备补丁,从 assert 里拷贝到 dex 里
        File dexPath = new File(getDir("dex", Context.MODE_PRIVATE), "patch_dex.jar");
        AssetsUtils.prepareDex(this.getApplicationContext(), dexPath, "patch_dex.jar");
        HotFix.patch(this, dexPath.getAbsolutePath(),
            "com.camnter.newlife.ui.activity.hotfix.FixCall");
    }

}
