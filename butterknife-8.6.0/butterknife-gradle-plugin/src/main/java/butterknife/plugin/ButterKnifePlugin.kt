package butterknife.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import java.io.File
import kotlin.reflect.KClass

class ButterKnifePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.all {
            when (it) {
            /*
             * instanceof LibraryPlugin
             * project.extensions >> ExtensionContainer
             * fun <T : Any> ExtensionContainer.get(type: KClass<T>): T >> LibraryExtension
             * LibraryExtension.libraryVariants >> DefaultDomainObjectSet<LibraryVariant>
             */
                is LibraryPlugin -> applyPlugin(project.extensions[LibraryExtension::class].libraryVariants)
            /*
             * instanceof AppPlugin
             * project.extensions >> ExtensionContainer
             * fun <T : Any> ExtensionContainer.get(type: KClass<T>): T >> AppExtension
             * AppExtension.libraryVariants >> DomainObjectSet<ApplicationVariant>
             */
                is AppPlugin -> applyPlugin(project.extensions[AppExtension::class].applicationVariants)
            }
        }
    }

    private fun applyPlugin(variants: DomainObjectSet<out BaseVariant>) {
        variants.all { variant ->
            // 遍历 List<BaseVariantOutput>
            variant.outputs.forEach { output ->
                // ProcessAndroidResources
                val processResources = output.processResources
                // TODO proper task registered as source-generating?
                // 在 processResources 阶段添加任务
                processResources.doLast {
                    // 拿到 R 文件路径，替换分隔符
                    val pathToR = processResources.packageForR.replace('.', File.separatorChar)
                    // 拿到 R 文件
                    val rFile = processResources.sourceOutputDir.resolve(pathToR).resolve("R.java")
                    // JavaPoet + JavaParser 生成 R2.java
                    FinalRClassBuilder.brewJava(rFile, processResources.sourceOutputDir,
                            processResources.packageForR, "R2")
                }
            }
        }
    }

    private operator fun <T : Any> ExtensionContainer.get(type: KClass<T>): T {
        return getByType(type.java)!!
    }
}
