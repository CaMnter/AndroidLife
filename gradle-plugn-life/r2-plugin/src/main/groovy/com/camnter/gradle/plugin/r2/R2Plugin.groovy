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

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.ProcessAndroidResources
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

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
                configureR2Generation(target, featureExtension.featureVariants)
                configureR2Generation(target, featureExtension.libraryVariants)
            } else if (it instanceof AppPlugin) {
                AppExtension appExtension = target.extensions.getByType(AppExtension.class)
                configureR2Generation(target, appExtension.applicationVariants)
            } else if (it instanceof LibraryPlugin) {
                LibraryExtension libraryExtension = target.extensions.getByType(
                        LibraryExtension.class)
                configureR2Generation(target, libraryExtension.libraryVariants)
            }
        }
    }

    private void configureR2Generation(Project project, DomainObjectSet<BaseVariant> variants) {
        println "[R2Plugin]   [applyPlugin]"
        // 遍历 DomainObjectSet<out BaseVariant>
        variants.all { variant ->
            // 获取每个 output 文件夹 File
            File outputDir = project.buildDir.resolve("generated/source/r2/${variant.dirName}")
            // 创建对应的 R2 任务
            Task task = project.tasks.create("generate${variant.name.capitalize()}R2")
            // 设置 R2 任务的 输出目录 File
            task.outputs.dir(outputDir)
            // 注册任务
            variant.registerJavaGeneratingTask(task, outputDir)

            AtomicBoolean once = AtomicBoolean()

            // 遍历 DomainObjectCollection<BaseVariantOutput>
            variant.outputs.all { output ->

                // ProcessAndroidResources
                ProcessAndroidResources processResources = output.processResources
                // ProcessAndroidResources 添加到任务内
                task.dependsOn(processResources)

                // Though there might be multiple outputs, their R files are all the same. Thus, we only
                // need to configure the task once with the R.java input and action.
                if (once.compareAndSet(false, true)) {
                    // 拿到 R 文件夹路径
                    String rPackage = processResources.packageForR
                    // 替换 R 文件夹路径 的 分隔符
                    String pathToR = rPackage.replace('.', FileUtils.separatorChar)
                    // 拿到 R 文件
                    File rFile = new File(FileUtils.resolve(
                            new File(FileUtils.resolve(processResources.sourceOutputDir, pathToR)),
                            "R.java"))
                    task.apply {
                        // 注册 R File 到任务内
                        inputs.file(rFile)
                        // JavaPoet + JavaParser 生成 R2.java
                        doLast {
                            R2ClassBuilder.brewJava(rFile, outputDir, rPackage, "R2")
                        }
                    }
                }
            }
        }
    }
}