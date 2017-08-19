package com.alibaba.android.arouter.utils;

// Copy from galaxy sdk ${com.alibaba.android.galaxy.utils.ClassUtils}

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.alibaba.android.arouter.launcher.ARouter;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scanner, find out class with any conditions, copy from google source code.
 *
 * @author 正纬 <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/6/27 下午10:58
 */
public class ClassUtils {
    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";

    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator +
        "secondary-dexes";

    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";

    private static final int VM_WITH_MULTIDEX_VERSION_MAJOR = 2;
    private static final int VM_WITH_MULTIDEX_VERSION_MINOR = 1;


    /**
     * 获取 MultiDex 的 SharedPreferences
     *
     * 复制于 MultiDex 源码
     *
     * @param context context
     * @return SharedPreferences
     */
    private static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences(PREFS_FILE,
            Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
            ? Context.MODE_PRIVATE
            : Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }


    /**
     * 通过指定包名，扫描包下面包含的所有的 ClassName
     *
     * 1. 获取所有 dex file
     * 2. 从 dex file 中获取所有 packageName 目录下 class name
     *
     * @param context U know
     * @param packageName 包名
     * @return 所有class的集合
     */
    public static List<String> getFileNameByPackageName(Context context, String packageName)
        throws PackageManager.NameNotFoundException, IOException {
        List<String> classNames = new ArrayList<>();
        for (String path : getSourcePaths(context)) {
            DexFile dexfile = null;

            try {
                if (path.endsWith(EXTRACTED_SUFFIX)) {
                    // NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                    dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                } else {
                    dexfile = new DexFile(path);
                }
                Enumeration<String> dexEntries = dexfile.entries();
                while (dexEntries.hasMoreElements()) {
                    String className = dexEntries.nextElement();
                    if (className.contains(packageName)) {
                        classNames.add(className);
                    }
                }
            } catch (Throwable ignore) {
                Log.e("ARouter", "Scan map file in dex files made error.", ignore);
            } finally {
                if (null != dexfile) {
                    try {
                        dexfile.close();
                    } catch (Throwable ignore) {
                    }
                }
            }
        }

        Log.d("ARouter",
            "Filter " + classNames.size() + " classes by packageName <" + packageName + ">");
        return classNames;
    }


