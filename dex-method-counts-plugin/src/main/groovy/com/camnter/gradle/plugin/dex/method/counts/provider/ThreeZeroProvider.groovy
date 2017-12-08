package com.camnter.gradle.plugin.dex.method.counts.provider

import com.android.build.gradle.api.*
import com.camnter.gradle.plugin.dex.method.counts.task.BaseDexMethodCountsTask
import org.gradle.api.Project

class ThreeZeroProvider extends BaseProvider {

    ThreeZeroProvider(Project project) {
        super(project)
    }

    @Override
    def applyToApkVariant(ApkVariant variant) {
        variant.outputs.all {
            if (it instanceof ApkVariantOutput) {
                def taskName = createTaskName(variant)
                def outputDir = createOutputDir(variant, it)
                def dexMethodCountsTask = project.task(type: BaseDexMethodCountsTask,
                        overwrite: true, taskName) { BaseDexMethodCountsTask task ->
                    task.fileToCount = it.outputFile
                    task.outputDir = outputDir
                    task.variant = variant
                    task.variantOutput = it
                }
                addDexCountTaskToGraph(it.packageApplication, dexMethodCountsTask)
            } else {
                throw IllegalArgumentException(
                        "[DexMethodCountsPlugin]   Unexpected output type for variant ${variant.name}: ${it.class.name}")
            }
        }
    }

    @Override
    def applyToTestVariant(TestVariant variant) {
        applyToApkVariant(variant)
    }

    @Override
    def applyToLibraryVariant(LibraryVariant variant) {
        def packageLibraryTask = variant.packageLibrary
        def dexMethodCountsTask = project.task(type: BaseDexMethodCountsTask, overwrite: true,
                "dexMethodCounts${variant.name.capitalize()}") { BaseDexMethodCountsTask task ->
            task.fileToCount = packageLibraryTask.archivePath
            task.variant = variant
            task.variantOutput = null
        }
        addDexCountTaskToGraph(packageLibraryTask, dexMethodCountsTask)
    }

    @Override
    def applyToFeatureVariant(FeatureVariant variant) {
        applyToApkVariant(variant)
    }

    @Override
    def applyToApplicationVariant(ApplicationVariant variant) {
        applyToApkVariant(variant)
    }
}