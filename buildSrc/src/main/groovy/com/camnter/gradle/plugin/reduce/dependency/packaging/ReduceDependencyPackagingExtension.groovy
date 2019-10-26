//package com.camnter.gradle.plugin.reduce.dependency.packaging
//
//import com.android.build.gradle.api.ApplicationVariant
//import com.android.build.gradle.internal.api.ApplicationVariantImpl
//import com.android.build.gradle.internal.variant.ApplicationVariantData
//import com.android.build.gradle.internal.variant.BaseVariantData
//import com.android.builder.symbols.SymbolUtils
//import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.AarDependenceInfo
//import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.DependenceInfo
//import org.gradle.api.Project
//
///**
// * @author CaMnter
// */
//
//class ReduceDependencyPackagingExtension {
//
//    /** Custom defined resource package Id **/
//    int packageId
//    /** Local host application directory or Jenkins build number, fetch config files from here **/
//    String targetHost
//    /** Apply Host Proguard Mapping or not**/
//    boolean applyHostMapping = true
//    /** Exclude dependent aar or jar **/
//    Collection<String> excludes = new HashSet<>()
//
//    /**  host Symbol file - Host_R.txt */
//    File hostSymbolFile
//    /**  host dependence file - version.txt*/
//    File hostDependenceFile
//
//    BaseVariantData variantData
//
//    Collection<DependenceInfo> stripDependencies = []
//    Collection<AarDependenceInfo> retainedAarLibs = []
//
//    /** Variant application id */
//    String packageName
//
//    /** Package path for java classes */
//    String packagePath
//
//    static String getApplicationId(Project project, ApplicationVariant variant) {
//        String applicationId = variant.applicationId
//        printf "%-57s = %s\n",
//                ['[ReduceDependencyPackagingPlugin]   [applicationId]', applicationId]
//
//        final ApplicationVariantData variantData = (variant as ApplicationVariantImpl).variantData
//        def manifestFile = variantData?.getVariantConfiguration()?.getMainManifest()
//        if (manifestFile == null || !manifestFile.exists()) {
//            manifestFile = project.file("src/main/AndroidManifest.xml")
//        }
//        printf "%-57s = %s\n",
//                ['[ReduceDependencyPackagingPlugin]   [manifestFile]', "${manifestFile.path}   [exists] = ${manifestFile.exists()}"]
//
//        if (manifestFile.exists()) {
//            def packageName
//            try {
//                packageName = SymbolUtils.getPackageNameFromManifest(manifestFile)
//                if (packageName == null || packageName.empty) {
//                    packageName = parsePackageNameFromManifest(manifestFile)
//                    applicationId = packageName
//                }
//            } catch (Exception e) {
//                packageName = parsePackageNameFromManifest(manifestFile)
//                applicationId = packageName
//            }
//        }
//        printf "%-57s = %s\n\n",
//                ['[ReduceDependencyPackagingPlugin]   [applicationId]', applicationId]
//        return applicationId
//    }
//
//    static String parsePackageNameFromManifest(File manifestFile) {
//        def packageName = ''
//        def parsedManifest = new XmlParser().parse(
//                new InputStreamReader(new FileInputStream(manifestFile), "utf-8"))
//        if (parsedManifest != null) {
//            packageName = parsedManifest.attribute("package")
//        }
//        return packageName
//    }
//}