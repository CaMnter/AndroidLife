package com.camnter.gradle.plugin.method.trace

import com.camnter.gradle.plugin.method.trace.task.MethodExpectTraceTask
import com.camnter.gradle.plugin.method.trace.task.MethodWholeTraceTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

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
        Task methodWholeTraceTask = target.tasks.findByName('methodWholeTraceTask')
        Task methodExpectTraceTask = target.tasks.findByName('methodExpectTraceTask')
        methodExpectTraceTask.dependsOn methodWholeTraceTask
        target.extensions.create('methodTraceExtension', MethodTraceExtension)
    }
}