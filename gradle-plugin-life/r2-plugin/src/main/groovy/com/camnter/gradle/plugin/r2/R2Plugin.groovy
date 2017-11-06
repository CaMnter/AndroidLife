package com.camnter.gradle.plugin.r2

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.res.GenerateLibraryRFileTask
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask
import com.android.build.gradle.internal.scope.VariantScope
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import java.lang.reflect.Field
import java.util.concurrent.atomic.AtomicBoolean

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
            if (it instanceof FeaturePlugin) {
                FeatureExtension featureExtension = target.extensions.getByType(
                        FeatureExtension.class)
                configureR2Generation(target, featureExtension.featureVariants, false)
                configureR2Generation(target, featureExtension.libraryVariants, false)
            } else if (it instanceof AppPlugin) {
                AppExtension appExtension = target.extensions.getByType(AppExtension.class)
                configureR2Generation(target, appExtension.applicationVariants, false)
            } else if (it instanceof LibraryPlugin) {
                LibraryExtension libraryExtension = target.extensions.getByType(
                        LibraryExtension.class)
                configureR2Generation(target, libraryExtension.libraryVariants, true)
            }
        }
    }

    private void configureR2Generation(Project project, DomainObjectSet<BaseVariant> variants,
            boolean isLibrary) {
        println "[R2Plugin]   [configureR2Generation]"
        // 遍历 DomainObjectSet<out BaseVariant>
        try {
            variants.all {
                if (null == it) {
                    println "[R2Plugin]   [applyPlugin]   [variants.all]:   it == null"
                    return
                }
                // 获取每个 output 文件夹 File
                File outputDir = new File(
                        FileUtils.resolve(project.buildDir, "generated/source/r2/${it.dirName}"))
                // 创建对应的 R2 任务
                Task task = project.tasks.create("generate${it.name.capitalize()}R2")
                // 设置 R2 任务的 输出目录 File
                task.outputs.dir(outputDir)
                // 注册任务
                it.registerJavaGeneratingTask(task, outputDir)

                AtomicBoolean once = new AtomicBoolean()

                // 遍历 DomainObjectCollection<BaseVariantOutput>
                it.outputs.all { output ->
                    // ProcessAndroidResources
                    def processResources = output.processResources
                    // 拿到 R 文件夹路径
                    def rPackage = isLibrary ?
                            (processResources as GenerateLibraryRFileTask).packageForR :
                            getPackageForRFromLinkApplicationAndroidResourcesTask(
                                    processResources as LinkApplicationAndroidResourcesTask)
                    // ProcessAndroidResources 添加到任务内
                    task.dependsOn(output.processResources)
                    // Though there might be multiple outputs, their R files are all the same. Thus, we only
                    // need to configure the task once with the R.java input and action.
                    if (once.compareAndSet(false, true)) {
                        // 替换 R 文件夹路径 的 分隔符
                        String pathToR = rPackage.replace('.', FileUtils.FILE_SEPARATOR)
                        // 拿到 R 文件
                        File rFile = new File(FileUtils.resolve(new File(
                                FileUtils.resolve(processResources.sourceOutputDir, pathToR)),
                                "R.java"))

                        // 注册 R File 到任务内
                        task.inputs.file(rFile)
                        task.doLast {
                            R2ClassBuilder.brewJava(rFile, outputDir, rPackage, "R2")
                        }
                    }
                }
            }
        } catch (Exception e) {
            println "[R2Plugin]   [configureR2Generation]   ${e.message}"
        }
    }

    /**
     * Refer to Android gradle plugin 3.0.0 source code
     *
     * ProcessResources
     *
     * In 3.1.0 alpha2, renamed to LinkApplicationAndroidResourcesTask
     *
     * @return
     */
    private String getPackageForRFromLinkApplicationAndroidResourcesTask(
            LinkApplicationAndroidResourcesTask linkApplicationAndroidResourcesTask) {

        def variantScope = getVariantScope(linkApplicationAndroidResourcesTask)
        def variantData = variantScope.variantData
        def config = variantData.variantConfiguration
        def splitName = config.splitFromManifest
        def rPackage = splitName == null ? config.originalApplicationId :
                config.originalApplicationId + "." + splitName
        return rPackage
    }

    /**
     * Android gradle plugin 3.1.0 alpha02
     *
     * LinkApplicationAndroidResourcesTask
     *
     * private VariantScope variantScope
     * */
    private VariantScope getVariantScope(
            LinkApplicationAndroidResourcesTask linkApplicationAndroidResourcesTask) {
        Field variantScope = LinkApplicationAndroidResourcesTask.class.getDeclaredField(
                'variantScope')
        variantScope.setAccessible(true)
        def value = variantScope.get(linkApplicationAndroidResourcesTask)
        return value as VariantScope
    }
}
