package com.camnter.gradle.plugin.r2

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.ProcessAndroidResources
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author CaMnter
 */

class R2Plugin implements Plugin<Project> {

    /**
     * Apply this plugin to the given target object.
     *
     * @param target The target object
     */
    @Override
    void apply(Project target) {
        println "[R2Plugin]   [apply]"
        target.plugins.all {
            if (it instanceof AppPlugin) {
                def AppExtension appExtension = target.extensions.getByType(AppExtension.class)
                applyPlugin(appExtension.applicationVariants)
            } else if (it instanceof LibraryPlugin) {
                def LibraryExtension libraryExtension = target.extensions.getByType(
                        LibraryExtension.class)
                applyPlugin(libraryExtension.libraryVariants)
            }
        }
    }

    private void applyPlugin(DomainObjectSet<BaseVariant> variants) {
        println "[R2Plugin]   [applyPlugin]"
        variants.all { variant ->
            // 遍历 List<BaseVariantOutput>
            variant.outputs.forEach { output ->
                // ProcessAndroidResources
                def ProcessAndroidResources processResources = output.processResources
                if (processResources != null) {
                    // TODO proper task registered as source-generating?
                    // 在 processResources 阶段添加任务
                    processResources.doLast {
                        // 拿到 R 文件路径，替换分隔符
                        println "[R2Plugin]   [applyPlugin]   [packageForR]    [before] = ${processResources.packageForR}"
                        if (processResources.packageForR == null) {
                            return
                        }
                        def String pathToR = processResources.packageForR.replace(".",
                                FileUtils.FILE_SEPARATOR)
                        println "[R2Plugin]   [applyPlugin]   [packageForR]    [after]  = ${pathToR}"
                        // 拿到 R 文件
                        def File rFile = new File(FileUtils.resolve(new File(
                                FileUtils.resolve(processResources.sourceOutputDir, pathToR)),
                                "R.java"))
                        // JavaPoet + JavaParser 生成 R2.java
                        println "[R2Plugin]   [applyPlugin]   [rFile] = ${rFile.path}"
                        R2ClassBuilder.brewJava(rFile, processResources.sourceOutputDir,
                                processResources.packageForR, "R2")
                    }
                }
            }
        }
    }
}