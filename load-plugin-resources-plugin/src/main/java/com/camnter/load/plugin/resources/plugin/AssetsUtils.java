package com.camnter.load.plugin.resources.plugin;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author CaMnter
 */

public class AssetsUtils {

    private static final int BUF_SIZE = 2048;


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void copyAssets(Context context, String assetsName, String destFilePath)
        throws IOException {
        File file = new File(destFilePath);
        if (!file.getParentFile().exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out = new FileOutputStream(file);

        InputStream in = context.getAssets().open(assetsName);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

}
