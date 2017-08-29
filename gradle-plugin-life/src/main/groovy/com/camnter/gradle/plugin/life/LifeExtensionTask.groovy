package com.camnter.gradle.plugin.life

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class LifeExtensionTask extends DefaultTask {

    @TaskAction
    void output() {
        println "[LifeExtensionTask]   [output]   \n" + "[id]   ${project.lifeExtension.id}\n" +
                "[save]   ${project.lifeExtension.save}";
    }
}