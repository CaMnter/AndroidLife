package com.camnter.gradle.plugin.r2

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
        PluginUtils.dispatchPlugin(target) {
            configureR2Generation(target, it.applicationVariants)
        } {
            configureR2Generation(target, it.libraryVariants)
        } {
            configureR2Generation(target, it.featureVariants)
            configureR2Generation(target, it.libraryVariants)
        }
    }

    private static void configureR2Generation(Project project,
            DomainObjectSet<BaseVariant> variants) {
        println "[R2Plugin]   [applyPlugin]"
        // 遍历 DomainObjectSet<out BaseVariant>
        if (variants == null || variants.size() == 0) return
        variants.all { variant ->
            // 获取每个 output 文件夹 File
            File outputDir = new File(
                    FileUtils.resolve(project.buildDir, "generated/source/r2/${variant.dirName}"))
            // 创建对应的 R2 任务
            Task task = project.tasks.create("generate${variant.name.capitalize()}R2")
            // 设置 R2 任务的 输出目录 File
            task.outputs.dir(outputDir)
            // 注册任务
            variant.registerJavaGeneratingTask(task, outputDir)

            AtomicBoolean once = new AtomicBoolean()

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
                    String pathToR = rPackage.replace('.', FileUtils.FILE_SEPARATOR)
                    // 拿到 R 文件
                    File rFile = new File(FileUtils.resolve(
                            new File(FileUtils.resolve(processResources.sourceOutputDir, pathToR)),
                            "R.java"))

                    // 注册 R File 到任务内
                    task.inputs.file(rFile)
                    task.doLast {
                        R2ClassBuilder.brewJava(rFile, outputDir, rPackage, "R2")
                    }
                }
            }
        }
    }
}