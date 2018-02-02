package com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence

import com.android.builder.model.AndroidLibrary
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.res.ResourceEntry
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.res.StyleableEntry
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Lists

/**
 * Refer from VirtualAPK
 *
 * Represents a AAR dependence from Maven repository or Android library module
 *
 * @author CaMnter
 */

class AarDependenceInfo extends DependenceInfo {

    /**
     * Android library dependence in android build system, delegate of AarDependenceInfo
     * */
    AndroidLibrary library

    /**
     * All resources(e.g. drawable, layout...) this library can access
     * include resources of self-project and dependence(direct&transitive) project
     * */
    ListMultimap<String, ResourceEntry> aarResources = ArrayListMultimap.create()
    /**
     * All styleables this library can access, like "aarResources"
     * */
    List<StyleableEntry> aarStyleables = Lists.newArrayList()

    AarDependenceInfo(String group, String artifact, String version, AndroidLibrary library) {
        super(group, artifact, version)
        this.library = library
    }

    @Override
    File getJarFile() {
        return library.jarFile
    }

    @Override
    DependenceType getDependenceType() {
        return DependenceType.AAR
    }

    File getAssetsFolder() {
        return library.assetsFolder
    }

    File getJniFolder() {
        return library.jniFolder
    }

    Collection<File> getLocalJars() {
        return library.localJars
    }

    /**
     * Return collection of "resourceType:resourceName", parse from R symbol file
     * @return set of a combination of resource type and name
     */
    public Set<String> getResourceKeys() {

        def resKeys = [] as Set<String>

        def rSymbol = library.symbolFile
        if (rSymbol.exists()) {
            rSymbol.eachLine { line ->
                if (!line.empty) {
                    def tokenizer = new StringTokenizer(line)
                    def valueType = tokenizer.nextToken()
                    // resource type (attr/string/color etc.)
                    def resType = tokenizer.nextToken()
                    // resource name
                    def resName = tokenizer.nextToken()


                    resKeys.add("${resType}:${resName}")
                }
            }
        }

        return resKeys
    }

    /**
     * Return the package name of this library, parse from manifest file
     * manifest file are obtained by delegating to "dependency"
     * @return package name of this library
     */
    public String getPackage() {
        def xmlManifest = new XmlParser().parse(library.manifest)
        return xmlManifest.@package
    }
}