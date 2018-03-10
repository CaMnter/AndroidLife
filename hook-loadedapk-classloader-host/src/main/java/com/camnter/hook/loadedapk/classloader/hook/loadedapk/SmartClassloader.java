package com.camnter.hook.loadedapk.classloader.hook.loadedapk;

import dalvik.system.DexClassLoader;

/**
 * @author CaMnter
 */

public class SmartClassloader extends DexClassLoader {

    public SmartClassloader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

}
