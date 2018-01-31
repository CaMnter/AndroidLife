package com.camnter.gradle.plugin.reduce.dependency.packaging.collector

import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.DependenceInfo

import java.util.zip.ZipFile

/**
 * Refer from VirtualAPK
 *
 * Collector of Class and Java Resource(no-class files in jar) in host apk
 *
 * @author CaMnter
 */

class HostClassAndResCollector {

    private def hostJarFiles = [] as LinkedList<File>
    private def hostClassesAndResources = [] as LinkedHashSet<String>

    /**
     * Collect jar entries that already exist in the host apk
     *
     * @param stripDependencies DependencyInfos that exists in the host apk, including AAR and JAR
     * @return set of classes and java resources
     */
    public Set<String> collect(Collection<DependenceInfo> stripDependencies) {
        flatToJarFiles(stripDependencies, hostJarFiles)
        hostJarFiles.each {
            hostClassesAndResources.addAll(unzipJar(it))
        }
        hostClassesAndResources
    }

    /**
     * Collect the jar files that are held by the DependenceInfo， including local jars of the DependenceInfo
     * @param stripDependencies Collection of DependenceInfo
     * @param jarFiles Collection used to store jar files
     */
    def flatToJarFiles(Collection<DependenceInfo> stripDependencies, Collection<File> jarFiles) {
        stripDependencies.each {
            jarFiles.add(it.jarFile)
            // TODO 剖析 AAR 内部的 jar，暂时返回 aar
            //            if (it in AarDependenceInfo) {
            //                it.localJars.each {
            //                    jarFiles.add(it)
            //                }
            //            }
        }
    }

    /**
     * Unzip the entries of Jar
     *
     * @return Set of entries in the JarFile
     */
    public static Set<String> unzipJar(File jarFile) {

        def jarEntries = [] as Set<String>

        ZipFile zipFile = new ZipFile(jarFile)
        try {
            zipFile.entries().each {
                jarEntries.add(it.name)
            }
        } finally {
            zipFile.close()
        }

        return jarEntries
    }
}