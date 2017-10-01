package com.camnter.gradle.plugin.life.kit

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

        if (ImageUtils.checkImage(imageFile)) {
            File webpFile = new File(
                    "${imageFile.getPath().substring(0, imageFile.getPath().indexOf("."))}.webp")
            /**
             * eg: "cwebp ${imageFile.getPath()} -o ${webpFile.getPath()} -quiet"
             * */
            formatClosure.call(imageFile, webpFile)
            if (webpFile.length() < imageFile.length()) {
                println "[WebpUtils]:"
                printf "%-14s >> %s\n", ['[image path]', imageFile.getPath()]
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

        if (ImageUtils.checkImage(imageFile)) {
            if (imageFile.getName().endsWith(ImageUtils.PNG)) {
                if (isPNGConvertSupported(project)) {
                    if (ImageUtils.isAlphaPNG(imageFile)) {
                        if (isTransparentPNGSupported(project)) {
                            formatWebp(imageFile)
                        }
                    } else {
                        formatWebp(imageFile, formatClosure)
                    }
                }
            } else if (imageFile.getName().endsWith(ImageUtils.JPG) || imageFile.getName().
                    endsWith(ImageUtils.JPEG)) {
                formatWebp(imageFile)
                // other
            } else {
                println "[WebpUtils]:"
                printf "%-14s >> %s\n", [imageFile.getPath(), 'don\'t convert']
            }
        }
    }
}