package com.camnter.utils.hotfix;

import android.app.Application;
import android.os.Environment;

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

    @Override public void onCreate() {
        super.onCreate();
        HotPatch.init(this);
        // 获取补丁，如果存在就执行注入操作
        String dexPath = Environment.getExternalStorageDirectory()
            .getAbsolutePath()
            .concat("/patch_dex.jar");
        HotPatch.inject(dexPath, this);
    }

}
