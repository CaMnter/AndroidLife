package com.camnter.life.javassist.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project

/**
 * CaMnter
 * */

class JavassistActivityInject extends BaseInject {

    private static final String TAG = JavassistActivityInject.simpleName

    JavassistActivityInject(Project project) {
        super(project)
    }

    /**
     * 目录
     *
     * @param directoryInput directoryInput
     */
    @Override
    def inject(DirectoryInput directoryInput) {
        if (!checkoutAppExtension()) return
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
         * JavassistActivity 有这些 import
         *
         * import android.widget.TextView;
         * import android.widget.Toast;
         * import com.camnter.newlife.R;
         * */
        classPool.importPackage('android.widget.TextView')
        classPool.importPackage('android.widget.Toast')
        classPool.importPackage('com.camnter.newlife.R')

        def dirFile = directoryInput.file
        if (dirFile.isDirectory()) {
            dirFile.eachFileRecurse {
                def filePath = it.absolutePath
                if (it.name.equals('JavassistActivity.class')) {
                    println "[${TAG}]   JavassistActivity.class was found   [filePath] = ${filePath}"
                    final CtClass applicationClass = classPool.getCtClass(
                            'com.camnter.newlife.ui.activity.javassist.JavassistActivity')
                    println "[${TAG}]   [JavassistActivity CtClass] = ${applicationClass.toString()}"

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

                    final CtMethod onCreate = applicationClass.getDeclaredMethod('initViews')
                    println '[${TAG}]   JavassistActivity#initViews was found'

                    applicationClass.get

                    def injectContent =
                            """
final TextView textView = (TextView) this.findView(R.id.text);
final String showText = "Javassist success";
textView.setText(showText);
Toast.makeText(this, showText, Toast.LENGTH_LONG).show();
"""
                    onCreate.insertBefore(injectContent)
                    applicationClass.writeFile(dirPath)
                    // 释放
                    applicationClass.detach()
                }
            }
        }
    }

    /**
     * jar
     *
     * @param jarInput jarInput
     */
    @Override
    def inject(JarInput jarInput) {
        def md5Path = DigestUtils.md5Hex(jarInput.file.absolutePath)
        def md5Name = jarInput.name
        if (md5Name.endsWith(".jar")) {
            md5Name = md5Name.substring(0, md5Name.length() - 4)
            md5Name = md5Name + md5Path
        }
        return md5Name
    }
}