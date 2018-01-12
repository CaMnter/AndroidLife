package com.camnter.life.javassist.plugin

import com.android.build.api.dsl.extension.AppExtension
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.gradle.BaseExtension
import org.gradle.api.Project

/**
 * CaMnter
 * */

abstract class BaseInject {

    final Project project
    final BaseExtension android

    BaseInject(Project project) {
        this.project = project
        this.android = project.extensions.findByType(AppExtension.class)
    }

    def checkoutAppExtension() {
        return null != android
    }

    /**
     * 目录
     *
     * @param directoryInput directoryInput
     */
    abstract def inject(DirectoryInput directoryInput)

    /**
     * jar
     *
     * @param jarInput jarInput
     */
    abstract def inject(JarInput jarInput)
}