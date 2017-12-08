package com.camnter.gradle.plugin.dex.method.counts.provider

import com.android.build.gradle.api.*
import com.camnter.gradle.plugin.dex.method.counts.task.BaseDexMethodCountsTask
import org.gradle.api.Project

class LessThanThreeZeroProvider extends BaseProvider {

    LessThanThreeZeroProvider(Project project) {
        super(project)
    }

    @Override
    def applyToApkVariant(ApkVariant variant) {
        getOutputs(variant).each {
            def taskName = createTaskName(variant)
            def outputDir = createOutputDir(variant, it)
            def dexMethodCountsTask = project.task(type: BaseDexMethodCountsTask,
                    overwrite: true, taskName) { BaseDexMethodCountsTask task ->
                task.fileToCount = it.outputFile
                task.outputDir = outputDir
                task.variant = variant
                task.variantOutput = it
            }
            addDexCountTaskToGraph(it.assemble, dexMethodCountsTask)
        }
    }

    @Override
    def applyToTestVariant(TestVariant variant) {
        applyToApkVariant(variant)
    }

    @Override
    def applyToLibraryVariant(LibraryVariant variant) {
        getOutputs(variant).each {
            def taskName = createTaskName(variant)
            def outputDir = createOutputDir(variant, it)
            def dexMethodCountsTask = project.task(type: BaseDexMethodCountsTask,
                    overwrite: true, taskName) { BaseDexMethodCountsTask task ->
                task.fileToCount = it.outputFile
                task.outputDir = outputDir
                task.variant = variant
                task.variantOutput = it
            }
            addDexCountTaskToGraph(it.assemble, dexMethodCountsTask)
        }
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