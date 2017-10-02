package com.camnter.gradle.magic.plugin.kit

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * @author CaMnter
 */

class ImageUtils {

    public static final String JPG = ".jpg"
    public static final String JPEG = ".jpeg"
    public static final String PNG = ".png"
    public static final String DOT_9PNG = ".9.png"

    static boolean checkImage(File file) {
        return (file.getName().contains(JPG) || file.getName().contains(PNG) ||
                file.getName().contains(JPEG)) && !file.getName().contains(DOT_9PNG)
    }

    static boolean checkJPG(File file) {
        return file.getName().contains(JPG) || file.getName().endsWith(JPEG)
    }

    static boolean checkAlphaPNG(File filePath) {
        if (filePath.exists()) {
            try {
                BufferedImage bufferedImage = ImageIO.read(filePath)
                return bufferedImage.getColorModel().hasAlpha()
            } catch (Exception e) {
                println e.message
                return false
            }
        } else {
            return false
        }
    }

    static boolean checkImageSize(File imageFile, int maxSize) {
        if (checkImage(imageFile)) {
            if (imageFile.length() >= maxSize) {
                return true
            }
        }
        return false
    }
}