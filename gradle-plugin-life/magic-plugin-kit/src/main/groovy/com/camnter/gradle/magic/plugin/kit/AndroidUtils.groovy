package com.camnter.gradle.magic.plugin.kit

import com.android.build.gradle.AppExtension
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project

/**
 * @author CaMnter
 */

class AndroidUtils {

    static int getMinSdkVersion(Project project) {
        def minSdkVersion = 0
        PluginUtils.dispatchPlugin(project) { AppExtension appExtension ->
            minSdkVersion = appExtension.defaultConfig.minSdkVersion.apiLevel
        } { LibraryExtension libraryExtension ->
            minSdkVersion = libraryExtension.defaultConfig.minSdkVersion.apiLevel
        } { FeatureExtension featureExtension ->
            minSdkVersion = featureExtension.defaultConfig.minSdkVersion.apiLevel
        }
        return minSdkVersion
    }
}