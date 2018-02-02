package com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence

import com.android.builder.model.JavaLibrary

/**
 * Refer from VirtualAPK
 *
 * Represents a Jar dependency. This could be the output of a Java project.
 *
 * @author CaMnter
 */

class JarDependenceInfo extends DependenceInfo {

    JavaLibrary library

    JarDependenceInfo(String group, String artifact, String version, JavaLibrary library) {
        super(group, artifact, version)
        this.library = library
    }

    @Override
    File getJarFile() {
        return library.jarFile
    }

    @Override
    DependenceType getDependenceType() {
        return DependenceType.JAR
    }
}