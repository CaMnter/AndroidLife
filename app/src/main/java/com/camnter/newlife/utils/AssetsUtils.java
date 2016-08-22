package com.camnter.newlife.utils;

import android.content.Context;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Description：AssetsUtils
 * Created by：CaMnter
 */

public class AssetsUtils {

    private static final int BUF_SIZE = 2048;


    /**
     * 从 assets 拷贝 patch_dex.jar 到 dexInternalStoragePath 路径
     *
     * @param context context
     * @param dexInternalStoragePath dexInternalStoragePath 拷贝的目标路径
     * @param dexFile dexFile
     * @return 是否成功
     */
    public static boolean prepareDex(Context context, File dexInternalStoragePath, String dexFile) {
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;

        try {
            bis = new BufferedInputStream(context.getAssets().open(dexFile));
            dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while ((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();
            return true;
        } catch (IOException e) {
            if (dexWriter != null) {
                try {
                    dexWriter.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            return false;
        }
    }


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
