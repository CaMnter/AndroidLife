package com.camnter.hotpatch;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Description：AssetsUtils
 * Created by：CaMnter
 */

public class AssetsUtils {

    public static void copyAssets(Context context, String assetsName, String destFilePath)
        throws IOException {
        File file = new File(destFilePath);
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
