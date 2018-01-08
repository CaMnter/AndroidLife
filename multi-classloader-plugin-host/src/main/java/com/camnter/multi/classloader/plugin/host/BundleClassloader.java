package com.camnter.multi.classloader.plugin.host;

import dalvik.system.DexClassLoader;

/**
 * @author CaMnter
 */

public class BundleClassloader extends DexClassLoader {

    public BundleClassloader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

}
