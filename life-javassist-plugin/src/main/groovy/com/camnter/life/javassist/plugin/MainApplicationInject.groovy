package com.camnter.life.javassist.plugin

import com.android.build.api.dsl.extension.AppExtension
import com.android.build.api.transform.DirectoryInput
import com.android.build.gradle.BaseExtension
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

class MainApplicationInject {

    static void inject(Project project, DirectoryInput directoryInput) {
        final BaseExtension android = project.extensions.findByType(AppExtension.class)
        if (android != null) {
            // 注入
            def dirPath = directoryInput.file.absolutePath
            final ClassPool classPool = ClassPool.getDefault()
            /**
             * 当前文件夹路径加入 ClassPool
             * 否则，找不到类
             * */
            classPool.appendClassPath(dirPath)
            /**
             * BaseExtension 中 取 android.jar
             *
             * 加入 android.jar 否则
             * 找不到 android 相关类
             * */
            classPool.appendClassPath(android.bootClasspath[0].toString())
            /**
             * MainApplication 有这些 import
             *
             * import com.camnter.newlife.ui.activity.smartrouter.CustomRouterActivity;
             * import com.camnter.smartrouter.SmartRouters;
             * import com.camnter.smartrouter.core.Router;
             * import com.camnter.utils.AssetsUtils;
             * import dodola.hotfix.HotFix;
             * */
            classPool.importPackage(
                    'com.camnter.newlife.ui.activity.smartrouter.CustomRouterActivity')
            classPool.importPackage('com.camnter.smartrouter.SmartRouters')
            classPool.importPackage('com.camnter.smartrouter.core.Router')
            classPool.importPackage('com.camnter.utils.AssetsUtils')
            classPool.importPackage('dodola.hotfix.HotFix')

            def dirFile = directoryInput.file
            if (dirFile.isDirectory()) {
                dirFile.eachFileRecurse {
                    def filePath = it.absolutePath
                    if (it.name.equals('MainApplication.class')) {
                        println "[MainApplicationInject]   MainApplication.class was found   [filePath] = ${filePath}"
                        final CtClass applicationClass = classPool.getCtClass(
                                'com.camnter.newlife.MainApplication')
                        println "[MainApplicationInject]   [MainApplication CtClass] = ${applicationClass.toString()}"

                        /**
                         * 解冻
                         *
                         * 如果一个 CtClass 对象通过 writeFile()，toClass() 或者 toBytecode()
                         * 转换成了 class 文件
                         *
                         * 那么 javassist 会冻结这个 CtClass
                         * 后面就不能继续修改这个 CtClass
                         *
                         * 为了警告
                         * 开发者不要修改已经被 JVM 加载的 class 文件，因为 JVM 不允许重新加载一个类
                         * */
                        if (applicationClass.isFrozen()) {
                            applicationClass.defrost()
                        }

                        final CtMethod onCreate = applicationClass.getDeclaredMethod('onCreate')
                        println '[MainApplicationInject]   MainApplication#onCreate was found'

                        def injectContent = """// MainApplicationInject came here.\nfinal int mainApplicationInject = \"MainApplicationInject came here\";"""
                        onCreate.insertBefore(injectContent)
                        applicationClass.writeFile(dirPath)
                        // 释放
                        applicationClass.detach()
                    }
                }
            }
        }
    }
}