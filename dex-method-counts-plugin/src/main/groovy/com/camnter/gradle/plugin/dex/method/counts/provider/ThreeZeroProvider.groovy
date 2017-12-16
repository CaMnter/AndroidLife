package com.camnter.gradle.plugin.dex.method.counts.provider

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.internal.api.*
import com.camnter.gradle.plugin.dex.method.counts.task.DexMethodCountsTask
import org.gradle.api.Project

/**
 * @author CaMnter
 */

class ThreeZeroProvider extends BaseProvider {

    ThreeZeroProvider(Project project) {
        super(project)
    }

    @Override
    def applyToApkVariant(ApkVariantImpl variant) {
        variant.outputs.all {
            if (it instanceof ApkVariantOutput) {
                def taskName = createTaskName(variant, it)
                def outputDir = createOutputDir(variant, it)
                def dexMethodCountsTask = project.task(type: DexMethodCountsTask,
                        overwrite: true, taskName) { DexMethodCountsTask task ->
                    task.fileToCount = it.outputFile
                    task.outputDir = outputDir
                    task.variant = variant
                    task.variantOutput = it
                    task.isBuildTools3 = true
                }
                addDexCountTaskToGraph(it.packageApplication, dexMethodCountsTask)
            } else {
                throw IllegalArgumentException(
                        "[DexMethodCountsPlugin]   Unexpected output type for variant ${variant.name}: ${it.class.name}")
            }
        }
    }

    @Override
    def applyToTestVariant(TestVariantImpl variant) {
        applyToApkVariant(variant)
    }

    @Override
    def applyToLibraryVariant(LibraryVariantImpl variant) {
        def taskName = createTaskName(variant, null)
        def outputDir = createOutputDir(variant, null)
        def packageLibraryTask = variant.packageLibrary
        def dexMethodCountsTask = project.task(type: DexMethodCountsTask, overwrite: true,
                taskName) { DexMethodCountsTask task ->
            task.fileToCount = packageLibraryTask.archivePath
            task.outputDir = outputDir
            task.variant = variant
            task.variantOutput = null
            task.isBuildTools3 = true
        }
        addDexCountTaskToGraph(packageLibraryTask, dexMethodCountsTask)
    }

    @Override
    def applyToFeatureVariant(FeatureVariantImpl variant) {
        applyToApkVariant(variant)
    }

    @Override
    def applyToApplicationVariant(ApplicationVariantImpl variant) {
        applyToApkVariant(variant)
    }
}