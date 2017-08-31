package com.camnter.gradle.plugin.life

import org.gradle.api.Plugin
import org.gradle.api.Project

public class LifePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('lifePlugin') << {
            println "[LifePlugin]   [apply]   [Save you from anything]"
        }

        project.task('lifeTask', type: LifeTask)

        project.extensions.create('lifeExtension', LifePluginExtension)
        project.lifeExtension.extensions.create('nestLifeExtension', NestLifePluginExtension)
        project.task('lifeExtensionTask', type: LifeExtensionTask)
    }
}
