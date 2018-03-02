/*
 * Copyright (C) 2011 The Android Open Source Project
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
package dalvik.system;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.List;

/**
 * Base class for common functionality between various dex-based
 * {@link ClassLoader} implementations.
 *
 * @author CaMnter
 */
public class BaseDexClassLoader extends ClassLoader {

    public BaseDexClassLoader(String dexPath, File optimizedDirectory,
                              String librarySearchPath, ClassLoader parent) {
        throw new RuntimeException("Stub!");
    }


    private void reportClassLoaderChain() {
        throw new RuntimeException("Stub!");
    }


    public BaseDexClassLoader(ByteBuffer[] dexFiles, ClassLoader parent) {
        throw new RuntimeException("Stub!");
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new RuntimeException("Stub!");
    }


    public void addDexPath(String dexPath) {
        throw new RuntimeException("Stub!");
    }


    @Override
    protected URL findResource(String name) {
        throw new RuntimeException("Stub!");
    }


    @Override
    protected Enumeration<URL> findResources(String name) {
        throw new RuntimeException("Stub!");
    }


    @Override
    public String findLibrary(String name) {
        throw new RuntimeException("Stub!");
    }


    @Override
    protected synchronized Package getPackage(String name) {
        throw new RuntimeException("Stub!");
    }


    public String getLdLibraryPath() {
        throw new RuntimeException("Stub!");
    }


    @Override
    public String toString() {
        throw new RuntimeException("Stub!");
    }


    public static void setReporter(Reporter newReporter) {
        throw new RuntimeException("Stub!");
    }


    /**
     * @hide
     */
    public static Reporter getReporter() {
        throw new RuntimeException("Stub!");
    }


    /**
     * @hide
     */
    public interface Reporter {
        /**
         * Reports the construction of a BaseDexClassLoader and provides information about the
         * class loader chain.
         * Note that this only reports if all class loader in the chain are BaseDexClassLoader.
         *
         * @param classLoadersChain the chain of class loaders used during the construction of the
         * class loader. The first element is the BaseDexClassLoader being constructed,
         * the second element is its parent, and so on.
         * @param classPaths the class paths of the class loaders present in
         * {@param classLoadersChain}. The first element corresponds to the first class
         * loader and so on. A classpath is represented as a list of dex files separated by
         * {@code File.pathSeparator}.
         */
        void report(List<BaseDexClassLoader> classLoadersChain, List<String> classPaths);
    }

}