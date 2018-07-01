package com.camnter.life.asm.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.ApkVariantData
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * @author CaMnter
 */

class LifeAsmPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin.class)) return
        AppExtension android = project.extensions.findByType(AppExtension.class)
        android.applicationVariants.all {
            final ApkVariantData variantData = (it as ApplicationVariantImpl).variantData
            final VariantScope scope = variantData.scope

            // debug or release
            def taskName = scope.getTaskName('lifeAsmTask')
            final Task task = project.task(taskName)

            project.afterEvaluate {
                // TODO transform
            }
        }
    }
}