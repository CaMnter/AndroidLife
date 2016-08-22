package com.camnter.newlife;

import android.app.Application;
import android.content.Context;
import com.camnter.hotfix.HotFix;
import com.camnter.newlife.utils.AssetsUtils;
import java.io.File;

/**
 * Description：MainApplication
 * Created by：CaMnter
 * Time：2015-10-19 13:49
 */
public class MainApplication extends Application {

    public static MainApplication instance;


    public static MainApplication getInstance() {
        return instance;
    }


    @Override public void onCreate() {
        instance = this;
        super.onCreate();
        this.insertPile();
        this.insertPatch();
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
