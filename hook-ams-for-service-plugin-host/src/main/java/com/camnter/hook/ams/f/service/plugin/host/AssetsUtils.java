package com.camnter.hook.ams.f.service.plugin.host;

import android.content.Context;
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
