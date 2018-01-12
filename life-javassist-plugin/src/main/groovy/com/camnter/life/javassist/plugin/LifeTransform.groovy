package com.camnter.life.javassist.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.gradle.api.Project

/**
 * CaMnter
 * */

class LifeTransform extends Transform {

    private Project project

    LifeTransform(Project project) {
        this.project = project
    }

    /**
     * transformClassesWith ${name} ForDebug
     * transformClassesWith ${name} ForRelease
     *
     * @return Transform name
     */
    @Override
    String getName() {
        return LifeTransform.simpleName
    }

    /**
     * TransformManager.CONTENT_CLASS
     * TransformManager.CONTENT_JARS
     * TransformManager.CONTENT_RESOURCES
     * TransformManager.CONTENT_NATIVE_LIBS
     * TransformManager.CONTENT_DEX
     * TransformManager.DATA_BINDING_ARTIFACT
     *
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * TransformManager.PROJECT_ONLY
     * TransformManager.SCOPE_FULL_PROJECT
     * TransformManager.SCOPE_FULL_WITH_IR_FOR_DEXING
     * TransformManager.SCOPE_FULL_LIBRARY_WITH_LOCAL_JARS
     *
     * Scope.PROJECT
     * 只有 项目内容
     *
     * Scope.SUB_PROJECTS
     * 只有 子项目
     *
     * Scope.EXTERNAL_LIBRARIES
     * 只有 外部库
     *
     * Scope.TESTED_CODE
     * 只有 测试代码
     *
     * Scope.PROVIDED_ONLY
     * 只有 提供本地或远程依赖项
     *
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 是否支持 增量编译
     *
     * @return
     */
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {
        def input = transformInvocation.inputs
        if (input == null) return
        MainApplicationInject mainApplicationInject = new MainApplicationInject(project)
        input.each {
            // 文件夹
            it.directoryInputs.each {
                // 注入代码
                mainApplicationInject.inject(it)
                def output = transformInvocation.outputProvider.getContentLocation(it.name,
                        it.contentTypes,
                        it.scopes, Format.DIRECTORY)
                // input 目录复制到 output 目录
                FileUtils.copyDirectory(it.file, output)
            }

            // jar 文件
            it.jarInputs.each {
                def md5Name = mainApplicationInject.inject(it)
                def output = transformInvocation.outputProvider.getContentLocation(md5Name,
                        it.contentTypes,
                        it.scopes, Format.JAR)
                FileUtils.copyDirectory(it.file, output)
            }
        }
    }
}