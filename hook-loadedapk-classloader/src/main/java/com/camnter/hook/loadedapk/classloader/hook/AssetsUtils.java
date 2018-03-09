package com.camnter.hook.loadedapk.classloader.hook;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author CaMnter
 */

public class AssetsUtils {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void copyAssets(Context context, String assetsName, String destFilePath)
        throws IOException {
        final File file = new File(destFilePath);
        if (!file.getParentFile().exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            file.delete();
        }
        InputStream in = null;
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(file);
            in = context.getAssets().open(assetsName);

            final byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSilently(in);
            closeSilently(out);
        }
    }


    /**
     * 把 Assets 里面得文件复制到 /data/data/files 目录下
     *
     * @param context context
     * @param sourceName sourceName
     */
    public static void extractAssets(@NonNull final Context context,
                                     @NonNull final String sourceName) {
        AssetManager assetManager = context.getAssets();
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = assetManager.open(sourceName);
            File extractFile = context.getFileStreamPath(sourceName);
            fos = new FileOutputStream(extractFile);
            byte[] buffer = new byte[1024];
            int count;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSilently(is);
            closeSilently(fos);
        }

    }


    private static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Throwable e) {
            // ignore
        }
    }


    private static File BASE_DIR;


    /**
     * 待加载插件经过 opt 优化之后
     * 存放 odex 得路径
     *
     * @param context context
     * @param packageName packageName
     * @return File
     */
    public static File getPluginOptDexDir(@NonNull final Context context,
                                          @NonNull final String packageName) {
        return enforceDirExists(new File(getPluginBaseDir(context, packageName), "odex"));
    }


    /**
     * 插件的 lib 库路径
     *
     * @param context context
     * @param packageName packageName
     * @return File
     */
    public static File getPluginLibDir(@NonNull final Context context,
                                       @NonNull final String packageName) {
        return enforceDirExists(new File(getPluginBaseDir(context, packageName), "lib"));
    }


    /**
     * 获取 /data/data/<package>/files/plugin/ 目录
     *
     * 传入进来的 packageName 是 ApplicationInfo # packageName
     * /data/data/<package>/files/
     *
     * @param context context
     * @param packageName packageName
     * @return File
     */
    private static File getPluginBaseDir(@NonNull final Context context, @NonNull final String packageName) {
        if (BASE_DIR == null) {
            BASE_DIR = context.getFileStreamPath("plugin");
            enforceDirExists(BASE_DIR);
        }
        return enforceDirExists(new File(BASE_DIR, packageName));
    }


    /**
     * 校验 base dir 目录
     *
     * @param baseDir baseDir
     * @return File
     */
    private static synchronized File enforceDirExists(@NonNull final File baseDir) {
        if (!baseDir.exists()) {
            boolean success = baseDir.mkdir();
            if (!success) {
                throw new RuntimeException("[AssetsUtils]   create dir " + baseDir + "failed");
            }
        }
        return baseDir;
    }

}
