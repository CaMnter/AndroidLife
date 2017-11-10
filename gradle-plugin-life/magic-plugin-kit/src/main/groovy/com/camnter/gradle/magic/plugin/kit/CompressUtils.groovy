package com.camnter.gradle.magic.plugin.kit

/**
 * @author CaMnter
 */

class CompressUtils {

    static final TAG = 'CompressUtils'

    static void compressResource(File file, Closure pngClosure, Closure jpgClosure) {

        if (ImageUtils.checkImage(file)) {
            def originalSize = file.length()
            def path = file.path
            if (ImageUtils.checkJPG(file)) {
                /**
                 * eg: "guetzli ${file.path} ${file.path}"
                 * */
                jpgClosure.call(file)
            } else {
                /**
                 * eg: "pngquant --skip-if-larger --speed 3 --force --output ${file.path} -- ${file.path}"
                 * */
                pngClosure.call(file)
            }
            def currentSize = file.length()
            printf "%-85s  %s\n",
                    ["[${TAG}]   [originalSize] = ${originalSize}   [currentSize] = ${currentSize}", path]
        }
    }
}
