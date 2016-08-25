/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */

package dodola.rocoofix;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;
import dalvik.system.DexFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.zip.ZipFile;

/**
 * modify from MultiDex source code
 *
 * https://github.com/dodola/RocooFix/blob/master/rocoo/src/main/java/com/dodola/rocoofix/RocooFix.java
 *
 * https://android.googlesource.com/platform/frameworks/multidex/+/master/library/src/android/support/multidex/MultiDex.java
 */
public final class RocooFix {

    static final String TAG = "Rocoo";

    private static final String CODE_CACHE_NAME = "code_cache";

    private static final String CODE_CACHE_SECONDARY_FOLDER_NAME = "rocoo-dexes";

    private static final Set<String> installedApk = new HashSet<String>();


    private RocooFix() {
    }


    public static void init(Context context) {
        initPathFromAssets(context, "rocoo.dex");
    }


    /**
     * 从 Assets 里取出补丁
     *
     * @param context context
     * @param assetName 补丁名
     */
    public static void initPathFromAssets(Context context, String assetName) {
        File dexDir = new File(context.getFilesDir(), "hotfix");
        // 创建 hotfix 文件夹
        dexDir.mkdir();
        String dexPath = null;
        try {
            // 复制补丁到 hotfix 文件夹内,并记录复制后补丁的路径
            dexPath = copyAsset(context, assetName, dexDir);
        } catch (IOException e) {
        } finally {
            // 如果复制成功
            if (dexPath != null && new File(dexPath).exists()) {
                // 加载补丁
                applyPatch(context, dexPath);
            }
        }
    }


    /**
     * 加载补丁
     *
     * @param context context
     * @param dexPath 补丁地址
     */
    public static void applyPatch(Context context, String dexPath) {

        // if (IS_VM_CAPABLE) {
        //     //art虚拟机走另外一套fix
        //     return;
        // }

        try {
            ApplicationInfo applicationInfo = getApplicationInfo(context);
            if (applicationInfo == null) {
                return;
            }

            synchronized (installedApk) {
                if (installedApk.contains(dexPath)) {
                    return;
                }
                installedApk.add(dexPath);

                /* The patched class loader is expected to be a descendant of
                 * dalvik.system.BaseDexClassLoader. We modify its
                 * dalvik.system.DexPathList pathList field to append additional DEX
                 * file entries.
                 */
                ClassLoader loader;
                try {
                    // 获取 PathClassLoader
                    loader = context.getClassLoader();
                } catch (RuntimeException e) {
                    /* Ignore those exceptions so that we don't break tests relying on Context like
                     * a android.test.mock.MockContext or a android.content.ContextWrapper with a
                     * null base Context.
                     */
                    Log.w(TAG, "Failure while trying to obtain Context class loader. " +
                        "Must be running in test mode. Skip patching.", e);
                    return;
                }
                if (loader == null) {
                    // Note, the context class loader is null when running Robolectric tests.
                    Log.e(TAG,
                        "Context class loader is null. Must be running in test mode. "
                            + "Skip patching.");
                    return;
                }

                // 包含补丁 File 的 List
                List<File> files = new ArrayList<File>();
                files.add(new File(dexPath));
                // 获取 缓存 dex File 的文件夹
                File dexDir = getDexDir(context, applicationInfo);
                installDexes(loader, dexDir, files);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取 ApplicationInfo 信息
     *
     * @param context context
     * @return ApplicationInfo
     * @throws NameNotFoundException
     */
    private static ApplicationInfo getApplicationInfo(Context context)
        throws NameNotFoundException {
        PackageManager pm;
        String packageName;
        try {
            pm = context.getPackageManager();
            packageName = context.getPackageName();
        } catch (RuntimeException e) {
            /* Ignore those exceptions so that we don't break tests relying on Context like
             * a android.test.mock.MockContext or a android.content.ContextWrapper with a null
             * base Context.
             */
            Log.w(TAG, "Failure while trying to obtain ApplicationInfo from Context. " +
                "Must be running in test mode. Skip patching.", e);
            return null;
        }
        if (pm == null || packageName == null) {
            // This is most likely a mock context, so just return without patching.
            return null;
        }
        return pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
    }


    /**
     * 开始 根据不同版本的策略 插入补丁 dex
     *
     * @param loader classloader
     * @param dexDir 缓存 dex File 的文件夹
     * @param files 包含补丁 File 的 List
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IOException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    private static void installDexes(ClassLoader loader, File dexDir, List<File> files)
        throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
               InvocationTargetException, NoSuchMethodException, IOException,
               InstantiationException, ClassNotFoundException {
        if (!files.isEmpty()) {
            if (Build.VERSION.SDK_INT >= 24) {
                // Android N or 7 的策略
                V24.install(loader, files, dexDir);
            } else if (Build.VERSION.SDK_INT >= 23) {
                // Android 6.0 的策略
                V23.install(loader, files, dexDir);
            } else if (Build.VERSION.SDK_INT >= 19) {
                // Android 4.4 以上的策略
                V19.install(loader, files, dexDir);
            } else if (Build.VERSION.SDK_INT >= 14) {
                // Android 4.0 以上的策略
                V14.install(loader, files, dexDir);
            } else {
                // Android 4.0 以下的策略
                V4.install(loader, files);
            }
        }
    }


    /**
     * Locates a given field anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the field into.
     * @param name field name
     * @return a field object
     * @throws NoSuchFieldException if the field cannot be located
     */
    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }
        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }


    /**
     * 反射获取 目标类的 目标属性
     *
     * Locates a given method anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the method into.
     * @param name method name
     * @param parameterTypes method parameter types
     * @return a method object
     * @throws NoSuchMethodException if the method cannot be located
     */
    private static Method findMethod(Object instance, String name, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);

                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }
        throw new NoSuchMethodException("Method " + name + " with parameters " +
            Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }


