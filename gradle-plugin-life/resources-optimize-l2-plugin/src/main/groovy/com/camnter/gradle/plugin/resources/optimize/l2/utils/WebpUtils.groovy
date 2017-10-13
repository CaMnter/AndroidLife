package com.camnter.gradle.plugin.resources.optimize.l2.utils

import org.gradle.api.Project

/**
 * @author CaMnter
 */

class WebpUtils {

    static final int VERSION_SUPPORT_WEBP = 14
    static final int VERSION_SUPPORT_TRANSPARENT_WEBP = 18

    static boolean isPNGConvertSupported(Project project) {
        return AndroidUtils.getMinSdkVersion(project) >= VERSION_SUPPORT_WEBP
    }

    static boolean isTransparentPNGSupported(Project project) {
        return AndroidUtils.getMinSdkVersion(project) >= VERSION_SUPPORT_TRANSPARENT_WEBP
    }

    def static formatWebp(File imageFile, Closure formatClosure) {
        def path = imageFile.getPath()
        if (ImageUtils.checkImage(imageFile)) {
            File webpFile = new File("${path.substring(0, path.indexOf("."))}.webp")
            /**
             * eg: "cwebp ${imageFile.getPath()} -o ${webpFile.getPath()} -quiet"
             * */
            formatClosure.call(imageFile, webpFile)
            if (webpFile.length() < imageFile.length()) {
                println '[WebpUtils]:'
                printf "%-14s >> %s\n", ['[image path]', path]
                printf "%-14s >> %s\n", ['[image length]', imageFile.length()]
                printf "%-14s >> %s\n", ['[webp length]', webpFile.length()]
                if (imageFile.exists()) {
                    imageFile.delete()
                }
            } else {
                if (webpFile.exists()) {
                    webpFile.delete()
                }
            }
        }
    }

    def static securityFormatWebp(Project project, File imageFile, Closure formatClosure) {
        def name = imageFile.name
        if (ImageUtils.checkImage(imageFile)) {
            if (name.contains(ImageUtils.PNG)) {
                if (isPNGConvertSupported(project)) {
                    if (ImageUtils.checkAlphaPNG(imageFile)) {
                        if (isTransparentPNGSupported(project)) {
                            formatWebp(imageFile)
                        }
                    } else {
                        formatWebp(imageFile, formatClosure)
                    }
                }
            } else if (name.endsWith(ImageUtils.JPG) || name.
                    contains(ImageUtils.JPEG)) {
                formatWebp(imageFile)
                // other
            } else {
                println "[WebpUtils]:"
                printf "%s >> %s\n", [imageFile.getPath(), 'don\'t convert']
            }
        }
    }
}