package com.camnter.newlife.utils.hotfix;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import com.camnter.hotpatch.HotPatch;
import dalvik.system.DexClassLoader;
import java.lang.reflect.Array;

import static com.camnter.newlife.utils.ReflectionUtils.combineArray;
import static com.camnter.newlife.utils.ReflectionUtils.getField;
import static com.camnter.newlife.utils.ReflectionUtils.setField;

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
        String dexPath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/patch_dex.jar");
        HotPatch.inject(dexPath);
        // String dexPath = Environment.getExternalStorageDirectory()
        //     .getAbsolutePath()
        //     .concat("/patch_dex.jar");
        // File file = new File(dexPath);
        // if (file.exists()) {
        //     this.inject(dexPath);
        // } else {
        //     Log.e("", "Dex patch 不存在");
        // }
    }


    private void inject(String path) {

        try {
            // 获取 classes 的 dexElements
            Class<?> clazz = Class.forName("dalvik.system.BaseDexClassLoader");
            Object pathList = getField(clazz, "pathList", getClassLoader());
            Object baseElements = getField(pathList.getClass(), "dexElements", pathList);

            // 获取 patch_dex 的 dexElements（ 需要先加载 dex ）
            String dexopt = getDir("dexopt", 0).getAbsolutePath();
            DexClassLoader dexClassLoader = new DexClassLoader(path, dexopt, dexopt,
                getClassLoader());
            Object obj = getField(clazz, "pathList", dexClassLoader);
            Object dexElements = getField(obj.getClass(), "dexElements", obj);

            // 合并两个 Elements
            Object combineElements = combineArray(dexElements, baseElements);

            // 将合并后的 Element 数组重新赋值给 app 的 classLoader
            setField(pathList.getClass(), "dexElements", pathList, combineElements);

            // 测试注入
            Object object = getField(pathList.getClass(), "dexElements", pathList);
            int length = Array.getLength(object);
            Log.e("HotPatchApplication", "length = " + length);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

}
