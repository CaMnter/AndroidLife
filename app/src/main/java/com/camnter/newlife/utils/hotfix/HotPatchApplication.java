package com.camnter.newlife.utils.hotfix;

import android.app.Application;
import android.util.Log;
import java.io.File;

/**
 * Description：HotPatchApplication
 *
 * BaseDexClassLoader >> pathList >> dexElements
 *
 * 1. apk 的 classes.dex 可以从应用本身的 DexClassLoader 中获取。
 * 2. path_dex 的 dex 需要 new 一个 DexClassLoader 加载后再获取。
 * 3. 分别通过反射取出 dex 文件，重新合并成一个数组，然后赋值给盈通本身的 ClassLoader 的 dexElements。
 *
 *
 * Created by：CaMnter
 */

public class HotPatchApplication extends Application {

    public static final String PATCH_PATH = "file:///android_asset/patch_dex.jar";


    @Override public void onCreate() {
        File file = new File(PATCH_PATH);
        if (file.exists()) {
            this.inject(PATCH_PATH);
        } else {
            Log.e("", "Dex patch 不存在");
        }
    }


    private void inject(String path) {

    }

}
