package com.camnter.gradle.plugin.reduce.dependency.packaging.utils

/**
 * Copy from VirtualAPK
 *
 * Extend << operator to copy file
 *
 * @author CaMnter
 */

class FileBinaryCategory {

    /**
     * Write content from a url to a file
     *
     * @param file file
     * @param url url
     */
    def static leftShift(File file, URL url) {
        def conn = url.openConnection()
        conn.with {
            def is = conn.getInputStream()
            file.withOutputStream { os ->
                def bs = new BufferedOutputStream(os)
                bs << is
            }
        }
    }

    /**
     * Write content from a src file to a dst file
     *
     * @param dst dst
     * @param src src
     */
    def static leftShift(File dst, File src) {
        src.withInputStream { is ->
            dst.withOutputStream { os ->
                def bs = new BufferedOutputStream(os)
                bs << is
            }
        }
    }
}