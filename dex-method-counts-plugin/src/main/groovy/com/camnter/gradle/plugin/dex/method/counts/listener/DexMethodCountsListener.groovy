package com.camnter.gradle.plugin.dex.method.counts.listener

import com.camnter.gradle.plugin.dex.method.counts.DexMethodCountsExtension
import com.camnter.gradle.plugin.dex.method.counts.task.DexMethodCountsTask
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

/**
 * @author CaMnter
 */

class DexMethodCountsListener implements TaskExecutionListener, BuildListener {

    DexMethodCountsExtension dexMethodCountsExtension

    def parcels = []

    Project project

    DexMethodCountsListener(Project project) {
        this.project = project
    }

    @Override
    void beforeExecute(Task task) {}

    @Override
    void afterExecute(Task task, TaskState taskState) {
        if (task instanceof DexMethodCountsTask) {
            DexMethodCountsTask dexMethodCountsTask = task as DexMethodCountsTask
            parcels.add([dexMethodCountsTask.fileToCount, dexMethodCountsTask.analysisOutputFile])
        }
    }

    @Override
    void buildStarted(Gradle gradle) {}

    @Override
    void settingsEvaluated(Settings settings) {}

    @Override
    void projectsLoaded(Gradle gradle) {}

    @Override
    void projectsEvaluated(Gradle gradle) {}

    @Override
    void buildFinished(BuildResult buildResult) {
        dexMethodCountsExtension = project.dexMethodCountsExtension
        if (!dexMethodCountsExtension.printAble || parcels.size() == 0) return
        println "\nDex analysis:\n"
        for (parcel in parcels) {
            def first = parcel[0]
            def second = parcel[1]
            if (first) {
                println "${(first as File).name}:"
            }
            if (second) {
                println "${(second as File).path}\n"
            }
        }
    }
}