    /**
     * 反射合并 两个数组
     * 要合并的数据(补丁)  插到 合并后数组的 头部
     *
     * Replace the value of a field containing a non null array, by a new array containing the
     * elements of the original array plus the elements of extraElements.
     *
     * @param instance the instance whose field is to be modified.
     * @param fieldName the field to modify.
     * @param extraElements elements to append at the end of the array.
     */
    private static void expandFieldArray(Object instance, String fieldName,
                                         Object[] extraElements)
        throws NoSuchFieldException, IllegalArgumentException,
               IllegalAccessException {
        // 反射获取 旧数组 数据
        Field jlrField = findField(instance, fieldName);
        // 转换为 旧 Object[]
        Object[] original = (Object[]) jlrField.get(instance);
        // 实例化一个 新 Object[],容纳 旧 Object[] 和 此次要合并的数组数据
        Object[] combined = (Object[]) Array.newInstance(
            original.getClass().getComponentType(), original.length + extraElements.length);
        // 先放入 此次要合并的数组数据
        System.arraycopy(extraElements, 0, combined, 0, extraElements.length);
        // 再 放入 就 Object[] 数据
        System.arraycopy(original, 0, combined, extraElements.length, original.length);
        // 修改 旧数组地址 内容
        jlrField.set(instance, combined);
    }


    /**
     * 获取 缓存 dex File 的文件夹
     *
     * @param context context
     * @param applicationInfo applicationInfo
     * @return 文件夹 File
     * @throws IOException
     */
    private static File getDexDir(Context context, ApplicationInfo applicationInfo)
        throws IOException {
        // /data/data/code_cache
        File cache = new File(applicationInfo.dataDir, CODE_CACHE_NAME);
        try {
            // 检查 /data/data/code_cache
            mkdirChecked(cache);
        } catch (IOException e) {
            /* If we can't emulate code_cache, then store to filesDir. This means abandoning useless
             * files on disk if the device ever updates to android 5+. But since this seems to
             * happen only on some devices running android 2, this should cause no pollution.
             */
            // /code_cache
            cache = new File(context.getFilesDir(), CODE_CACHE_NAME);
            // 检查 /code_cache
            mkdirChecked(cache);
        }
        // /data/data/code_cache/rocoo-dexes
        File dexDir = new File(cache, CODE_CACHE_SECONDARY_FOLDER_NAME);
        // 检查 /data/data/code_cache/rocoo-dexes
        mkdirChecked(dexDir);
        return dexDir;
    }


