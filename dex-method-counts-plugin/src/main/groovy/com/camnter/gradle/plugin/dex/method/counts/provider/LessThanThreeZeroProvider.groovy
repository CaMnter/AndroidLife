package com.camnter.gradle.plugin.dex.method.counts.provider

import com.android.build.gradle.api.*
import org.gradle.api.Project

class LessThanThreeZeroProvider extends BaseProvider {

    LessThanThreeZeroProvider(Project project) {
        super(project)
    }

    @Override
    def applyToApkVariant(ApkVariant variant) {
        getOutputs(variant).each {
            // TODO 创建任务
            // TODO addDexcountTaskToGraph(output.packageApplication, task)
        }
    }

    @Override
    def applyToTestVariant(TestVariant variant) {
        applyToApkVariant(variant)
    }

    @Override
    def applyToLibraryVariant(LibraryVariant variant) {
        getOutputs(variant).each {
            // TODO 创建任务
            // TODO addDexcountTaskToGraph(output.packageApplication, task)
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