package com.camnter.life.asm.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author CaMnter
 */

class LifeAsmPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin.class)) return
        AppExtension android = project.extensions.findByType(AppExtension.class)
        android.applicationVariants.all {
            project.afterEvaluate {
                android.registerTransform(new LifeAsmTransform(project))
            }
        }
    }
}