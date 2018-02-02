package com.camnter.gradle.plugin.reduce.dependency.packaging.utils

/**
 * Refer from VirtualAPK
 *
 * @author CaMnter
 */

class FileUtil {

    static void saveFile(File dir, String fileName, Closure<List<?>> action) {
        List<?> list = action.call();
        saveFile(dir, fileName, list)
    }

    static void saveFile(File dir, String fileName, Collection<?> collection) {
        saveFile(dir, fileName, false, collection)
    }

    static void saveFile(File dir, String fileName, boolean sort, Collection<?> collection) {
        dir.mkdirs()
        def file = new File(dir, "${fileName}.txt")
        ArrayList<?> list = new ArrayList<>(collection)
        if (sort) {
            Collections.sort(list)
        }
        list.add('')
        file.write(list.join('\r\n'))
    }
}
