package com.camnter.hook.ams.f.service.plugin.host;

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

}
