package com.camnter.gradle.plugin.arouter.core

import com.camnter.gradle.plugin.arouter.utils.ScanSetting
import org.apache.commons.io.IOUtils
import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * 为 扫描 ARouter 生成的 IRouteRoot IInterceptorGroup 和 IProviderGroup 实现类
 * 生成对应的 注册代码
 *
 * generate register code into LogisticsCenter.class
 * @author billy.qi email: qiyilike@163.com
 */
class RegisterCodeGenerator {
    ScanSetting extension

    private RegisterCodeGenerator(ScanSetting extension) {
        this.extension = extension
    }

    /**
     * 找到 LogisticsCenter.class
     * 插入 注册代码
     *
     * @param registerSetting registerSetting
     */
    static void insertInitCodeTo(ScanSetting registerSetting) {
        if (registerSetting != null && !registerSetting.classList.isEmpty()) {
            RegisterCodeGenerator processor = new RegisterCodeGenerator(registerSetting)
            File file = RegisterTransform.fileContainsInitClass
            if (file.getName().endsWith('.jar')) processor.insertInitCodeIntoJarFile(file)
        }
    }

    /**
     * 解 jar
     * 拿出一个一个 class
     * 然后得到对应的 InputStream
     * 然后进行 ASM 操作
     * 在 LogisticsCenter 的 loadRouterMap 方法内添加 注册代码
     *
     * generate code into jar file
     * @param jarFile the jar file which contains LogisticsCenter.class
     * @return File
     */
    private File insertInitCodeIntoJarFile(File jarFile) {
        if (jarFile) {
            def optJar = new File(jarFile.getParent(), jarFile.name + ".opt")
            if (optJar.exists()) optJar.delete()
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = file.getInputStream(jarEntry)
                jarOutputStream.putNextEntry(zipEntry)
                if (ScanSetting.GENERATE_TO_CLASS_FILE_NAME == entryName) {

                    Logger.i('Insert init code to class >> ' + entryName)

                    def bytes = referHackWhenInit(inputStream)
                    jarOutputStream.write(bytes)
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                inputStream.close()
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            file.close()

            if (jarFile.exists()) {
                jarFile.delete()
            }
            optJar.renameTo(jarFile)
        }
        return jarFile
    }

    /**
     * refer hack class when object init
     *
     * ASM 操作
     * 在 LogisticsCenter 的 loadRouterMap 方法内添加 注册代码
     *
     * @param inputStream inputStream
     * @return byte[]
     */
    private byte[] referHackWhenInit(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ClassVisitor cv = new MyClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    /**
     * 自定义 ClassVisitor
     * 主要覆写 visitMethod 方法
     *
     * 然后，当遇到 loadRouterMap 方法的时候
     * return 一个自定义 MethodVisitor
     */
    class MyClassVisitor extends ClassVisitor {

        MyClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        void visit(int version, int access, String name, String signature,
                String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc,
                String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
            // generate code into this method
            if (name == ScanSetting.GENERATE_TO_METHOD_NAME) {
                mv = new RouteMethodVisitor(Opcodes.ASM5, mv)
            }
            return mv
        }
    }

    /**
     * 自定义 MethodVisitor
     * 用来给 loadRouterMap 方法添加 注册代码
     */
    class RouteMethodVisitor extends MethodVisitor {

        RouteMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv)
        }

        @Override
        void visitInsn(int opcode) {
            // generate code before return
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                extension.classList.each { name ->
                    name = name.replaceAll("/", ".")
                    mv.visitLdcInsn(name) //类名
                    // generate invoke register method into LogisticsCenter.loadRouterMap()
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC
                            , ScanSetting.GENERATE_TO_CLASS_NAME
                            , ScanSetting.REGISTER_METHOD_NAME
                            , "(Ljava/lang/String;)V"
                            , false)
                }
            }
            super.visitInsn(opcode)
        }

        @Override
        void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }
    }
}