package com.camnter.gradle.plugin.life

import org.gradle.api.Plugin
import org.gradle.api.Project

class LifePluginExtension {
    def id = "[CaMnter]"
    def save = "[Save you from anything]"
}

class LifePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('lifePlugin') << {
            println "[LifePlugin]   [apply]   [Save you from anything]"
        }

        project.task('lifeTask', type: LifeTask)

        project.extensions.create("lifeExtension", LifePluginExtension)
        project.task('lifeExtensionTask', type: LifeExtensionTask)
    }
}