package com.camnter.gradle.plugin.dex.method.counts.provider

import com.android.build.gradle.internal.api.*
import com.camnter.gradle.plugin.dex.method.counts.task.BaseDexMethodCountsTask
import org.gradle.api.Project

class LessThanThreeZeroProvider extends BaseProvider {

    LessThanThreeZeroProvider(Project project) {
        super(project)
    }

    @Override
    def applyToApkVariant(ApkVariantImpl variant) {
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
    def applyToTestVariant(TestVariantImpl variant) {
        applyToApkVariant(variant)
    }

    @Override
    def applyToLibraryVariant(LibraryVariantImpl variant) {
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
    def applyToFeatureVariant(FeatureVariantImpl variant) {
        applyToApkVariant(variant)
    }

    @Override
    def applyToApplicationVariant(ApplicationVariantImpl variant) {
        applyToApkVariant(variant)
    }
}