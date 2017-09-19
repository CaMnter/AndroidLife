package com.camnter.gradle.plugin.method.trace

import com.camnter.gradle.plugin.method.trace.task.MethodExpectTraceTask
import com.camnter.gradle.plugin.method.trace.task.MethodWholeTraceTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author CaMnter
 */

class MethodTracePlugin implements Plugin<Project> {

    /**
     * Apply this plugin to the given target object.
     *
     * @param target The target object
     */
    @Override
    void apply(Project target) {
        target.task('methodWholeTraceTask', type: MethodWholeTraceTask)
        target.task('methodExpectTraceTask', type: MethodExpectTraceTask)
    }
}