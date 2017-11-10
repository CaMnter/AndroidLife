package com.camnter.gradle.magic.plugin.kit

/**
 * @author CaMnter
 */

class CommandUtils {

    static final String NOT_SUCH = 'no such'

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

    String execByWindow(String command, File dir) {
        println "[CommandUtils]   [command] = ${command}"
        return exec("cmd /c start  /b ${command}", dir)
    }

    /**
     * 执行系统命令, 返回执行结果
     *
     * @param command 需要执行的命令
     * @param dir 执行命令的子进程的工作目录, null 表示和当前主进程工作目录相同
     */
    static String exec(String command, File dir) throws Exception {
        StringBuilder result = new StringBuilder()
        Process process = null
        BufferedReader bufferedOutput = null
        BufferedReader bufferedError = null

        try {
            // 执行命令, 返回一个子进程对象（命令在子进程中执行）
            process = Runtime.getRuntime().exec(command, null, dir)
            // 方法阻塞, 等待命令执行完成（ 成功会返回 0）
            process.waitFor()
            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（子进程的输出就是主进程的输入）
            bufferedOutput =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))
            bufferedError =
                    new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"))
            // 读取输出
            String line
            while ((line = bufferedOutput.readLine()) != null) {
                result.append(line).append('\n')
            }
            while ((line = bufferedError.readLine()) != null) {
                result.append(line).append('\n')
            }
        } finally {
            closeStream(bufferedOutput)
            closeStream(bufferedError)
            // 销毁子进程
            if (process != null) {
                process.destroy()
            }
        }
        // 返回执行结果
        return result.toString()
    }

    static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close()
            } catch (Exception e) {
                // nothing
                println "[CommandUtils]   [closeStream] = ${e.message}"
            }
        }
    }

    static boolean checkPath(String path) {
        return new File(path).exists()
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