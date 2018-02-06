package com.camnter.gradle.plugin.reduce.dependency.packaging

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.variant.BaseVariantData
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.AarDependenceInfo
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.DependenceInfo
import org.gradle.api.Project

/**
 * @author CaMnter
 */

class ReduceDependencyPackagingExtension {

    /** Exclude dependent aar or jar **/
    Collection<String> excludes = new HashSet<>()

    /**  host Symbol file - Host_R.txt */
    File hostSymbolFile
    /**  host dependence file - version.txt*/
    File hostDependenceFile

    BaseVariantData variantData

    Collection<DependenceInfo> stripDependencies = []
    Collection<AarDependenceInfo> retainedAarLibs = []

    /** Variant application id */
    String packageName

    /** Package path for java classes */
    String packagePath

    static String getApplicationId(Project project, ApplicationVariant variant) {
        String applicationId = variant.applicationId
        printf "%-57s = %s\n",
                ['[ReduceDependencyPackagingPlugin]   [applicationId]', applicationId]
        def manifestFile = project.file("src/main/AndroidManifest.xml")
        printf "%-57s = %s\n",
                ['[ReduceDependencyPackagingPlugin]   [manifestFile]', "${manifestFile.path}   [exists] = ${manifestFile.exists()}"]
        if (manifestFile.exists()) {
            def parsedManifest = new XmlParser().parse(
                    new InputStreamReader(new FileInputStream(manifestFile), "utf-8"))
            if (parsedManifest != null) {
                def packageName = parsedManifest.attribute("package")
                if (packageName != null) {
                    applicationId = packageName
                    printf "%-57s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [applicationId]', applicationId]
                }
            }
        }
        printf "%-57s = %s\n\n",
                ['[ReduceDependencyPackagingPlugin]   [applicationId]', applicationId]
        return applicationId
    }
}