    /**
     * 检查 文件夹 是否存在
     *
     * @param dir file
     * @throws IOException
     */
    private static void mkdirChecked(File dir) throws IOException {
        dir.mkdir();
        if (!dir.isDirectory()) {
            File parent = dir.getParentFile();
            if (parent == null) {
                Log.e(TAG, "Failed to create dir " + dir.getPath() + ". Parent file is null.");
            } else {
                Log.e(TAG, "Failed to create dir " + dir.getPath() +
                    ". parent file is a dir " + parent.isDirectory() +
                    ", a file " + parent.isFile() +
                    ", exists " + parent.exists() +
                    ", readable " + parent.canRead() +
                    ", writable " + parent.canWrite());
            }
            throw new IOException("Failed to create directory " + dir.getPath());
        }
    }


    private static final class V24 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
            throws IllegalArgumentException, IllegalAccessException,
                   NoSuchFieldException, InvocationTargetException, NoSuchMethodException,
                   InstantiationException, ClassNotFoundException {

            // 获取 BaseDexClassLoader 中的 DexPathList pathList 属性
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            // 获取 DexPathList 中的 Element[] dexElements 属性
            Field dexElement = findField(dexPathList, "dexElements");
            Class<?> elementType = dexElement.getType().getComponentType();
            /**
             * 获取 DexPathList 中的 loadDexFile 方法
             * private static DexFile loadDexFile(File file, File optimizedDirectory, ClassLoader loader,
             *      Element[] elements)
             */
            Method loadDex = findMethod(dexPathList, "loadDexFile", File.class, File.class,
                ClassLoader.class, dexElement.getType());
            loadDex.setAccessible(true);

            // 调用 loadDexFile 方法
            Object dex = loadDex.invoke(null, additionalClassPathEntries.get(0), optimizedDirectory,
                loader, dexElement.get(dexPathList));
            // 反射 Element 的构造方法
            Constructor<?> constructor = elementType.getConstructor(File.class, boolean.class,
                File.class, DexFile.class);
            constructor.setAccessible(true);
            // 反射构造一个 Element
            Object element = constructor.newInstance(new File(""), false,
                additionalClassPathEntries.get(0), dex);

            // 将 刚才构造的 Element 放入数组中
            Object[] newEles = new Object[1];
            newEles[0] = element;

            /**
             * 合并 dexElements 数组 和 构造的 Element 数组
             * 构造的 Element 数组 会在合并数组的头部
             * 达到插桩效果
             */
            expandFieldArray(dexPathList, "dexElements", newEles);
        }

    }


    private static final class V23 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
            throws IllegalArgumentException, IllegalAccessException,
                   NoSuchFieldException, InvocationTargetException, NoSuchMethodException,
                   InstantiationException {

            // 获取 BaseDexClassLoader 中的 DexPathList pathList 属性
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            // 获取 DexPathList 中的 Element[] dexElements 属性
            Field dexElement = findField(dexPathList, "dexElements");
            // 拿到 Element 的 class
            Class<?> elementType = dexElement.getType().getComponentType();
            /**
             * 获取 DexPathList 中的 loadDexFile 方法
             * private static DexFile loadDexFile(File file, File optimizedDirectory)
             */
            Method loadDex = findMethod(dexPathList, "loadDexFile", File.class, File.class);
            loadDex.setAccessible(true);
            // 调用 loadDexFile 方法
            Object dex = loadDex.invoke(null, additionalClassPathEntries.get(0),
                optimizedDirectory);
            // 反射 Element 的构造方法
            Constructor<?> constructor = elementType.getConstructor(File.class, boolean.class,
                File.class, DexFile.class);
            constructor.setAccessible(true);
            // 反射构造一个 Element
            Object element = constructor.newInstance(new File(""), false,
                additionalClassPathEntries.get(0), dex);

            // 将 刚才构造的 Element 放入数组中
            Object[] newEles = new Object[1];
            newEles[0] = element;

            /**
             * 合并 dexElements 数组 和 构造的 Element 数组
             * 构造的 Element 数组 会在合并数组的头部
             * 达到插桩效果
             */
            expandFieldArray(dexPathList, "dexElements", newEles);
        }

    }


    /**
     * Installer for platform versions 19.
     */
    private static final class V19 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
            throws IllegalArgumentException, IllegalAccessException,
                   NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                new ArrayList<File>(additionalClassPathEntries), optimizedDirectory,
                suppressedExceptions));
            if (suppressedExceptions.size() > 0) {
                for (IOException e : suppressedExceptions) {
                    Log.w(TAG, "Exception in makeDexElement", e);
                }
                Field suppressedExceptionsField =
                    findField(loader, "dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions =
                    (IOException[]) suppressedExceptionsField.get(loader);

                if (dexElementsSuppressedExceptions == null) {
                    dexElementsSuppressedExceptions =
                        suppressedExceptions.toArray(
                            new IOException[suppressedExceptions.size()]);
                } else {
                    IOException[] combined =
                        new IOException[suppressedExceptions.size() +
                            dexElementsSuppressedExceptions.length];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions, 0, combined,
                        suppressedExceptions.size(), dexElementsSuppressedExceptions.length);
                    dexElementsSuppressedExceptions = combined;
                }

                suppressedExceptionsField.set(loader, dexElementsSuppressedExceptions);
            }
        }


        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
            Object dexPathList, ArrayList<File> files, File optimizedDirectory,
            ArrayList<IOException> suppressedExceptions)
            throws IllegalAccessException, InvocationTargetException,
                   NoSuchMethodException {
            Method makeDexElements =
                findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class,
                    ArrayList.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory,
                suppressedExceptions);
        }
    }


    /**
     * Installer for platform versions 14, 15, 16, 17 and 18.
     */
    private static final class V14 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
            throws IllegalArgumentException, IllegalAccessException,
                   NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                new ArrayList<File>(additionalClassPathEntries), optimizedDirectory));
        }


        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
            Object dexPathList, ArrayList<File> files, File optimizedDirectory)
            throws IllegalAccessException, InvocationTargetException,
                   NoSuchMethodException {
            Method makeDexElements =
                findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory);
        }
    }


    /**
     * Installer for platform versions 4 to 13.
     */
    private static final class V4 {
        private static void install(ClassLoader loader, List<File> additionalClassPathEntries)
            throws IllegalArgumentException, IllegalAccessException,
                   NoSuchFieldException, IOException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.DexClassLoader. We modify its
             * fields mPaths, mFiles, mZips and mDexs to append additional DEX
             * file entries.
             */
            int extraSize = additionalClassPathEntries.size();

            Field pathField = findField(loader, "path");

            StringBuilder path = new StringBuilder((String) pathField.get(loader));
            String[] extraPaths = new String[extraSize];
            File[] extraFiles = new File[extraSize];
            ZipFile[] extraZips = new ZipFile[extraSize];
            DexFile[] extraDexs = new DexFile[extraSize];
            for (ListIterator<File> iterator = additionalClassPathEntries.listIterator();
                 iterator.hasNext(); ) {
                File additionalEntry = iterator.next();
                String entryPath = additionalEntry.getAbsolutePath();
                path.append(':').append(entryPath);
                int index = iterator.previousIndex();
                extraPaths[index] = entryPath;
                extraFiles[index] = additionalEntry;
                extraZips[index] = new ZipFile(additionalEntry);
                extraDexs[index] = DexFile.loadDex(entryPath, entryPath + ".dex", 0);
            }

            pathField.set(loader, path.toString());
            expandFieldArray(loader, "mPaths", extraPaths);
            expandFieldArray(loader, "mFiles", extraFiles);
            expandFieldArray(loader, "mZips", extraZips);
            expandFieldArray(loader, "mDexs", extraDexs);
        }
    }


    /**
     * 复制 Asset 的文件
     *
     * @param context context
     * @param assetName Asset 内的文件名
     * @param dir 指定目录
     * @return 返回输出路径
     * @throws IOException
     */
    public static String copyAsset(Context context, String assetName, File dir) throws IOException {
        File outFile = new File(dir, assetName);
        if (!outFile.exists()) {
            AssetManager assetManager = context.getAssets();
            InputStream in = assetManager.open(assetName);
            OutputStream out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            out.close();
        }
        return outFile.getAbsolutePath();
    }


    /**
     * 复制文件
     *
     * @param in InputStream
     * @param out OutputStream
     * @throws IOException
     */
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

}
