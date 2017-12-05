package com.camnter.gradle.plugin.dex.method.counts

import com.android.repository.Revision
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * @author CaMnter
 */

class DexMethodCountsPlugin implements Plugin<Project> {

    static final String VERSION_3_ZERO_FIELD = "com.android.builder.Version"
    // <= 3.0
    static final String VERSION_3_ONE_FIELD = "com.android.builder.model.Version"
    // > 3.1
    static final String AGP_VERSION_FIELD = "ANDROID_GRADLE_PLUGIN_VERSION"

    def gradlePluginVersion

    def gradlePluginRevision
    def threeOhRevision = Revision.parseRevision("3.0.0")
    def isBuildTools3

    @Override
    void apply(Project project) {
        initVersion()
    }

    def initVersion() {
        Exception exception
        try {
            gradlePluginVersion = Class.forName(VERSION_3_ZERO_FIELD).
                    getDeclaredField(AGP_VERSION_FIELD).
                    get(this).
                    toString()
        } catch (Exception e) {
            exception = e
        }
        try {
            gradlePluginVersion = Class.forName(VERSION_3_ONE_FIELD).
                    getDeclaredField(AGP_VERSION_FIELD).
                    get(this).
                    toString()
        } catch (Exception e) {
            exception = e
        }
        if (gradlePluginVersion == null && exception != null) {
            throw IllegalStateException(
                    "dex-method-counts-plugin requires the Android plugin to be configured",
                    exception)
        } else if (gradlePluginVersion == null) {
            throw IllegalStateException(
                    "dex-method-counts-plugin requires the Android plugin to be configured")
        }
        gradlePluginRevision =
                Revision.parseRevision(gradlePluginVersion, Revision.Precision.PREVIEW)
        isBuildTools3 = gradlePluginRevision.compareTo(threeOhRevision,
                Revision.PreviewComparison.IGNORE) >= 0
    }

}