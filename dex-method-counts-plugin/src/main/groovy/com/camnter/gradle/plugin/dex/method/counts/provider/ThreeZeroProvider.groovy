package com.camnter.gradle.plugin.dex.method.counts.provider

import com.android.build.gradle.api.*
import org.gradle.api.Project

class ThreeZeroProvider extends BaseProvider {

    ThreeZeroProvider(Project project) {
        super(project)
    }

    @Override
    def applyToApkVariant(ApkVariant variant) {
        variant.outputs.all {
            if (it instanceof ApkVariantOutput) {
                // TODO output.outputFile
                // TODO 创建任务
                // TODO addDexcountTaskToGraph(output.packageApplication, task)
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
        // TODO 创建任务
        // TODO addDexcountTaskToGraph(output.packageApplication, task)
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