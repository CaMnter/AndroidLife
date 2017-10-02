package com.camnter.gradle.magic.plugin.kit

/**
 * Refer to the  kotlin source code
 *
 * kotlin/io/files/FilePathComponents.kt
 * kotlin/io/files/Utils.kt
 *
 * @author CaMnter
 */

class FileUtils {

    public static FILE_SEPARATOR = String.valueOf(File.separatorChar) as String

    static String resolve(File file, String relative) {
        if (isRooted(relative)) return relative
        def baseName = file.toString()
        def fileName
        if (baseName.isEmpty() || baseName.endsWith(FILE_SEPARATOR)) {
            fileName = baseName + relative
        } else {
            fileName = baseName + FILE_SEPARATOR + relative
        }
        return fileName
    }

    static int getRootLength(String path) {
        // Note: separators should be already replaced to system ones
        def first = path.indexOf(FILE_SEPARATOR, 0)
        def length = path.length()
        if (first == 0) {
            if (length > 1 && (Character.valueOf(path[1])) == File.separatorChar) {
                // Network names like //my.host/home/something ? => //my.host/home/ should be root
                // NB: does not work in Unix because //my.host/home is converted into /my.host/home there
                // So in Windows we'll have root of //my.host/home but in Unix just /
                first = path.indexOf(FILE_SEPARATOR, 2)
                if (first >= 0) {
                    first = path.indexOf(FILE_SEPARATOR, first + 1)
                    if (first >= 0) return first + 1 else return length
                }
            }
            return 1
        }
        // C:\
        if (first > 0 && path[first - 1] == ':') {
            first++
            return first
        }
        // C:
        if (first == -1 && path.endsWith(':')) return length
        return 0
    }

    static boolean isRooted(String relative) {
        return getRootLength(relative) > 0
    }
}