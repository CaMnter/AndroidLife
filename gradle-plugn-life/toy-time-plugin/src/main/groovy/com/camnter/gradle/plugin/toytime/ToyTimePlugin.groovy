package com.camnter.gradle.plugin.toytime

import org.gradle.api.Plugin
import org.gradle.api.Project

class ToyTimePlugin implements Plugin<Project> {

    /**
     * Apply this plugin to the given target object.
     *
     * @param target The target object
     */
    @Override
    void apply(Project target) {
        target.gradle.addListener(new ToyTimeListener())
    }
}