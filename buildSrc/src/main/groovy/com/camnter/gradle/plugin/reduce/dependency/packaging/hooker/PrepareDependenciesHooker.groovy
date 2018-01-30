package com.camnter.gradle.plugin.reduce.dependency.packaging.hooker

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.tasks.PrepareDependenciesTask
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.AarDependenceInfo
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.DependenceInfo
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.JarDependenceInfo
import org.gradle.api.Project

class PrepareDependenciesHooker extends GradleTaskHooker<PrepareDependenciesTask> {

    //group:artifact:version
    def hostDependencies = [] as Set

    def retainedAarLibs = [] as Set<AarDependenceInfo>
    def retainedJarLib = [] as Set<JarDependenceInfo>
    def stripDependencies = [] as Collection<DependenceInfo>

    PrepareDependenciesHooker(Project project, ApkVariant apkVariant) {
        super(project, apkVariant)
    }

    /**
     * Return the task name or transform name of the hooked task(transform task)
     * */
    @Override
    String getTaskName() {
        return null
    }

    /**
     * Callback function before the hooked task executes
     * @param task Hooked task
     */
    @Override
    void beforeTaskExecute(PrepareDependenciesTask task) {}

    /**
     * Callback function after the hooked task executes
     * @param task Hooked task
     */
    @Override
    void afterTaskExecute(PrepareDependenciesTask task) {}
}