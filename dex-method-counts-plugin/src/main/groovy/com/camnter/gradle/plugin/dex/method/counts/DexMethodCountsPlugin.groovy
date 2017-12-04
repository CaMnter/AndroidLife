package com.camnter.gradle.plugin.dex.method.counts

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.PluginContainer

/**
 * @author CaMnter
 */

class DexMethodCountsPlugin implements Plugin<Project> {

    @Override
    def apply(Project project) {
        applyPluginIfNotApply(project, JavaPlugin.class)
        Task dexMethodCountsBeforeJarTask = project.task('dexMethodCountsBeforeJar',
                type: BeforeJarTask)
        Task dexMethodCountsAfterJarTask = project.task('dexMethodCountsBeforeJar',
                type: AfterJarTask)
        Task jarTask = project.tasks.getByName('jar')
        jarTask.dependsOn dexMethodCountsBeforeJarTask
        dexMethodCountsAfterJarTask.dependsOn jarTask
    }

    static <T extends Plugin> T applyPluginIfNotApply(Project project, Class<T> pluginClass) {
        PluginContainer pluginContainer = project.getPlugins()
        if (!pluginContainer.hasPlugin(pluginClass)) {
            pluginContainer.apply(pluginClass)
        }
        return pluginContainer.getPlugin(pluginClass) as T
    }
}