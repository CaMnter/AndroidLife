package com.camnter.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *  Description：FixPlugin
 *  Created by：CaMnter
 *  */
public class FixPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.logger.error "[ FixPlugin ] # [ apply begin ]"
        def android = project.extensions.findByType(AppExtension)
        android.registerTransform(new PreDexTransform(project))
        project.logger.error "[ FixPlugin ] # [ android ]"
        project.logger.error "[ FixPlugin ] # [ apply end ]"
    }
}