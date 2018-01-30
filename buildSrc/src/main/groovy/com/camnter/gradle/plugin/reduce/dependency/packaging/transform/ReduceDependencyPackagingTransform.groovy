package com.camnter.gradle.plugin.reduce.dependency.packaging.transform

import com.android.build.api.transform.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import groovy.io.FileType
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

/**
 * @author CaMnter
 */

class ReduceDependencyPackagingTransform extends Transform {

    Project project
    DomainObjectSet<BaseVariant> variants

    ReduceDependencyPackagingTransform(Project project, DomainObjectSet<BaseVariant> variants) {
        this.project = project
        this.variants = variants
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

        String applicationId = variants.first().applicationId

        printf "%-57s = %s\n",
                ['[ReduceDependencyPackagingPlugin]   [applicationId]', applicationId]
        def manifestFile = project.file("src/main/AndroidManifest.xml")
        printf "%-57s = %s\n",
                ['[ReduceDependencyPackagingPlugin]   [manifestFile]', "${manifestFile.path}   [exists] = ${manifestFile.exists()}"]
        if (manifestFile.exists()) {
            def parsedManifest = new XmlParser().parse(
                    new InputStreamReader(new FileInputStream(manifestFile), "utf-8"))
            if (parsedManifest != null) {
                def packageName = parsedManifest.attribute("package")
                if (packageName != null) {
                    applicationId = packageName
                    printf "%-57s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [applicationId]', applicationId]
                }
            }
        }
        applicationId = applicationId.replaceAll("\\.", String.valueOf(File.separatorChar))
        printf "%-57s = %s\n\n",
                ['[ReduceDependencyPackagingPlugin]   [applicationId]', applicationId]

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
                    if (inputFileSuffixName.contains(applicationId)) {
                        if (!output.parentFile.exists()) {
                            output.mkdirs()
                        }
                        FileUtils.copyFile(it, output)
                        copySuccess = output.exists()
                    }

                    printf "%-57s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [copySuccess]', copySuccess]
                    printf "%-57s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [input name]', directoryInput.name]
                    printf "%-57s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [inputFileSuffixName]', inputFileSuffixName]
                    printf "%-57s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [inputDir]', inputDir]
                    printf "%-57s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [outputDir]', outputDir]
                    printf "%-57s = %s\n\n",
                            ['[ReduceDependencyPackagingPlugin]   [input]', it.path]
                    printf "%-57s = %s\n\n",
                            ['[ReduceDependencyPackagingPlugin]   [output]', output.path]
                }
            }

            it.jarInputs.each { JarInput jarInput ->
                // TODO check condition && check bundle && check filter
            }
        }
    }
}