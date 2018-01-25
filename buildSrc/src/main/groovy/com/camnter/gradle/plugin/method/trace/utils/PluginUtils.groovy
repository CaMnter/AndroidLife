package com.camnter.gradle.plugin.method.trace.utils

import com.android.build.gradle.*
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer

/**
 * @author CaMnter
 */

class PluginUtils {

    static int dispatchPlugin(Project project, Closure<AppExtension> appClosure,
            Closure<LibraryExtension> libraryClosure,
            Closure<FeatureExtension> featureClosure) {
        PluginContainer plugins = project.plugins
        if (plugins.hasPlugin(AppPlugin.class)) {
            AppExtension appExtension = project.extensions.getByType(AppExtension.class)
            appClosure.call(appExtension)
        } else if (plugins.hasPlugin(LibraryPlugin.class)) {
            LibraryExtension libraryExtension = project.extensions.getByType(LibraryExtension.class)
            libraryClosure.call(libraryExtension)
        } else if (plugins.hasPlugin(FeaturePlugin.class)) {
            FeatureExtension featureExtension = project.extensions.getByType(FeatureExtension.class)
            featureClosure.call(featureExtension)
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