    /**
     * get all the dex path
     *
     * 参考了 MultiDex 源码
     *
     * 1. 如果 VM 已经支持了 MultiDex，就不要去 Secondary Folder 加载 Classesx.zip 了，那里已经么有了
     * 2. 如果没支持，继续
     * 3. 找到 /data/data/( app package name )/code_cache/secondary-dexes 文件夹
     * 4. 获取 dex file
     * 5. 如果是 debug 模式，还得去尝试读取 instant run 的全部 dex 文件
     *
     * @param context the application context
     * @return all the dex path
     * @throws PackageManager.NameNotFoundException exception
     * @throws IOException exception
     */
    public static List<String> getSourcePaths(Context context)
        throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = context.getPackageManager()
            .getApplicationInfo(context.getPackageName(), 0);
        File sourceApk = new File(applicationInfo.sourceDir);

        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir); // add the default apk path

        // the prefix of extracted file, ie: test.classes
        String extractedFilePrefix = sourceApk.getName() + EXTRACTED_NAME_EXT;

        // 如果 VM 已经支持了 MultiDex，就不要去 Secondary Folder 加载 Classesx.zip 了，那里已经么有了
        // 通过是否存在 sp 中的 multidex.version 是不准确的，因为从低版本升级上来的用户，是包含这个 sp 配置的
        if (!isVMMultidexCapable()) {
            // the total dex numbers
            int totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1);
            File dexDir = new File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME);

            for (int secondaryNumber = 2; secondaryNumber <= totalDexNumber; secondaryNumber++) {
                // for each dex file, ie: test.classes2.zip, test.classes3.zip...
                String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
                File extractedFile = new File(dexDir, fileName);
                if (extractedFile.isFile()) {
                    sourcePaths.add(extractedFile.getAbsolutePath());
                    // we ignore the verify zip part
                } else {
                    throw new IOException(
                        "Missing extracted secondary dex file '" + extractedFile.getPath() + "'");
                }
            }
        }

        if (ARouter.debuggable()) { // Search instant run support only debuggable
            sourcePaths.addAll(tryLoadInstantRunDexFile(applicationInfo));
        }
        return sourcePaths;
    }


    /**
     * Get instant run dex path, used to catch the branch usingApkSplits=false.
     *
     * 尝试读取 instant run 的全部 dex 文件
     *
     * >= 5.0
     * 直接获取 applicationInfo.splitSourceDirs
     *
     * < 5.0
     * 1. 开始反射实例化 instant run 包的 Paths
     * 2. 反射调用 Paths # getDexFileDirectory 获取到 dex 文件夹
     * 3. 然后拿到每个 .dex 文件
     */
    private static List<String> tryLoadInstantRunDexFile(ApplicationInfo applicationInfo) {
        List<String> instantRunSourcePaths = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            null != applicationInfo.splitSourceDirs) {
            // add the splite apk, normally for InstantRun, and newest version.
            instantRunSourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
            Log.d("ARouter", "Found InstantRun support");
        } else {
            try {
                // This man is reflection from Google instant run sdk, he will tell me where the dex files go.
                Class pathsByInstantRun = Class.forName("com.android.tools.fd.runtime.Paths");
                Method getDexFileDirectory = pathsByInstantRun.getMethod("getDexFileDirectory",
                    String.class);
                String instantRunDexPath = (String) getDexFileDirectory.invoke(null,
                    applicationInfo.packageName);

                File instantRunFilePath = new File(instantRunDexPath);
                if (instantRunFilePath.exists() && instantRunFilePath.isDirectory()) {
                    File[] dexFile = instantRunFilePath.listFiles();
                    for (File file : dexFile) {
                        if (null != file && file.exists() && file.isFile() &&
                            file.getName().endsWith(".dex")) {
                            instantRunSourcePaths.add(file.getAbsolutePath());
                        }
                    }
                    Log.d("ARouter", "Found InstantRun support");
                }

            } catch (Exception e) {
                Log.e("ARouter", "InstantRun support error, " + e.getMessage());
            }
        }

        return instantRunSourcePaths;
    }


    /**
     * Identifies if the current VM has a native support for multidex, meaning there is no need for
     * additional installation by this library.
     *
     * 判断虚拟机是否支持 MultiDex
     *
     * 复制于 MultiDex 源码
     *
     * 1. 通过正则表达式将版本号分成 major (主版本号)和 minor (次版本号)
     * 2. 通过判断主版本和次版本是否大于一个常量来判定虚拟机内建支持 MultiDex
     *
     * 当虚拟机的主版本号大于 3 或版本大于等于 2.1 时，就意味着内建支持 MultiDex
     * 实际中，不同 Android 版本的虚拟机版本对照表如下：
     *
     * Android版本    |   虚拟机版本
     *
     * android 4.4    |     2.0
     *
     * android 5.0    |     2.1
     *
     * android 5.0.1  |     2.1
     *
     * android 5.1    |     2.1
     *
     * android 6.0    |     2.1
     *
     * 4.4 以后的 ART 虚拟机均支持内建的 MultiDex 特征
     * 4.4 的 ART 虚拟机还处于测试阶段，所以不支持
     *
     * @return true if the VM handles MultiDex
     */
    private static boolean isVMMultidexCapable() {
        boolean isMultidexCapable = false;
        String vmName = null;

        try {
            if (isYunOS()) {    // YunOS需要特殊判断
                vmName = "'YunOS'";
                isMultidexCapable = Integer.valueOf(System.getProperty("ro.build.version.sdk")) >=
                    21;
            } else {    // 非YunOS原生Android
                vmName = "'Android'";
                String versionString = System.getProperty("java.vm.version");
                if (versionString != null) {
                    Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?")
                        .matcher(versionString);
                    if (matcher.matches()) {
                        try {
                            int major = Integer.parseInt(matcher.group(1));
                            int minor = Integer.parseInt(matcher.group(2));
                            isMultidexCapable = (major > VM_WITH_MULTIDEX_VERSION_MAJOR)
                                || ((major == VM_WITH_MULTIDEX_VERSION_MAJOR)
                                && (minor >= VM_WITH_MULTIDEX_VERSION_MINOR));
                        } catch (NumberFormatException ignore) {
                            // let isMultidexCapable be false
                        }
                    }
                }
            }
        } catch (Exception ignore) {

        }

        Log.i("galaxy", "VM with name " + vmName +
            (isMultidexCapable ? " has multidex support" : " does not have multidex support"));
        return isMultidexCapable;
    }


    /**
     * 判断系统是否为 YunOS 系统
     */
    private static boolean isYunOS() {
        try {
            String version = System.getProperty("ro.yunos.version");
            String vmName = System.getProperty("java.vm.name");
            return (vmName != null && vmName.toLowerCase().contains("lemur"))
                || (version != null && version.trim().length() > 0);
        } catch (Exception ignore) {
            return false;
        }
    }

}