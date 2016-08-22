package com.camnter.patch

import javassist.ClassPool
import javassist.CtClass

/**
 * https://github.com/dodola/HotFix/blob/master/buildSrc/src/main/groovy/dodola/patch/PatchClass.groovy
 * * */
public class PatchClass {

    /**
     * 植入代码
     * @param buildDir 是项目的 build class 目录,就是我们需要注入的 class 所在地
     * @param lib 这个是 hack 的目录,就是 AntilazyLoad 类的 class 文件所在地
     */
    public static void process(String buildDir, String lib) {

        println(lib)
        ClassPool classes = ClassPool.getDefault()
        classes.appendClassPath(buildDir)
        classes.appendClassPath(lib)

        // 下面的操作比较容易理解,在将需要关联的类的构造方法中插入引用代码

        CtClass c = classes.getCtClass("com.camnter.newlife.ui.activity.hotfix.FixCall")
        if (c.isFrozen()) {
            c.defrost()
        }
        println("[ PatchClass ]  #  [ process ]  #  FixCall 添加构造方法")
        def constructor = c.getConstructors()[0];
        constructor.insertBefore("System.out.println(com.camnter.hack.AntilazyLoad.class);")
        c.writeFile(buildDir)



        CtClass c1 = classes.getCtClass("com.camnter.newlife.ui.activity.hotfix.LoadFixCall")
        if (c1.isFrozen()) {
            c1.defrost()
        }
        println("[ PatchClass ]  #  [ process ]  #  LoadFixCall 添加构造方法")
        def constructor1 = c1.getConstructors()[0];
        constructor1.insertBefore("System.out.println(com.camnter.hack.AntilazyLoad.class);")
        c1.writeFile(buildDir)
    }

    static void growl(String title, String message) {
        def proc = ["osascript", "-e", "display notification \"${message}\" with title \"${title}\""].
                execute()
        if (proc.waitFor() != 0) {
            println "[WARNING] ${proc.err.text.trim()}"
        }
    }
}
