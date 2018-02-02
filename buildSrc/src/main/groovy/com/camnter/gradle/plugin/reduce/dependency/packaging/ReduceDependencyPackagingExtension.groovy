package com.camnter.gradle.plugin.reduce.dependency.packaging

import com.android.build.gradle.internal.variant.BaseVariantData
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.AarDependenceInfo
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.DependenceInfo

/**
 * @author CaMnter
 */

class ReduceDependencyPackagingExtension {

    /** Exclude dependent aar or jar **/
    Collection<String> excludes = new HashSet<>()

    /**  host dependence file - version.txt*/
    File hostDependenceFile

    BaseVariantData variantData

    Collection<DependenceInfo> stripDependencies = []
    Collection<AarDependenceInfo> retainedAarLibs = []

}