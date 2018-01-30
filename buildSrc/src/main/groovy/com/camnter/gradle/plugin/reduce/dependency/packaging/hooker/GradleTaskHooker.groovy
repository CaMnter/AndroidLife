package com.camnter.gradle.plugin.reduce.dependency.packaging.hooker

import com.android.build.gradle.api.ApkVariant
import com.camnter.gradle.plugin.reduce.dependency.packaging.ReduceDependencyPackagingExtension
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Copy from VirtualAPK
 *
 * Base class of gradle task hooker， provides some common field used by hookers
 * @param < T >  Type of hooked task
 *
 * @author CaMnter
 */

abstract class GradleTaskHooker<T extends Task> {

    private Project project

    /**
     * A Build variant when build a apk and all its public data.*/
    private ApkVariant apkVariant

    private ReduceDependencyPackagingExtension reduceDependencyPackagingExtension

    private TaskHookerManager taskHookerManager

    GradleTaskHooker(Project project, ApkVariant apkVariant) {
        this.project = project
        this.apkVariant = apkVariant
        this.reduceDependencyPackagingExtension = project.reduceDependencyPackagingExtension
    }

    Project getProject() {
        return this.project
    }

    ApkVariant getApkVariant() {
        return this.apkVariant
    }

    ReduceDependencyPackagingExtension getReduceDependencyPackagingExtension() {
        return this.reduceDependencyPackagingExtension
    }

    TaskHookerManager getTaskHookerManager() {
        return this.taskHookerManager
    }

    void setTaskHookerManager(TaskHookerManager taskHookerManager) {
        this.taskHookerManager = taskHookerManager
    }

    T getTask() {}

    /**
     * Return the task name or transform name of the hooked task(transform task)*/
    abstract String getTaskName()

    /**
     * Callback function before the hooked task executes
     * @param task Hooked task
     */
    abstract void beforeTaskExecute(T task)

    /**
     * Callback function after the hooked task executes
     * @param task Hooked task
     */
    abstract void afterTaskExecute(T task)
}