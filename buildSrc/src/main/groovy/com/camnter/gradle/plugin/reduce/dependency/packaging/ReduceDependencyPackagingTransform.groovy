package com.camnter.gradle.plugin.reduce.dependency.packaging

import com.android.build.api.transform.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.io.FileUtils
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

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

        printf "%-52s = %s\n",
                ['[ReduceDependencyPackagingPlugin]   [applicationId]', applicationId]
        def manifestFile = project.file("src/main/AndroidManifest.xml")
        printf "%-52s = %s\n",
                ['[ReduceDependencyPackagingPlugin]   [manifestFile]', "${manifestFile.path}   [exists] = ${manifestFile.exists()}"]
        if (manifestFile.exists()) {
            def parsedManifest = new XmlParser().parse(
                    new InputStreamReader(new FileInputStream(manifestFile), "utf-8"))
            if (parsedManifest != null) {
                def packageName = parsedManifest.attribute("package")
                if (packageName != null) {
                    applicationId = packageName
                    printf "%-52s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [applicationId]', applicationId]
                }
            }
        }
        applicationId = applicationId.replaceAll("\\.", String.valueOf(File.separatorChar))
        printf "%-52s = %s\n\n",
                ['[ReduceDependencyPackagingPlugin]   [applicationId]', applicationId]

        transformInvocation.inputs.each {
            it.directoryInputs.each { DirectoryInput directoryInput ->
                directoryInput.file.traverse(type: FileType.FILES) {
                    def entryName = it.path.substring(directoryInput.file.path.length() + 1)
                    def destName = directoryInput.name + '/' + entryName
                    def output = transformInvocation.outputProvider.getContentLocation(destName,
                            directoryInput.contentTypes,
                            directoryInput.scopes,
                            Format.DIRECTORY)
                    // check bundle
                    def copySuccess = false
                    if (entryName.contains(applicationId)) {
                        FileUtils.copyFile(it, output)
                        copySuccess = true
                    }

                    printf "%-52s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [copySuccess]', copySuccess]
                    printf "%-52s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [directoryInput]', directoryInput.file.path]
                    printf "%-52s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [entryName]', entryName]
                    printf "%-52s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [destName]', destName]
                    printf "%-52s = %s\n",
                            ['[ReduceDependencyPackagingPlugin]   [output]', output.path]
                    printf "%-52s = %s\n\n",
                            ['[ReduceDependencyPackagingPlugin]   [input]', it.path]
                }
            }
        }
    }
}