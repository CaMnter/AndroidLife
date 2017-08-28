package com.camnter.gradle.plugin.life

import org.gradle.api.Plugin
import org.gradle.api.Project

class LifePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('camnterLifeTask') << {
            println "[LifePlugin]   [println]   Save you from anything"
        }
    }

}