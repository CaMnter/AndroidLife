package com.camnter.gradle.plugin.dex.method.counts

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.FeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.lang.reflect.Method

/**
 * @author CaMnter
 */

class DexMethodCountsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.all {
            if (it instanceof FeaturePlugin) {
                FeatureExtension featureExtension = project.extensions.getByType(
                        FeatureExtension.class)
                applyVariant(project, featureExtension.featureVariants)
            } else if (it instanceof AppPlugin) {
                AppExtension appExtension = project.extensions.getByType(AppExtension.class)
                applyVariant(project, appExtension.applicationVariants)
            } else if (it instanceof LibraryPlugin) {
                LibraryExtension libraryExtension = project.extensions.getByType(
                        LibraryExtension.class)
                applyVariant(project, libraryExtension.libraryVariants)
            }
        }
    }

    private Collection<BaseVariantOutput> applyVariant(Project project,
            DomainObjectSet<BaseVariant> variants) {
        variants.all {
            Collection<BaseVariantOutput> outputs = getOutputs(it)
            outputs.each {
                // TODO create Task
                // TODO it.outputFile File
                // TODO it.assemble Task
                // TODO it.name String
                println "[DexMethodCountsPlugin]   [name] = ${it.name}  [outputFile] = ${it.outputFile}   [assemble] = ${it.assemble}   [baseName] = ${it.baseName}   [dirName] = ${it.dirName}"
            }
        }
    }

    private static Collection<BaseVariantOutput> getOutputs(BaseVariant variant) {
        Method getOutputs = BaseVariant.class.getMethod("getOutputs")
        getOutputs.setAccessible(true)
        return getOutputs.invoke(variant) as Collection<BaseVariantOutput>
    }
}