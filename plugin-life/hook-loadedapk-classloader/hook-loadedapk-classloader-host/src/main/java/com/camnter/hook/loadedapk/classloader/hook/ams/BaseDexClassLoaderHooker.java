package com.camnter.hook.loadedapk.classloader.hook.ams;

import android.os.Build;
import android.support.annotation.NonNull;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipFile;

/**
 * DexElements 插桩
 *
 * @author CaMnter
 */

public final class BaseDexClassLoaderHooker {

    @SuppressWarnings("DanglingJavadoc")
    public static void patchClassLoader(@NonNull final ClassLoader classLoader,
                                        @NonNull final File apkFile,
                                        @NonNull final File optDexFile)
        throws IllegalAccessException,
               NoSuchMethodException,
               IOException,
               InvocationTargetException,
               InstantiationException,
               NoSuchFieldException,
               ClassNotFoundException {

        // 获取 BaseDexClassLoader # DexPathList pathList
        final Field pathListField = DexClassLoader.class.getSuperclass()
            .getDeclaredField("pathList");
        pathListField.setAccessible(true);
        final Object pathList = pathListField.get(classLoader);

        // 获取 DexPathList # Element[] dexElements
        final Field dexElementArray = pathList.getClass().getDeclaredField("dexElements");
        dexElementArray.setAccessible(true);
        final Object[] dexElements = (Object[]) dexElementArray.get(pathList);

        // Element 类型
        final Class<?> elementClass = dexElements.getClass().getComponentType();

        // 用于替换 PathList # Element[] dexElements
        final Object[] newElements = (Object[]) Array.newInstance(elementClass,
            dexElements.length + 1);

        /**
         * <= 4.0.0
         *
         * no method
         *
         * >= 4.0.0
         *
         * Element(File file, ZipFile zipFile, DexFile dexFile)
         *
         * ---
         *
         * >= 5.0.0
         *
         * Element(File file, boolean isDirectory, File zip, DexFile dexFile)
         *
         * ---
         *
         * >= 8.0.0
         *
         * @Deprecated
         * Element(File dir, boolean isDirectory, File zip, DexFile dexFile)
         * Element(DexFile dexFile, File dexZipPath)
         *
         */
        final int sdkVersion = Build.VERSION.SDK_INT;

        if (sdkVersion < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            throw new RuntimeException(
                "[BaseDexClassLoaderHooker]   the sdk version must >= 14 (4.0.0)");
        }

        final Object element;
        final Constructor<?> constructor;

        if (sdkVersion >= Build.VERSION_CODES.O) {
            // >= 8.0.0
            // DexFile dexFile, File dexZipPath
            constructor = elementClass.getConstructor(
                DexFile.class,
                File.class
            );
            element = constructor.newInstance(
                DexFile.loadDex(apkFile.getCanonicalPath(), optDexFile.getAbsolutePath(), 0),
                apkFile
            );
        } else if (sdkVersion >= Build.VERSION_CODES.LOLLIPOP) {
            // >= 5.0.0
            // File file, boolean isDirectory, File zip, DexFile dexFile
            constructor = elementClass.getConstructor(
                File.class,
                boolean.class,
                File.class,
                DexFile.class
            );
            element = constructor.newInstance(
                apkFile,
                false,
                apkFile,
                DexFile.loadDex(apkFile.getCanonicalPath(), optDexFile.getAbsolutePath(), 0)
            );
        } else {
            // >= 4.0.0
            // File file, ZipFile zipFile, DexFile dexFile
            constructor = elementClass.getConstructor(
                File.class,
                ZipFile.class,
                DexFile.class
            );
            element = constructor.newInstance(
                apkFile,
                new ZipFile(apkFile),
                DexFile.loadDex(apkFile.getCanonicalPath(), optDexFile.getAbsolutePath(), 0)
            );
        }

        final Object[] toAddElementArray = new Object[] { element };
        // 把原始的 elements 复制进去
        System.arraycopy(dexElements, 0, newElements, 0, dexElements.length);
        // 把插件的 element  复制进去
        System.arraycopy(toAddElementArray, 0, newElements, dexElements.length,
            toAddElementArray.length);

        // 替换
        dexElementArray.set(pathList, newElements);
    }

}
