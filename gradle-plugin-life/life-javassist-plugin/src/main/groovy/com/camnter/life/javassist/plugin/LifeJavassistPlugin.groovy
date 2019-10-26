package com.camnter.life.javassist.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.ApkVariantData
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class LifeJavassistPlugin implements Plugin<Project> {

    static String LIFE_JAVASSIST_EXTENSION = 'lifeJavassistExtension'
    AppExtension android

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin.class)) return

        android = project.extensions.findByType(AppExtension.class)
        final LifeTransform lifeTransform = new LifeTransform(project)
        android.registerTransform(lifeTransform)

        project.extensions.create(LIFE_JAVASSIST_EXTENSION, LifeJavassistExtension)
        android.applicationVariants.all {
            final ApkVariantData variantData = (it as ApplicationVariantImpl).variantData
            final VariantScope scope = variantData.scope

            final LifeJavassistExtension lifeJavassistExtension = project.extensions.getByName(
                    LIFE_JAVASSIST_EXTENSION)

            // 创建生成 LifeJavassistExtension 的 Task
            def taskName = scope.getTaskName('lifeJavassistExtensionTask')
            final Task task = project.task(taskName)
            task.doLast {
                createLifeJavassistExtension(scope, lifeJavassistExtension)
            }

            // 寻找生成 BuildConfig 的 Task
//            def buildConfigTaskName = scope.getGenerateBuildConfigTask().name
//            final Task buildConfigTask = project.tasks.getByName(buildConfigTaskName)
//
//            // 生成任务滞后于 buildConfigTask
//            if (buildConfigTask) {
//                task.dependsOn buildConfigTask
//                buildConfigTask.finalizedBy task
//            }
        }
    }

    static def createLifeJavassistExtension(VariantScope scope,
            LifeJavassistExtension lifeJavassistExtension) {
        def classContent =
"""
/**
 * Automatically generated file by javassist
 */
package com.camnter.newlife;

/**
 * CaMnter
 */
  
public class LifeJavassistExtension {

    public static final String JAVASSIST_TAG = "Class created by javassist";
    public static final String JAVASSIST_USER_SIGN = "${lifeJavassistExtension.sign}";

}
"""
        final File buildConfigOutputDir = scope.getBuildConfigSourceOutputDir()
        def javaFile = new File(buildConfigOutputDir, 'LifeJavassistExtension.java')
        javaFile.write(classContent, 'UTF-8')
    }
}