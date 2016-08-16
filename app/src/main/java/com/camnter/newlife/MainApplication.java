package com.camnter.newlife;

import android.app.Application;
import android.os.Environment;
import com.camnter.hotpatch.HotPatch;

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
        HotPatch.init(this);
        // 获取补丁，如果存在就执行注入操作
        String dexPath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/patch_dex.jar");
        HotPatch.inject(dexPath);
    }
}
