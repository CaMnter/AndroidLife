package com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence

/**
 * Refer from VirtualAPK
 *
 * Represents a Jar dependency. This could be the output of a Java project.
 *
 * @author CaMnter
 */

class JarDependenceInfo extends DependenceInfo {

    // @Delegate JavaDependency dependency
    File file

    JarDependenceInfo(String group, String artifact, String version, File file) {
        super(group, artifact, version)
        // this.dependency = jarDependency
        this.file = file
    }

    @Override
    File getJarFile() {
        return this.file
    }

    @Override
    DependenceType getDependenceType() {
        return DependenceType.JAR
    }
}