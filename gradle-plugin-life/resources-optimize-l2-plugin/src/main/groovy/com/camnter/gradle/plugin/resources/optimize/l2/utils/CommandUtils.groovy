package com.camnter.gradle.plugin.resources.optimize.l2.utils

/**
 * @author CaMnter
 */

class CommandUtils {

    static void command(String command, Closure outputClosure, Closure errorClosure) {
        PluginUtils.dispatchSystem {
            commandByOsX(command, outputClosure, errorClosure)
        } {
            commandByOsX(command, outputClosure, errorClosure)
        } {
            commandByWindow(command, outputClosure, errorClosure)
        }
    }

    private static void commandByOsX(String command, Closure outputClosure, Closure errorClosure) {
        try {
            println "[CommandUtils]   [command] = ${command}"
            // ['bash', '-c', command]
            def process = command.execute()
            printCommandInfo(process, outputClosure, errorClosure)
        } catch (Exception e) {
            errorClosure.call(e.message)
        }
    }

    private static void commandByOsXByBash(String command, Closure outputClosure,
            Closure errorClosure) {
        try {
            println "[CommandUtils]   [command] = ${command}"
            def process = ['bash', '-c', command].execute()
            printCommandInfo(process, outputClosure, errorClosure)
        } catch (Exception e) {
            errorClosure.call(e.message)
        }
    }

    private static void commandByWindow(String command, Closure outputClosure,
            Closure errorClosure) {
        try {
            println "[CommandUtils]   [command] = ${command}"
            def process = ("cmd /c start  /b ${command}").execute()
            printCommandInfo(process, outputClosure, errorClosure)
        } catch (Exception e) {
            errorClosure.call(e.message)
        }
    }

    private static void printCommandInfo(Process process, Closure outputClosure,
            Closure errorClosure) {
        def output = new StringBuilder()
        def error = new StringBuilder()
        process.consumeProcessOutput(output, error)
        process.waitFor()
        if ('' != output.toString() && 0 != output.length()) {
            def outputString = output.toString()
            printf "%6s:  %s", ['output', outputString]
            if (outputClosure != null) outputClosure.call(outputString)
        }
        if ('' != error.toString() && 0 != error.length()) {
            def errorString = error.toString()
            printf "%6s:  %s", ['error', errorString]
            if (errorClosure != null) errorClosure.call(errorString)
        }
    }

    static void which(String command, outputClosure, errorClosure) {
        PluginUtils.dispatchSystem {
            commandByOsXByBash("which ${command}", outputClosure, errorClosure)
        } {
            commandByOsXByBash("which ${command}", outputClosure, errorClosure)
        } {
            commandByWindow("which ${command}", outputClosure, errorClosure)
        }
    }

    static void chmod(String path) {
        command("chmod 755 $path")
    }

    static void removeDirectory(String path) {
        command("rm -rf $path")
    }

    static void removeFile(String path) {
        command("rm $path")
    }
}