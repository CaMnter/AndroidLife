package com.camnter.gradle.plugin.reduce.dependency.packaging.transform

import com.android.build.api.transform.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.camnter.gradle.plugin.reduce.dependency.packaging.ReduceDependencyPackagingExtension
import com.camnter.gradle.plugin.reduce.dependency.packaging.collector.HostClassAndResCollector
import groovy.io.FileType
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

/**
 * @author CaMnter
 */

class ReduceDependencyPackagingTransform extends Transform {

    Project project
    DomainObjectSet<BaseVariant> variants
    HostClassAndResCollector classAndResCollector
    ReduceDependencyPackagingExtension reduceDependencyPackagingExtension

    ReduceDependencyPackagingTransform(Project project, DomainObjectSet<BaseVariant> variants) {
        this.project = project
        this.variants = variants
        classAndResCollector = new HostClassAndResCollector()
        this.reduceDependencyPackagingExtension = project.reduceDependencyPackagingExtension
    }

    @Override
    String getName() {
        return "reduceDependencyPackagingTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_JARS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {

        if (!isIncremental()) {
            transformInvocation.outputProvider.deleteAll()
        }

        def packagePath = reduceDependencyPackagingExtension.packagePath
        if (packagePath == null || packagePath.empty) {
            reduceDependencyPackagingExtension.with {
                it.packageName = getApplicationId(project, variants.first())
                it.packagePath = packageName.replace('.'.charAt(0), File.separatorChar)
            }
        }

        transformInvocation.inputs.each {

            it.directoryInputs.each { DirectoryInput directoryInput ->
                def inputDir = directoryInput.file.path
                directoryInput.file.traverse(type: FileType.FILES) {
                    def inputFileSuffixName = it.path.substring(inputDir.length() + 1)
                    def outputDir = transformInvocation.outputProvider.getContentLocation(
                            directoryInput.name,
                            directoryInput.contentTypes,
                            directoryInput.scopes, Format.DIRECTORY)
                    def output = new File(
                            "${outputDir.path}${File.separator}${inputFileSuffixName}")

                    // TODO check condition && check bundle && check filter
                    def copySuccess = false
                    if (inputFileSuffixName.contains(packagePath)) {
                        if (!output.parentFile.exists()) {
                            output.mkdirs()
                        }
                        FileUtils.copyFile(it, output)
                        copySuccess = output.exists()
                    }

                    printf "%-57s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [copySuccess]', copySuccess]
                    printf "%-57s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [dir name]', directoryInput.name]
                    printf "%-57s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [inputFileSuffixName]', inputFileSuffixName]
                    printf "%-57s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [inputDir]', inputDir]
                    printf "%-57s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [outputDir]', outputDir]
                    printf "%-57s = %s\n\n",
                            ['[ReduceDependencyPackagingPlugin]   [dir input]', it.path]
                    printf "%-57s = %s\n\n",
                            ['[ReduceDependencyPackagingPlugin]   [dir output]', output.path]
                }
            }

            def stripEntries = classAndResCollector.collect(
                    reduceDependencyPackagingExtension.stripDependencies)


            it.jarInputs.each { JarInput jarInput ->
                Set<String> jarEntryNames = HostClassAndResCollector.unzipJar(jarInput.file)
                def outputFile = transformInvocation.outputProvider.getContentLocation(
                        jarInput.name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                // TODO check condition && check bundle && check filter
                def copySuccess = false
                if (!stripEntries.containsAll(jarEntryNames)) {
                    FileUtils.copyFile(jarInput.file, outputFile)
                    copySuccess = outputFile.exists()
                }
                printf "%-57s = %s\n",
                        ['[ReduceDependencyPackagingPlugin]   [copySuccess]', copySuccess]
                printf "%-57s = %s\n",
                        ['[ReduceDependencyPackagingPlugin]   [jar name]', jarInput.name]
                printf "%-57s = %s\n\n",
                        ['[ReduceDependencyPackagingPlugin]   [jar input]', jarInput.file.path]
                printf "%-57s = %s\n\n",
                        ['[ReduceDependencyPackagingPlugin]   [jar output]', outputFile.path]
                println "${name} jar: ${jarInput.file.absoluteFile}"
            }
        }
    }
}