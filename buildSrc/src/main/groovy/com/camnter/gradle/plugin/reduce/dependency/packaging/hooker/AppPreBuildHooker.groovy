package com.camnter.gradle.plugin.reduce.dependency.packaging.hooker

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.ide.ArtifactDependencyGraph
import com.android.build.gradle.internal.tasks.AppPreBuildTask
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.model.Dependencies
import com.android.builder.model.SyncIssue
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.AarDependenceInfo
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.DependenceInfo
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.dependence.JarDependenceInfo
import com.camnter.gradle.plugin.reduce.dependency.packaging.utils.FileUtil
import org.gradle.api.Project

import java.util.function.Consumer

/**
 * Refer from VirtualAPK
 *
 * Gather list of dependencies(aar&jar) need to be stripped&retained after the PrepareDependenciesTask finished.
 * The entire stripped operation throughout the build lifecycle is based on the result of this hooker。
 *
 * @author CaMnter
 */

class AppPreBuildHooker extends GradleTaskHooker<AppPreBuildTask> {

    //group:artifact:version
    def hostDependencies = [] as Set

    def retainedAarLibs = [] as Set<AarDependenceInfo>
    def retainedJarLib = [] as Set<JarDependenceInfo>
    def stripDependencies = [] as Collection<DependenceInfo>

    AppPreBuildHooker(Project project, ApkVariant apkVariant) {
        super(project, apkVariant)
    }

    /**
     * Return the task name or transform name of the hooked task(transform task)
     * */
    @Override
    String getTaskName() {
        return "pre${apkVariant.name.capitalize()}Build"
    }

    /**
     * Callback function before the hooked task executes
     *
     * @param task Hooked task
     */
    @Override
    void beforeTaskExecute(AppPreBuildTask task) {
        reduceDependencyPackagingExtension?.hostDependenceFile?.splitEachLine('\\s+',
                { List<String> columns ->
                    final def module = columns[0].split(':')
                    hostDependencies.add("${module[0]}:${module[1]}")
                })
        reduceDependencyPackagingExtension?.excludes?.each { String artifact ->
            final def module = artifact.split(':')
            hostDependencies.add("${module[0]}:${module[1]}")
        }
    }

    /**
     * Callback function after the hooked task executes
     *
     * @param task Hooked task
     */
    @Override
    void afterTaskExecute(AppPreBuildTask task) {
        final BaseVariantData variantData = (apkVariant as ApplicationVariantImpl).variantData
        Dependencies dependencies = new ArtifactDependencyGraph().createDependencies(
                variantData.scope,
                false,
                new Consumer<SyncIssue>() {
                    @Override
                    void accept(SyncIssue syncIssue) {
                        printf "%-69s = %s\n",
                                ['[ReduceDependencyPackagingPlugin]   [AppPreBuildHooker]   [syncIssue]', syncIssue]
                    }
                })

        // android dependencies
        dependencies.libraries.each {
            printf "%-69s = %s\n",
                    ['[ReduceDependencyPackagingPlugin]   [AppPreBuildHooker]   [aar]', it.jarFile.path]
            def mavenCoordinates = it.resolvedCoordinates
            def aar = new AarDependenceInfo(mavenCoordinates.groupId,
                    mavenCoordinates.artifactId,
                    mavenCoordinates.version,
                    it)
            if (hostDependencies.contains(
                    "${mavenCoordinates.groupId}:${mavenCoordinates.artifactId}")) {
                stripDependencies.add(aar)
            } else {
                retainedAarLibs.add(aar)
            }
        }

        // java dependencies
        dependencies.javaLibraries.each {
            printf "%-69s = %s\n",
                    ['[ReduceDependencyPackagingPlugin]   [AppPreBuildHooker]   [jar]', it.jarFile.path]
            def mavenCoordinates = it.resolvedCoordinates
            def jar = new JarDependenceInfo(mavenCoordinates.groupId,
                    mavenCoordinates.artifactId,
                    mavenCoordinates.version,
                    it)
            if (hostDependencies.contains(
                    "${mavenCoordinates.groupId}:${mavenCoordinates.artifactId}")) {
                stripDependencies.add(jar)
            } else {
                retainedJarLib.add(jar)
            }
        }

        File hostDir = task.fakeOutputDirectory
        FileUtil.saveFile(hostDir, "${taskName}-stripDependencies", stripDependencies)
        FileUtil.saveFile(hostDir, "${taskName}-retainedAarLibs", retainedAarLibs)
        FileUtil.saveFile(hostDir, "${taskName}-retainedJarLib", retainedJarLib)

        printf "%-69s = %s\n",
                ['[ReduceDependencyPackagingPlugin]   [AppPreBuildHooker]   [hostDir]', hostDir.path]

        reduceDependencyPackagingExtension.variantData = variantData
        reduceDependencyPackagingExtension.retainedAarLibs = retainedAarLibs
        reduceDependencyPackagingExtension.stripDependencies = stripDependencies
    }
}