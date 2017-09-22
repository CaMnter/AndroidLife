package com.camnter.gradle.plugin.method.trace.task

import com.camnter.gradle.plugin.method.trace.utils.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * @author CaMnter
 */

class MethodExpectTraceTask extends DefaultTask {

    /**
     * ent: 进入函数
     * xit: 退出函数
     **/
    @TaskAction
    void main() {
        println '[MethodExpectTraceTask]   [main]'
        def packageName = ''
        if (project.hasProperty('packageName')) {
            packageName = project.getProject('packageName')
        }

        packageName = project.methodTraceExtension.packageName
        if ('' == packageName || null == packageName) {
            project.logger.error "[MethodExpectTraceTask]  Task failure, because packageName is required"
            return
        }

        def packageNameSignature = packageName.replaceAll('[.]', '/')
        def capturesDirFile = new File(
                project.getRootProject().projectDir.path + FileUtils.FILE_SEPARATOR + 'captures')
        capturesDirFile.traverse {
            def fileName = it.name
            /**
             * file
             * .txt 结尾
             * 非 _enter.txt 结尾
             * 非 _enterExit 结尾
             * */
            if (it.isFile() && fileName.endsWith('.txt') &&
                    !fileName.endsWith('_enter.txt') &&
                    !fileName.endsWith('_enterExit.txt')) {
                printf "%-60s  =  %s",
                        ["[MethodExpectTraceTask]   [main]   [fileName]", "${fileName}\n"]
                def expectEnterTxtName = fileName.replace('.txt', '_enter.txt')
                def expectEnterExitTextName = fileName.replace('.txt', '_enterExit.txt')

                def expectEnterFile = new File(capturesDirFile, expectEnterTxtName)
                def expectEnterExitFile = new File(capturesDirFile, expectEnterExitTextName)
                expectEnterFile.write('')
                expectEnterExitFile.write('')

                Stack<String> entTrace = new Stack<>()
                it.eachLine { line ->
                    /**
                     * 输出 ent
                     * 有的 traceview  包含 方法签名
                     * 有的 traceview  包含 package name
                     * */
                    if (line.contains(' ent ') && (line.contains(packageName) || line.contains(
                            packageNameSignature))) {
                        entTrace.push(line)
                        expectEnterFile.append("${line}\n")
                        expectEnterExitFile.append("${line}\n")
                    }
                    /**
                     * xit
                     * 有的 traceview  包含 方法签名
                     * 有的 traceview  包含 package name
                     * */
                    if (line.contains(' xit ') && (line.contains(packageName) || line.contains(
                            packageNameSignature))) {
                        expectEnterExitFile.append("${line}\n")
                        def entLine = entTrace.peek()
                        if (line.substring(line.indexOf('.')) == entLine.substring(
                                entLine.indexOf('.'))) {
                            entTrace.pop()
                            def xitTime = getTimeFromTraceInfo(TraceType.Xit, line)
                            def entTime = getTimeFromTraceInfo(TraceType.Ent, entLine)
                            def elapsedTime = xitTime - entTime
                            def elapsedTimeString = elapsedTime as String
                            expectEnterExitFile.append("${'diff ---  ' + elapsedTimeString}\n")
                        }
                    }
                }
                printf "%60s  =  %s",
                        ["[MethodExpectTraceTask]   [main]   [expectEnterFile]", "${expectEnterFile.path}\n"]
                printf "%60s  =  %s",
                        ["[MethodExpectTraceTask]   [main]   [expectEnterExitFile]", "${expectEnterExitFile.path}\n"]
            }
        }
    }

    long getTimeFromTraceInfo(TraceType traceType, String trace) {
        def firstPointIndex = trace.indexOf('.')
        def firstHalfContent = trace.substring(0, firstPointIndex).trim()
        def time = firstHalfContent.substring(
                firstHalfContent.indexOf(traceType.value) + traceType.value.length())
        if (time.isLong()) {
            return time as Long
        }
        return 0
    }

    enum TraceType {
        Ent('ent'),
        Xit('xit')

        private final String value

        private TraceType(String value) {
            this.value = value
        }

        String toString() {
            return this.value
        }
    }
}