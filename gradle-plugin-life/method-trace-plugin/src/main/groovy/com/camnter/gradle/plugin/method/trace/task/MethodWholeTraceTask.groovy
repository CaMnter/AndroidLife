package com.camnter.gradle.plugin.method.trace.task

import com.android.build.gradle.AppExtension
import com.camnter.gradle.plugin.method.trace.utils.CommandUtils
import com.camnter.gradle.plugin.method.trace.utils.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * @author CaMnter
 */

class MethodWholeTraceTask extends DefaultTask {

    @TaskAction
    void main() {
        println '[MethodWholeTraceTask]   [main]'
        def capturesDirFile = new File(
                project.getRootProject().projectDir.path + FileUtils.FILE_SEPARATOR + 'captures')
        capturesDirFile.traverse {
            def fileName = it.name
            if (it.isDirectory() || !fileName.endsWith('.trace')) return

            // In order for DmTraceDump tool to write the content
            def txtFile = new File(capturesDirFile.path, fileName.replace('trace', 'txt'))
            txtFile.write('')

            // DmTraceDump command

            // The space in the path is processed only when the command line is executed
            def input = it.absolutePath.replaceAll(" ", "\\\\ ")
            def output = txtFile.absolutePath.replaceAll(" ", "\\\\ ")

            def command = "${platformToolsPath}/dmtracedump  -ho ${input} >> ${output}"
            printf "%43s : %s\n", ['[MethodWholeTraceTask]   [main]   [command]', command]

            CommandUtils.command(command)
        }
    }

    def getPlatformToolsPath() {
        def localPropertiesFile = new File(project.rootDir, 'local.properties')

        def sdkPath = null
        if (localPropertiesFile.exists()) {
            def properties = new Properties()
            localPropertiesFile.withInputStream { inputStream -> properties.load(inputStream) }
            sdkPath = properties.getProperty('sdk.dir')
        }
        if (null == sdkPath || sdkPath == '') {
            AppExtension appExtension = project.extensions.getByType(AppExtension.class)
            sdkPath = appExtension.getSdkDirectory().getAbsolutePath()

            if (null == sdkPath || sdkPath == '') {
                sdkPath = appExtension.plugin.getSdkFolder().getAbsolutePath()
            }
        }

        def platformToolsDirFile = new File(
                sdkPath + File.separator + 'platform-tools' + File.separator)
        if (platformToolsDirFile.exists()) {
            return platformToolsDirFile.path
        }

        return ''
    }
}