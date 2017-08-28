package com.camnter.gradle.plugin.life

import org.gradle.api.Plugin
import org.gradle.api.Project

class LifePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('lifePlugin') << {
            println "[LifePlugin]   [apply]   [Save you from anything]";
        }
        project.task('lifeTask', type: LifeTask);
    }
}