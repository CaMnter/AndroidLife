package com.camnter.gradle.plugin.reduce.dependency.packaging

import com.android.build.gradle.internal.variant.BaseVariantData

/**
 * @author CaMnter
 */

class ReduceDependencyPackagingExtension {

    /** Exclude dependent aar or jar **/
    Collection<String> excludes = new HashSet<>()

    /**  host dependence file - version.txt*/
    File hostDependenceFile

    BaseVariantData variantData

}