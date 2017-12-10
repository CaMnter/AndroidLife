package com.camnter.gradle.plugin.dex.method.counts.provider

import com.android.build.gradle.*
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.api.*
import com.camnter.gradle.plugin.dex.method.counts.task.BaseDexMethodCountsTask
import com.camnter.gradle.plugin.dex.method.counts.utils.FileUtils
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginContainer

import java.lang.reflect.Method

abstract class BaseProvider {

    protected final Project project

    BaseProvider(Project project) {
        this.project = project
    }

    def apply() {
        if (isInstantRun(project)) {
            project.logger.info(
                    "[DexMethodCountsPlugin]   Instant Run detected; disabling dex-method-counts-plugin")
            return
        }

        DomainObjectCollection<BaseVariant> variants
        PluginContainer plugins = project.plugins
        ExtensionContainer extensions = project.extensions
        if (plugins.hasPlugin(AppPlugin.class)) {
            AppExtension appExtension = extensions.getByType(AppExtension.class)
            variants = appExtension.applicationVariants
        } else if (plugins.hasPlugin(TestPlugin.class)) {
            TestExtension testExtension = extensions.getByType(TestPlugin.class)
            variants = testExtension.applicationVariants
        } else if (plugins.hasPlugin(LibraryPlugin.class)) {
            LibraryExtension libraryExtension = extensions.getByType(LibraryExtension.class)
            variants = libraryExtension.libraryVariants
        } else if (plugins.hasPlugin(FeaturePlugin.class)) {
            FeatureExtension featureExtension = extensions.getByType(FeatureExtension.class)
            variants = featureExtension.featureVariants
        } else {
            throw IllegalArgumentException(
                    "[DexMethodCountsPlugin]   dex-method-counts-plugin requires the Android plugin to be configured")
        }

        variants.all { BaseVariant variant ->
            if (variant instanceof TestVariantImpl) {
                applyToTestVariant(variant)
            } else if (variant instanceof LibraryVariantImpl) {
                applyToLibraryVariant(variant)
            } else if (variant instanceof FeatureVariantImpl) {
                applyToFeatureVariant(variant)
            } else if (variant instanceof ApplicationVariantImpl) {
                applyToApplicationVariant(variant)
            }
        }
    }

    static def isInstantRun(Project project) {
        def optionString = project.getProperties().get("android.optional.compilation") as String
        if (optionString == null) return false
        def isInstantRun = false
        optionString.split(",").each {
            if (it.trim() == "INSTANT_DEV") {
                isInstantRun = true
            }
        }
        return isInstantRun
    }

    protected Collection<BaseVariantOutput> getOutputs(BaseVariant variant) {
        Method getOutputs = BaseVariant.class.getMethod("getOutputs")
        getOutputs.setAccessible(true)
        return getOutputs.invoke(variant) as Collection<BaseVariantOutput>
    }

    def addDexCountTaskToGraph(Task parentTask, BaseDexMethodCountsTask dexcountTask) {
        dexcountTask.dependsOn(parentTask)
        dexcountTask.mustRunAfter(parentTask)
        parentTask.finalizedBy(dexcountTask)
    }

    def createTaskName(BaseVariant variant) {
        def taskName = "dexMethodCounts${variant.name.capitalize()}"
        if (getOutputs(variant).size > 1) {
            if (output == null) {
                throw AssertionError("[DexMethodCountsPlugin]   Output should never be null here")
            }
            taskName += it.name.capitalize()
        }
        return taskName
    }

    def createOutputDir(BaseVariant variant, ApkVariantOutput output) {
        def outputDir = FileUtils.resolve(project.buildDir,
                "output/dex-method-counts-plugin")
        if (getOutputs(variant).size > 1) {
            if (output == null) {
                throw AssertionError("[DexMethodCountsPlugin]   Output should never be null here")
            }
            outputDir = FileUtils.resolve(outputDir,
                    "${output.name}")
        }
        return outputDir
    }

    def abstract applyToApkVariant(ApkVariantImpl variant)

    def abstract applyToTestVariant(TestVariantImpl variant)

    def abstract applyToLibraryVariant(LibraryVariantImpl variant)

    def abstract applyToFeatureVariant(FeatureVariantImpl variant)

    def abstract applyToApplicationVariant(ApplicationVariantImpl variant)
}