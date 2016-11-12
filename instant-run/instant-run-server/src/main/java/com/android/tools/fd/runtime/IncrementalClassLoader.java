/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.fd.runtime;

import android.util.Log;
import com.android.annotations.NonNull;
import dalvik.system.BaseDexClassLoader;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import static com.android.tools.fd.runtime.BootstrapApplication.LOG_TAG;

// This is based on com.google.devtools.build.android.incrementaldeployment.IncrementalClassLoader
// with some cleanup around path handling and made it compile on JDK 6 (e.g. removed multicatch
// etc)
// See
//  https://github.com/google/bazel/blob/master/src/tools/android/java/com/google/devtools/build/android/incrementaldeployment/IncrementalClassLoader.java
// (May 11 revision, ca96e11)


/**
 * A class loader that loads classes from any .dex file in a particular directory on the SD card.
 * <p>
 * <p>Used to implement incremental deployment to Android phones.
 */
public class IncrementalClassLoader extends ClassLoader {
    /** When false, compiled out of runtime library */
    public static final boolean DEBUG_CLASS_LOADING = false;

    private final DelegateClassLoader delegateClassLoader;


    /**
     * 为了保证和 要 Hook 的 classLoader 保持一致的 父 classLoader，特意用了  super(original.getParent()) 去实例化
     * 一个和 要 Hook 的 classLoader 一样的 父 classLoader
     * 然后执行 静态工厂方法 createDelegateClassLoader 去 构造一个 DelegateClassLoader
     *
     * 比如 original 的关系如下：BootClassLoader -> original
     * 正常的话，我们拿到 original 去实例化一个 classLoader 会如下情况：
     * BootClassLoader -> original —> classLoader
     * 但是这里执行的是 super(original.getParent()) 而不是 super(original)
     *
     * 所以这里得到的是 BootClassLoader -> classLoader
     *
     * @param original hook 的 classLoader
     * @param nativeLibraryPath 本地 lib 路径
     * @param codeCacheDir 项目 cache 文件夹路径
     * @param dexes dex 的所有路径
     */
    public IncrementalClassLoader(
        ClassLoader original, String nativeLibraryPath, String codeCacheDir, List<String> dexes) {
        super(original.getParent());

        // TODO(bazel-team): For some mysterious reason, we need to use two class loaders so that
        // everything works correctly. Investigate why that is the case so that the code can be
        // simplified.
        delegateClassLoader = createDelegateClassLoader(nativeLibraryPath, codeCacheDir, dexes,
            original);
    }


    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        try {
            Class<?> aClass = delegateClassLoader.findClass(className);
            //noinspection PointlessBooleanExpression,ConstantConditions
            if (DEBUG_CLASS_LOADING && Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG,
                    "Incremental class loader: findClass(" + className + ") = " + aClass);
            }

