package butterknife.plugin;

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.tasks.ProcessAndroidResources
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

class ButterKnifePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.all {
            when (it) {
            /*
             * instance of FeaturePlugin
             * project.extensions >> ExtensionContainer
             * fun <T : Any> ExtensionContainer.get(type: KClass<T>): T >> FeatureExtension
             *
             * FeaturePlugin extends LibraryPlugin
             *
             * FeaturePlugin.featureVariants >> DomainObjectSet<ApplicationVariant>
             * FeaturePlugin.libraryVariants >> DomainObjectSet<ApplicationVariant>
             */
                is FeaturePlugin -> {
                    project.extensions[FeatureExtension::class].run {
                        configureR2Generation(project, featureVariants)
                        configureR2Generation(project, libraryVariants)
                    }
                }
            /*
             * instance of LibraryPlugin
             * project.extensions >> ExtensionContainer
             * fun <T : Any> ExtensionContainer.get(type: KClass<T>): T >> LibraryExtension
             * LibraryExtension.libraryVariants >> DefaultDomainObjectSet<LibraryVariant>
             */
                is LibraryPlugin -> {
                    project.extensions[LibraryExtension::class].run {
                        configureR2Generation(project, libraryVariants)
                    }
                }
            /*
             * instance of AppPlugin
             * project.extensions >> ExtensionContainer
             * fun <T : Any> ExtensionContainer.get(type: KClass<T>): T >> AppExtension
             * AppExtension.applicationVariants >> DomainObjectSet<ApplicationVariant>
             */
                is AppPlugin -> {
                    project.extensions[AppExtension::class].run {
                        configureR2Generation(project, applicationVariants)
                    }
                }
            }
        }
    }

    private fun configureR2Generation(project: Project, variants: DomainObjectSet<out BaseVariant>) {
        // 遍历 DomainObjectSet<out BaseVariant>
        variants.all { variant ->
            /**
             * 获取每个 output 文件夹 File
             * generated/source/r2/{package name}/
             */
            val outputDir = project.buildDir.resolve(
                    "generated/source/r2/${variant.dirName}")

            // 创建对应的 R2 任务
            val task = project.tasks.create("generate${variant.name.capitalize()}R2")
            // 设置 R2 任务的 输出目录 File
            task.outputs.dir(outputDir)
            // 注册任务
            variant.registerJavaGeneratingTask(task, outputDir)

            val once = AtomicBoolean()

            // 遍历 DomainObjectCollection<BaseVariantOutput>
            variant.outputs.all { output ->
                // ProcessAndroidResources
                val processResources = output.processResources
                // ProcessAndroidResources 添加到任务内
                task.dependsOn(processResources)

                // Though there might be multiple outputs, their R files are all the same. Thus, we only
                // need to configure the task once with the R.java input and action.
                if (once.compareAndSet(false, true)) {
                    // 拿到 R 文件夹路径
                    val variantScope = processResources.getVariantScope()
                    val variantData = variantScope.variantData
                    val config = variantData.variantConfiguration
                    val splitName = config.splitFromManifest
                    val rPackage = if (splitName == null) {
                        config.originalApplicationId
                    } else {
                        config.originalApplicationId + "." + splitName
                    }
                    // 替换 R 文件夹路径 的 分隔符
                    val pathToR = rPackage.replace('.', File.separatorChar)
                    // 拿到 R 文件
                    val rFile = processResources.sourceOutputDir.resolve(pathToR).resolve("R.java")
                    task.apply {
                        // 注册 R File 到任务内
                        inputs.file(rFile)
                        // JavaPoet + JavaParser 生成 R2.java
                        doLast {
                            FinalRClassBuilder.brewJava(rFile, outputDir, rPackage, "R2")
                        }
                    }
                }
            }
        }
    }

    private fun ProcessAndroidResources.getVariantScope(): VariantScope {
        val property = ProcessAndroidResources::class
                .declaredMemberProperties
                .find { it.name == "variantScope" } as KProperty1<*, *>
        property.isAccessible = true
        val value = property.getter.call(this)
        return value as VariantScope
    }

    private operator fun <T : Any> ExtensionContainer.get(type: KClass<T>): T {
        return getByType(type.java)!!
    }
}