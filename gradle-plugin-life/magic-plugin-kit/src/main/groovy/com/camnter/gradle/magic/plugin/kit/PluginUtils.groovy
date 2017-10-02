package com.camnter.gradle.magic.plugin.kit

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
            Closure linuxClosure,
            Closure windowClosure) {
        String system = System.getProperty("os.name")
        switch (system) {
            case "Mac OS X":
                osXClosure.call()
                break
            case "Linux":
                linuxClosure.call()
                break
            case "Windows":
                windowClosure.call()
                break
            default:
                return
        }
    }
}