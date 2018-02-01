package com.camnter.gradle.plugin.reduce.dependency.packaging.hooker

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.tasks.PrepareDependenciesTask
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.AarDependenceInfo
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.DependenceInfo
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.JarDependenceInfo
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency

/**
 * Refer from VirtualAPK
 *
 * Gather list of dependencies(aar&jar) need to be stripped&retained after the PrepareDependenciesTask finished.
 * The entire stripped operation throughout the build lifecycle is based on the result of this hooker。
 *
 * @author CaMnter
 */

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
        return "prepare${apkVariant.name.capitalize()}Dependencies"
    }

    /**
     * Collect host dependencies via hostDependenceFile or exclude configuration before PrepareDependenciesTask execute,
     * @param task Gradle Task fo PrepareDependenciesTask
     */
    @Override
    void beforeTaskExecute(PrepareDependenciesTask task) {
        reduceDependencyPackagingExtension.hostDependenceFile.splitEachLine('\\s+',
                { List<String> columns ->
                    final def module = columns[0].split(':')
                    hostDependencies.add("${module[0]}:${module[1]}")
                })
        reduceDependencyPackagingExtension.excludes.each { String artifact ->
            final def module = artifact.split(':')
            hostDependencies.add("${module[0]}:${module[1]}")
        }
    }

    /**
     * Classify all dependencies into retainedAarLibs & retainedJarLib & stripDependencies
     *
     * @param task Gradle Task fo PrepareDependenciesTask
     */
    @Override
    void afterTaskExecute(PrepareDependenciesTask task) {

        reduceDependencyPackagingExtension.variantData = task.variant

        final ApplicationVariant variant = task.variant as ApplicationVariant
        final Configuration configuration = variant.compileConfiguration
        configuration.allDependencies.each { Dependency dependency ->
            def group = dependency.group
            def name = dependency.name
            def version = dependency.version
            configuration.files(dependency).each {
                def fileName = it.name
                printf "%-57s = %s\n",
                        ['[ReduceDependencyPackagingPlugin]   [PrepareDependenciesHooker]   [dependency file]', it.path]
                if (fileName.endsWith('.aar')) {
                    final AarDependenceInfo aar = new AarDependenceInfo(group, name, version, it)
                    if (hostDependencies.contains("${group}:${name}")) {
                        stripDependencies.add(aar)
                    } else {
                        retainedAarLibs.add(aar)
                    }
                } else if (fileName.endsWith('.jar')) {
                    final JarDependenceInfo jar = new JarDependenceInfo(group, name, version, it)
                    if (hostDependencies.contains("${group}:${name}")) {
                        stripDependencies.add(jar)
                    } else {
                        retainedJarLib.add(jar)
                    }
                }
            }
        }
    }
}