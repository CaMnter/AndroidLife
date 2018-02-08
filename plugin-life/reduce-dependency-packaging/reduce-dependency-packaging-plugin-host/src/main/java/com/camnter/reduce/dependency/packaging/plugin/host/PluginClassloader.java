package com.camnter.reduce.dependency.packaging.plugin.host;

import dalvik.system.DexClassLoader;

/**
 * @author CaMnter
 */

public class PluginClassloader extends DexClassLoader {

    public PluginClassloader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

}
