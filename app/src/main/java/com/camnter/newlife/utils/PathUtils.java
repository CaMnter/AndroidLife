package com.camnter.newlife.utils;

import android.content.Context;
import android.os.Environment;
import java.io.File;

public class PathUtils {
    public static String pathPrefix;
    public static final String historyPathName = "/chat/";
    public static final String imagePathName = "/image/";
    public static final String voicePathName = "/voice/";
    public static final String filePathName = "/file/";
    public static final String videoPathName = "/video/";
    public static final String netdiskDownloadPathName = "/netdisk/";
    public static final String meetingPathName = "/meeting/";
    private static File storageDir = null;
    private static PathUtils instance = null;
    private File voicePath = null;
    private File imagePath = null;
    private File historyPath = null;
    private File videoPath = null;
    private File filePath;


    private PathUtils() {
    }


    public static PathUtils getInstance() {
        if (instance == null) {
            instance = new PathUtils();
        }

        return instance;
    }


    public void initDirs(String var1, String var2, Context var3) {
        String var4 = var3.getPackageName();
        pathPrefix = "/Android/data/" + var4 + "/";
        this.voicePath = generateVoicePath(var1, var2, var3);
        if (!this.voicePath.exists()) {
            this.voicePath.mkdirs();
        }

        this.imagePath = generateImagePath(var1, var2, var3);
        if (!this.imagePath.exists()) {
            this.imagePath.mkdirs();
        }

        this.historyPath = generateHistoryPath(var1, var2, var3);
        if (!this.historyPath.exists()) {
            this.historyPath.mkdirs();
        }

        this.videoPath = generateVideoPath(var1, var2, var3);
        if (!this.videoPath.exists()) {
            this.videoPath.mkdirs();
        }

        this.filePath = generateFiePath(var1, var2, var3);
        if (!this.filePath.exists()) {
            this.filePath.mkdirs();
        }
    }


    public File getImagePath() {
        return this.imagePath;
    }


    public File getVoicePath() {
        return this.voicePath;
    }


    public File getFilePath() {
        return this.filePath;
    }


    public File getVideoPath() {
        return this.videoPath;
    }


    public File getHistoryPath() {
        return this.historyPath;
    }


    private static File getStorageDir(Context context) {
        if (storageDir == null) {
            File file = Environment.getExternalStorageDirectory();
            if (file.exists()) {
                return file;
            }

            storageDir = context.getFilesDir();
        }

        return storageDir;
    }


    private static File generateImagePath(String var0, String var1, Context context) {
        String var3 = null;
        if (var0 == null) {
            var3 = pathPrefix + var1 + "/image/";
        } else {
            var3 = pathPrefix + var0 + "/" + var1 + "/image/";
        }

        return new File(getStorageDir(context), var3);
    }


    private static File generateVoicePath(String var0, String var1, Context context) {
        String var3 = null;
        if (var0 == null) {
            var3 = pathPrefix + var1 + "/voice/";
        } else {
            var3 = pathPrefix + var0 + "/" + var1 + "/voice/";
        }

        return new File(getStorageDir(context), var3);
    }


    private static File generateFiePath(String var0, String var1, Context context) {
        String var3 = null;
        if (var0 == null) {
            var3 = pathPrefix + var1 + "/file/";
        } else {
            var3 = pathPrefix + var0 + "/" + var1 + "/file/";
        }

        return new File(getStorageDir(context), var3);
    }


    private static File generateVideoPath(String var0, String var1, Context context) {
        String var3 = null;
        if (var0 == null) {
            var3 = pathPrefix + var1 + "/video/";
        } else {
            var3 = pathPrefix + var0 + "/" + var1 + "/video/";
        }

        return new File(getStorageDir(context), var3);
    }


    private static File generateHistoryPath(String var0, String var1, Context context) {
        String name = null;
        if (var0 == null) {
            name = pathPrefix + var1 + "/chat/";
        } else {
            name = pathPrefix + var0 + "/" + var1 + "/chat/";
        }

        return new File(getStorageDir(context), name);
    }


    private static File generateMessagePath(String var0, String var1, Context context) {
        File file = new File(generateHistoryPath(var0, var1, context),
                var1 + File.separator + "Msg.db");
        return file;
    }


    public static File getTempPath(File file) {
        File tempFile = new File(file.getAbsoluteFile() + ".tmp");
        return tempFile;
    }
}