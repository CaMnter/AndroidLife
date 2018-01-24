package com.camnter.gradle.plugin.method.trace.utils

import com.android.build.gradle.*
import org.gradle.api.Project

/**
 * @author CaMnter
 */

class PluginUtils {

    static int dispatchPlugin(Project project, Closure<AppExtension> appClosure,
            Closure<LibraryExtension> libraryClosure,
            Closure<FeatureExtension> featureClosure) {
        project.plugins.all {
            if (it instanceof AppPlugin) {
                AppExtension appExtension = project.extensions.getByType(AppExtension.class)
                appClosure.call(appExtension)
            } else if (it instanceof LibraryPlugin) {
                LibraryExtension libraryExtension = project.extensions.getByType(
                        LibraryExtension.class)
                libraryClosure.call(libraryExtension)
            } else if (it instanceof FeaturePlugin) {
                FeatureExtension featureExtension = project.extensions.getByType(
                        FeatureExtension.class)
                featureClosure.call(featureExtension)
            }
        }
    }

    static void dispatchSystem(Closure osXClosure,
            Closure windowClosure) {
        def osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains('windows')) {
            // windows
            windowClosure.call()
        } else {
            // osX | linux
            osXClosure.call()
        }
    }
}