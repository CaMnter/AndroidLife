package com.camnter.gradle.plugin.dex.method.counts

import org.gradle.api.DefaultTask
import org.gradle.api.java.archives.Attributes
import org.gradle.api.tasks.TaskAction

/**
 * @author CaMnter
 */

class BeforeJarTask extends DefaultTask {

    static def MAIN_CLASS_NAME = 'com.camnter.gradle.plugin.dex.method.counts.jar.Main'
    static def JAR_OUTPUT_FILE = 'build/output/dex-method-counts-plugin'

    @TaskAction
    void main() {
        project.jar {
            // Redirect output to match launcher script
            destinationDir = file(JAR_OUTPUT_FILE)
            manifest {
                Attributes("Main-Class": MAIN_CLASS_NAME)
            }
        }
    }

}