package com.camnter.gradle.plugin.arouter.utils

import com.camnter.gradle.plugin.arouter.core.RegisterTransform
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * 扫描工具类
 *
 * Scan all class in the package: com/alibaba/android/arouter/
 * find out all routers,interceptors and providers
 * @author billy.qi email: qiyilike@163.com
 * @since 17/3/20 11:48
 */
class ScanUtil {

    /**
     * 扫描 jar 中的 class
     * 这些 class 是否是 IRouteRoot IInterceptorGroup 和 IProviderGroup 实现类
     * 是的话，记录 该 class
     *
     * scan jar file
     * @param jarFile All jar files that are compiled into apk
     * @param destFile dest file after this transform
     */
    static void scanJar(File jarFile, File destFile) {
        if (jarFile) {
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                if (entryName.startsWith(ScanSetting.ROUTER_CLASS_PACKAGE_NAME)) {
                    InputStream inputStream = file.getInputStream(jarEntry)
                    scanClass(inputStream)
                    inputStream.close()
                } else if (ScanSetting.GENERATE_TO_CLASS_FILE_NAME == entryName) {
                    // mark this jar file contains LogisticsCenter.class
                    // After the scan is complete, we will generate register code into this file
                    RegisterTransform.fileContainsInitClass = destFile
                }
            }
            file.close()
        }
    }

    /**
     * 根据不包含 "com.android.support" 或 "/android/m2repository"
     * 判断出是不是本项目 class，是的话扫描 jar 中的 class
     *
     * @param path path
     * @return boolean
     */
    static boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository")
    }

    /**
     * 是否是 APT 生成目录
     *
     * @param entryName entryName
     * @return boolean
     */
    static boolean shouldProcessClass(String entryName) {
        return entryName != null && entryName.startsWith(ScanSetting.ROUTER_CLASS_PACKAGE_NAME)
    }

    /**
     * scan class file
     * @param class file
     */
    static void scanClass(File file) {
        scanClass(new FileInputStream(file))
    }

    /**
     * ASM 扫描类
     *
     * @param inputStream
     */
    static void scanClass(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ScanClassVisitor cv = new ScanClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        inputStream.close()
    }

    /**
     * 扫描 class 归类
     * 归类到对应的 ScanSetting 内
     *
     * 比如 IRouteRoot class 就归类到对应 ScanSetting 内中的 List 内
     */
    static class ScanClassVisitor extends ClassVisitor {

        ScanClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        void visit(int version, int access, String name, String signature,
                String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            RegisterTransform.registerList.each { ext ->
                if (ext.interfaceName && interfaces != null) {
                    interfaces.each { itName ->
                        if (itName == ext.interfaceName) {
                            ext.classList.add(name)
                        }
                    }
                }
            }
        }
    }

}