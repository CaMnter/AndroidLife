/**
 * @author CaMnter
 */

class CommandUtils {

    static void command(String command) {
        def process = command.execute()
        def output = new StringBuilder()
        def error = new StringBuilder()
        process.consumeProcessOutput(output, error)
        process.waitFor()
        if ('' != output.toString() && 0 != output.length()) {
            printf "%6s:  %s", ['output', output]
        }
        if ('' != error.toString() && 0 != error.length()) {
            printf "%6s:  %s", ['error', error]
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