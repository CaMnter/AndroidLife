package com.camnter.life.asm.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter

/**
 * @author CaMnter
 */

class AsmActivityInject extends BaseInject {

    AsmActivityInject(Project project) {
        super(project)
    }

    @Override
    def inject(DirectoryInput directoryInput) {
        if (!checkoutAppExtension()) return
        def dirFile = directoryInput.file
        if (dirFile.isDirectory()) {
            dirFile.eachFileRecurse {
                def filePath = it.absolutePath
                if (it.name == 'AsmActivity.class') {
                    File optClass = new File(it.getParent(), it.getName() + ".opt")
                    FileInputStream inputStream = null
                    FileOutputStream outputStream = null
                    try {
                        inputStream = new FileInputStream(it)
                        outputStream = new FileOutputStream(optClass)
                        byte[] bytes = referHack(inputStream)
                        outputStream.write(bytes)
                    } catch (Exception e) {
                        e.printStackTrace()
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close()
                            } catch (IOException e) {
                                e.printStackTrace()
                            }
                        }
                        if (outputStream != null) {
                            try {
                                outputStream.close()
                            } catch (IOException e) {
                                e.printStackTrace()
                            }
                        }
                    }
                    if (it.exists()) {
                        it.delete()
                    }
                    optClass.renameTo(it)
                }
            }
        }
    }

    static byte[] referHack(InputStream inputStream) {
        try {
            ClassReader classReader = new ClassReader(inputStream)
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            ClassVisitor changeVisitor = new AsmActivityVisitor(classWriter)
            classReader.accept(changeVisitor, ClassReader.EXPAND_FRAMES)
            return classWriter.toByteArray()
        } catch (IOException e) {
            e.printStackTrace()
        } finally {
        }
        return null
    }

    static class AsmActivityVisitor extends ClassVisitor {

        private String owner
        private ActivityAnnotationVisitor fileAnnotationVisitor

        AsmActivityVisitor(ClassVisitor classVisitor) {
            super(Opcodes.ASM5, classVisitor)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName,
                String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            this.owner = name
        }

        @Override
        AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            AnnotationVisitor annotationVisitor = super.visitAnnotation(desc, visible)
            if (desc != null) {
                fileAnnotationVisitor =
                        new ActivityAnnotationVisitor(Opcodes.ASM5, annotationVisitor, desc)
                return fileAnnotationVisitor
            }
            return annotationVisitor
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature,
                String[] exceptions) {
            MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions)
            if (fileAnnotationVisitor != null) {
                return new AsmActivityAdviceAdapter(mv, access, owner, name, desc)
            }
            return mv
        }
    }

    static class ActivityAnnotationVisitor extends AnnotationVisitor {
        public String desc
        public String name
        public String value

        ActivityAnnotationVisitor(int api, AnnotationVisitor av, String paramDesc) {
            super(api, av)
            this.desc = paramDesc
        }

        void visit(String paramName, Object paramValue) {
            this.name = paramName
            this.value = paramValue.toString()
        }
    }

    /**
     * public class com.camnter.newlife.ui.activity.asm.AsmActivity extends com.camnter.newlife.core.activity.BaseAppCompatActivity {* public com.camnter.newlife.ui.activity.asm.AsmActivity();
     * Code:
     * 0: aload_0
     * 1: invokespecial #1                  // Method com/camnter/newlife/core/activity/BaseAppCompatActivity."<init>":()V
     * 4: return
     *
     * protected int getLayoutId();
     * Code:
     * 0: ldc           #3                  // int 2130968609
     * 2: ireturn
     *
     *      protected void initViews(android.os.Bundle);
     * Code:
     * 0: aload_0
     * 1: invokestatic  #4                  // Method com/camnter/newlife/ui/activity/asm/ActivityTimeManger.onCreateStart:(Landroid/app/Activity;)V
     * 4: getstatic     #5                  // Field java/lang/System.out:Ljava/io/PrintStream;
     * 7: ldc           #6                  // String 「AsmActivity」   「onCreate」
     * 9: invokevirtual #7                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
     * 12: aload_0
     * 13: invokestatic  #8                  // Method com/camnter/newlife/ui/activity/asm/ActivityTimeManger.onCreateEnd:(Landroid/app/Activity;)V
     * 16: return
     *
     * protected void initListeners();
     * Code:
     * 0: return
     *
     * protected void initData();
     * Code:
     * 0: return
     * */
    class AsmActivityAdviceAdapter extends AdviceAdapter {

        ActivityAnnotationVisitor activityAnnotationVisitor

        protected AsmActivityAdviceAdapter(MethodVisitor mv, int access, String className,
                String name, String desc) {
            super(Opcodes.ASM5, mv, access, name, desc)
            owner = className
        }

        @Override
        AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            AnnotationVisitor annotationVisitor = super.visitAnnotation(desc, visible);
            if (desc != null) {
                activityAnnotationVisitor =
                        new ActivityAnnotationVisitor(Opcodes.ASM5, annotationVisitor, desc);
                return activityAnnotationVisitor
            }
            return annotationVisitor
        }

        @Override
        protected void onMethodEnter() {
            if (activityAnnotationVisitor == null) {
                return
            }
            super.onMethodEnter()
            // aload_0
            mv.visitVarInsn(ALOAD, 0)
            // invokestatic  #4 // Method com/camnter/newlife/ui/activity/asm/ActivityTimeManger.onCreateStart:(Landroid/app/Activity;)V
            mv.visitMethodInsn(INVOKESTATIC,
                    "com/camnter/newlife/ui/activity/asm/ActivityTimeManger",
                    activityAnnotationVisitor.value + "Start",
                    "(Landroid/app/Activity;)V")
        }

        @Override
        protected void onMethodExit(int opcode) {
            if (activityAnnotationVisitor == null) {
                return
            }
            super.onMethodExit(opcode)
            // aload_0
            mv.visitVarInsn(ALOAD, 0)
            // invokestatic  #8 // Method com/camnter/newlife/ui/activity/asm/ActivityTimeManger.onCreateEnd:(Landroid/app/Activity;)V
            mv.visitMethodInsn(INVOKESTATIC,
                    "com/camnter/newlife/ui/activity/asm/ActivityTimeManger",
                    activityAnnotationVisitor.value + "End",
                    "(Landroid/app/Activity;)V")
        }
    }

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
