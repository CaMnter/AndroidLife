package com.camnter.gradle.plugin.toytime

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.internal.time.Clock

/**
 * @author CaMnter
 */

class ToyTimeListener implements TaskExecutionListener, BuildListener {

    Project project

    Clock clock
    def times = []

    def keyword = ''
    def minElapsedMillis = 0

    ToyTimeListener(Project target) {
        project = target
    }

    @Override
    void buildFinished(BuildResult buildResult) {
        printf "\n%-17s   =  %s", ["[keyword]", this.keyword]
        printf "\n%-17s  =  %s\n\n", ["[minElapsedMillis]", this.minElapsedMillis]

        println "Task spend time:"
        for (time in times) {
            printf "%7sms  %s\n", time
        }
    }

    @Override
    void beforeExecute(Task task) {
        this.clock = new Clock()
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        def elapsedMillis = this.clock.elapsedMillis
        if (elapsedMillis >= minElapsedMillis) {
            if (null != keyword && keyword.length() > 0) {
                if (task.path.contains(keyword)) {
                    times.add([elapsedMillis, task.path])
                }
            } else {
                times.add([elapsedMillis, task.path])
            }
        }
        task.project.logger.info "${task.path} spend ${elapsedMillis}ms"
    }

    @Override
    void buildStarted(Gradle gradle) {}

    @Override
    void settingsEvaluated(Settings settings) {}

    @Override
    void projectsLoaded(Gradle gradle) {}

    @Override
    void projectsEvaluated(Gradle gradle) {
        def toyTimeExtension = project.extensions.getByName('toyTimeExtension')
        this.keyword = toyTimeExtension.keyword
        this.minElapsedMillis = toyTimeExtension.minElapsedMillis
    }
}