            return aClass;
        } catch (ClassNotFoundException e) {
            //noinspection PointlessBooleanExpression,ConstantConditions
            if (DEBUG_CLASS_LOADING && Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG,
                    "Incremental class loader: findClass(" + className + ") : not found");
            }
            throw e;
        }
    }


    /**
     * 代理 ClassLoader
     * 没有做任何功能上的扩展
     * 主要为了想在 findClass 里打点 Log
     *
     * A class loader whose only purpose is to make {@code findClass()} public.
     */
    private static class DelegateClassLoader extends BaseDexClassLoader {
        private DelegateClassLoader(
            String dexPath, File optimizedDirectory, String libraryPath, ClassLoader parent) {
            super(dexPath, optimizedDirectory, libraryPath, parent);
        }


        /**
         * 代理 BaseDexClassLoader 的 findClass(String name) 方法
         * 主要就是想打点 Log
         *
         * @param name 类名
         * @return Class
         * @throws ClassNotFoundException
         */
        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                Class<?> aClass = super.findClass(name);
                //noinspection PointlessBooleanExpression,ConstantConditions
                if (DEBUG_CLASS_LOADING && Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Delegate class loader: findClass(" + name + ") = " + aClass);
                }

                return aClass;
            } catch (ClassNotFoundException e) {
                //noinspection PointlessBooleanExpression,ConstantConditions
                if (DEBUG_CLASS_LOADING && Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Delegate class loader: findClass(" + name + ") : not found");
                }
                throw e;
            }
        }
    }


    /**
     * 静态工厂方法，构建一个 DelegateClassLoader 实例
     *
     * @param nativeLibraryPath 本地 lib 路径
     * @param codeCacheDir 项目 cache 文件夹路径
     * @param dexes dex 的所有路径
     * @param original 父 classloader（双亲机制需要）
     * @return DelegateClassLoader
     */
    private static DelegateClassLoader createDelegateClassLoader(
        String nativeLibraryPath, String codeCacheDir, List<String> dexes,
        ClassLoader original) {
        String pathBuilder = createDexPath(dexes);
        return new DelegateClassLoader(pathBuilder, new File(codeCacheDir),
            nativeLibraryPath, original);
    }


    /**
     * 拿到一组 dex 的路径，合成一个 String 数据
     *
     * @param dexes 一组 dex 的路径
     * @return String
     */
    @NonNull
    private static String createDexPath(List<String> dexes) {
        StringBuilder pathBuilder = new StringBuilder();
        boolean first = true;
        for (String dex : dexes) {
            // 最后一个不加分隔符 "/"
            if (first) {
                first = false;
            } else {
                pathBuilder.append(File.pathSeparator);
            }

            pathBuilder.append(dex);
        }

        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.v(LOG_TAG, "Incremental dex path is "
                + BootstrapApplication.join('\n', dexes));
        }
        return pathBuilder.toString();
    }


    /**
     * Hook ClassLoader 的 parent，由于 ClassLoader 的 parent 的只涉及到加载 class 和 res，所有 hook 了加载机制
     * newParent 一般都会是通过 BootClassloader 实例化出来的，所以 BootClassloader -> newParent
     * Hook 之后变为
     * BootClassloader -> classLoader  >>>>>>  BootClassloader -> newParent -> classLoader
     *
     * @param classLoader hook 目标 classLoader
     * @param newParent 需要作为 classLoader 的 parent classLoader 去加载 class 和 res
     */
    private static void setParent(ClassLoader classLoader, ClassLoader newParent) {
        try {
            Field parent = ClassLoader.class.getDeclaredField("parent");
            parent.setAccessible(true);
            parent.set(classLoader, newParent);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 静态工厂方法，通过 classLoader + nativeLibraryPath + codeCacheDir + dexes = incrementalClassLoader
     * incrementalClassLoader 的构造方法指明了，会拿到 classLoader 的 parent 去作为 incrementalClassLoader 的 parent
     * 去实例化 incrementalClassLoader。所以是 BootClassLoader -> incrementalClassLoader。
     * 然后 classLoader 的关系是 BootClassLoader -> classLoader
     * 执行 setParent 后：BootClassLoader -> classLoader -> incrementalClassLoader
     *
     * @param classLoader 需要 hook 的 classLoader
     * @param nativeLibraryPath 本地 lib 路径
     * @param codeCacheDir 项目 cache 文件夹路径
     * @param dexes dex 的所有路径
     * @return hook 后的 classLoader 的 parent（incrementalClassLoader）
     */
    public static ClassLoader inject(
        ClassLoader classLoader, String nativeLibraryPath, String codeCacheDir,
        List<String> dexes) {
        IncrementalClassLoader incrementalClassLoader =
            new IncrementalClassLoader(classLoader, nativeLibraryPath, codeCacheDir, dexes);
        setParent(classLoader, incrementalClassLoader);

        // This works as follows:
        // We're given the current class loader that's used to load the bootstrap application.
        // We have a new class loader which reads patches/overrides from the data directory
        // instead. We want *that* class loader to have the bootstrap class loader's parent
        // as its parent, and then we make the bootstrap class loader parented by our
        // class loader.
        //
        // In other words, we have this:
        //      BootstrapApplication.classLoader = ClassLoader1, parent=ClassLoader2
        // We create ClassLoader3 from the .dex files in the data directory, and arrange for
        // the hierarchy to be like this:
        //      BootstrapApplication.classLoader = ClassLoader1, parent=ClassLoader3, parent=ClassLoader2
        // With this approach, a class find (which should always look at the parents first) should
        // find anything from ClassLoader3 before they get them from ClassLoader1.
        // (Note that ClassLoader2 in the above is generally the BootClassLoader, not containing
        // any classes we care about.)

        return incrementalClassLoader;
    }
}
