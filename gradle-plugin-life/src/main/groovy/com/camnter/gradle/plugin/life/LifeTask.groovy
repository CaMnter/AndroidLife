package com.camnter.gradle.plugin.life

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class LifeTask extends DefaultTask {

    @TaskAction
    void output() {
        println "[LifeTask]   [output]   [Save you from anything]";
    }
}