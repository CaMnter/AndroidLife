package com.camnter.gradle.plugin.dex.method.counts

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * @author CaMnter
 */

class AfterJarTask extends DefaultTask {

    @TaskAction
    void main() {
        def jarOutput = "${project.projectDir}/${BeforeJarTask.JAR_OUTPUT_FILE}"
        // TODO 复制脚本，修改脚本
    }
}