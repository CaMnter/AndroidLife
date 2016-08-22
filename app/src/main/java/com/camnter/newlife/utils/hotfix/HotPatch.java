package com.camnter.newlife.utils.hotfix;

import android.content.Context;
import android.util.Log;
import com.camnter.newlife.utils.AssetsUtils;
import com.camnter.newlife.utils.ReflectionUtils;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.io.IOException;

/**
 * Description：HotPatch
 * Created by：CaMnter
 */

public class HotPatch {

    public static void init(Context context) {
        File hackDir = context.getDir("hackDir", 0);
        File hackJar = new File(hackDir, "hack.jar");
        try {
            AssetsUtils.copyAssets(context, "hack.jar", hackJar.getAbsolutePath());
            inject(hackJar.getAbsolutePath(), context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void inject(String path, Context context) {
        File file = new File(path);
        if (file.exists()) {
            try {
                // 获取 classes 的 dexElements
                Class<?> cl = Class.forName("dalvik.system.BaseDexClassLoader");
                Object pathList = ReflectionUtils.getField(cl, "pathList",
                    context.getClassLoader());
                Object baseElements = ReflectionUtils.getField(pathList.getClass(), "dexElements",
                    pathList);

                // 获取 patch_dex 的 dexElements（需要先加载dex）
                String dexopt = context.getDir("dexopt", 0).getAbsolutePath();
                DexClassLoader dexClassLoader = new DexClassLoader(path, dexopt, dexopt,
                    context.getClassLoader());
                Object obj = ReflectionUtils.getField(cl, "pathList", dexClassLoader);
                Object dexElements = ReflectionUtils.getField(obj.getClass(), "dexElements", obj);

                // 合并两个 Elements
                Object combineElements = ReflectionUtils.combineArray(dexElements, baseElements);

                // 将合并后的 Element 数组重新赋值给 app 的 classLoader
                ReflectionUtils.setField(pathList.getClass(), "dexElements", pathList,
                    combineElements);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("HotPatch", file.getAbsolutePath() + "does not exists");
        }
    }
}
