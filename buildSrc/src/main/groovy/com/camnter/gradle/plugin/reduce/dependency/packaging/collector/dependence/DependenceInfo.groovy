package com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence

/**
 * Copy from VirtualAPK
 *
 * Represents a dependency in Android Project
 *
 * @author CaMnter
 */

abstract class DependenceInfo {

    /**
     * The type of of the DependenceInfo.
     * */
    enum DependenceType {

        AAR(0x01) /* Type of Android library */,
        JAR(0x02) /* Type of Java library */

        private final int value

        DependenceType(int value) {
            this.value = value
        }

        int getValue() {
            return value
        }
    }

    /**
     * Group name of dependence in a Maven repository
     * */
    private String group
    /**
     * Module name of dependence in a Maven repository
     * */
    private String artifact
    /**
     * Version of dependence in a Maven repository
     * */
    private String version

    DependenceInfo(String group, String artifact, String version) {
        this.group = group
        this.artifact = artifact
        this.version = version
    }

    String getGroup() {
        return group
    }

    String getArtifact() {
        return artifact
    }

    String getVersion() {
        return version
    }

    abstract File getJarFile()

    abstract DependenceType getDependenceType()

    @Override
    String toString() {
        return "${group}:${artifact}:${version} -> ${jarFile} -> ${super.toString()}"
    }
}