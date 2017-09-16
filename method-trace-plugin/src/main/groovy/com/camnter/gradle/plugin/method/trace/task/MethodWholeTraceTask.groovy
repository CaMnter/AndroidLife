package com.camnter.gradle.plugin.method.trace.task

import com.android.build.gradle.AppExtension
import com.camnter.gradle.plugin.method.trace.utils.FileUtils
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task

/**
 * @author CaMnter
 */

class MethodWholeTraceTask extends DefaultTask {

    @Override
    Task doLast(Action<? super Task> action) {
        def capturesDirFile = new File(
                project.getRootProject().projectDir.path + FileUtils.FILE_SEPARATOR + 'captures')
        capturesDirFile.traverse {
            def fileName = it.name
            if (it.isDirectory() || !fileName.endsWith('.trace')) return

            // In order for DmTraceDump tool to write the content
            def txtFile = new File(capturesDirFile.path, fileName.replace('trace', 'txt'))
            txtFile.write('')

            // DmTraceDump command
            def command = "${platformToolsPath}dmtracedump  -ho ${it.absolutePath} >> ${txtFile.absolutePath}"
            println "[WholeMethodTraceTask]   [doLast]   [command] == ${command}"

            def osName = System.getProperty("os.name").toLowerCase()

            if (osName.contains('windows')) {
                // windows
                ("cmd /c start  /b ${command}").execute()
            } else {
                // linux | osX
                ['bash', '-c', command].execute()
            }
        }
        return super.doLast(action)
    }

    def getPlatformToolsPath() {
        def localPropertiesFile = new File(project.rootDir, 'local.properties')

        def sdkPath = null
        if (localProperties.exists()) {
            def properties = new Properties()
            localPropertiesFile.withInputStream { inputStream -> properties.load(inputStream)
            }
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