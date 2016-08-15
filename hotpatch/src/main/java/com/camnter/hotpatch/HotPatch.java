package com.camnter.hotpatch;

import android.content.Context;
import android.util.Log;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.io.IOException;

/**
 * Description：HotPatch
 * Created by：CaMnter
 */

public class HotPatch {

    private static Context mContext;


    public static void init(Context context) {
        mContext = context;
        File hackDir = context.getDir("hackDir", 0);
        File hackJar = new File(hackDir, "hack.jar");
        try {
            AssetsUtils.copyAssets(context, "hack.jar", hackJar.getAbsolutePath());
            inject(hackJar.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void inject(String path) {
        File file = new File(path);
        if (file.exists()) {
            try {
                // 获取 classes 的 dexElements
                Class<?> cl = Class.forName("dalvik.system.BaseDexClassLoader");
                Object pathList = ReflectUtils.getField(cl, "pathList", mContext.getClassLoader());
                Object baseElements = ReflectUtils.getField(pathList.getClass(), "dexElements",
                    pathList);

                // 获取 patch_dex 的 dexElements（需要先加载dex）
                String dexopt = mContext.getDir("dexopt", 0).getAbsolutePath();
                DexClassLoader dexClassLoader = new DexClassLoader(path, dexopt, dexopt,
                    mContext.getClassLoader());
                Object obj = ReflectUtils.getField(cl, "pathList", dexClassLoader);
                Object dexElements = ReflectUtils.getField(obj.getClass(), "dexElements", obj);

                // 合并两个 Elements
                Object combineElements = ReflectUtils.combineArray(dexElements, baseElements);

                // 将合并后的 Element 数组重新赋值给 app 的 classLoader
                ReflectUtils.setField(pathList.getClass(), "dexElements", pathList,
                    combineElements);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("HotPatch", file.getAbsolutePath() + "does not exists");
        }
    }
}
