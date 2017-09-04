package com.camnter.gradle.plugin.toytime

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author CaMnter
 */

class ToyTimePlugin implements Plugin<Project> {

    /**
     * Apply this plugin to the given target object.
     *
     * @param target The target object
     */
    @Override
    void apply(Project target) {
        target.extensions.create('toyTimeExtension', ToyTimeExtension)
        target.gradle.addListener(new ToyTimeListener(target))
    }
}