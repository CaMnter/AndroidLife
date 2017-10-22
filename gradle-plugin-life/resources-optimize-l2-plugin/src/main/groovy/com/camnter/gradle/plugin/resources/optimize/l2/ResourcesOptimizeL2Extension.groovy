package com.camnter.gradle.plugin.resources.optimize.l2;

/**
 * @author CaMnter
 */

class ResourcesOptimizeL2Extension {
    def webpConvert = false
    def debugResourcesSize = true
    def debugResourcesOptimize = false
    // 100 kb
    def maxSize = 100

    def cwebpPath = ''
    def guetzliPath = ''
    def pngquantPath = ''
}