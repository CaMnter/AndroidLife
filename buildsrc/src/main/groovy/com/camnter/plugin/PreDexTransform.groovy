import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project

public class PreDexTransform extends Transform {

    Project project

    // 添加构造，为了方便从 plugin 中拿到 project 对象，待会有用
    public PreDexTransform(Project project) {
        this.project = project

        // 获取到 hack module 的 debug 目录，也就是 Antilazy.class 所在的目录
        def libPath = project.project(':hack').buildDir.absolutePath.
                concat("\\intermediates\\classes\\debug")
        Inject.appendClassPath(libPath)
        Inject.appendClassPath("/Users/CaMnter/Android/adt-bundle-mac-x86_64-20140702/sdk/platforms/android-23/android.jar")
    }

    /**
     * Returns the unique name of the transform.
     *
     * <p/>
     * This is associated with the type of work that the transform does. It does not have to be
     * unique per variant.*/
    // Transform 在 Task 列表中的名字
    // TransformClassesWithPreDexForXXXX
    @Override
    String getName() {
        return "preDex"
    }

    /**
     * Returns the type(s) of data that is consumed by the Transform. This may be more than
     * one type.
     *
     * <strong>This must be of type {@link QualifiedContent.DefaultContentType}</strong>*/
    // 指定 input 的类型
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * Returns the scope(s) of the Transform. This indicates which scopes the transform consumes.*/
    // 指定 Transform 的作用范围
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * Returns whether the Transform can perform incremental work.
     *
     * <p/>
     * If it does, then the TransformInput may contain a list of changed/removed/added files, unless
     * something else triggers a non incremental run.*/
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
            Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider,
            boolean isIncremental) throws IOException, TransformException, InterruptedException {
        // inputs 就是输入文件的集合
        // outputProvider可以获取 outputs 的路径

        // 遍历 transform 的inputs
        // inputs有两种类型，一种是目录，一种是jar，需要分别遍历。
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->

                //TODO 注入代码
                Inject.injectDir(directoryInput.file.absolutePath)

                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            input.jarInputs.each { JarInput jarInput ->

                //TODO 注入代码
                String jarPath = jarInput.file.absolutePath;
                String projectName = project.rootProject.name;
                if (jarPath.endsWith("classes.jar") && jarPath.contains(
                        "exploded-aar\\" + projectName)// hotpatch module 是用来加载dex，无需注入代码
                        && !jarPath.contains("exploded-aar\\" + projectName + "\\hotpatch")) {
                    Inject.injectJar(jarPath)
                }

                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}
