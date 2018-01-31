package com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence

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
    // @Delegate AndroidDependency dependency
    File file


    /**
     * All resources(e.g. drawable, layout...) this library can access
     * include resources of self-project and dependence(direct&transitive) project
     * */
    ListMultimap<String, ResourceEntry> aarResources = ArrayListMultimap.create()
    /**
     * All styleables this library can access, like "aarResources"
     * */
    List<StyleableEntry> aarStyleables = Lists.newArrayList()

    AarDependenceInfo(String group, String artifact, String version, File file) {
        super(group, artifact, version)
        // this.dependency = dependency
        this.file = file
    }

    @Override
    File getJarFile() {
        // TODO 剖析 AAR 内部的 jar，暂时返回 aar
        return this.file
    }

    @Override
    DependenceType getDependenceType() {
        return DependenceType.AAR
    }

    /**
     * Return collection of "resourceType:resourceName", parse from R symbol file
     * @return set of a combination of resource type and name
     */
    public Set<String> getResourceKeys() {

        def resKeys = [] as Set<String>

        def rSymbol = symbolFile
        if (rSymbol.exists()) {
            rSymbol.eachLine { line ->
                if (!line.empty) {
                    def tokenizer = new StringTokenizer(line)
                    def valueType = tokenizer.nextToken()
                    def resType = tokenizer.nextToken()
                    // resource type (attr/string/color etc.)
                    def resName = tokenizer.nextToken()
                    // resource name

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
        def xmlManifest = new XmlParser().parse(manifest)
        return xmlManifest.@package
    }
}