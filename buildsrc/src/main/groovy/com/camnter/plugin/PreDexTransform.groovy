import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project

public class PreDexTransform extends Transform {

    Project project

    // 添加构造，为了方便从 plugin 中拿到 project 对象，待会有用
    public PreDexTransform(Project project) {
        this.project = project
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

    /**
     * Executes the Transform.
     *
     * <p/>
     * The inputs are
     * <ul>
     *     <li>The <var>inputs</var> collection of {@link TransformInput}. These are the inputs
     *     that are consumed by this Transform. A transformed version of these inputs must
     *     be written into the output. What is received is controlled through
     * {@link #getInputTypes()}, and {@link #getScopes()}.</li>
     *     <li>The <var>referencedInputs</var> collection of {@link TransformInput}. This is
     *     for reference only and should be not be transformed. What is received is controlled
     *     through {@link #getReferencedScopes()}.</li>
     * </ul>
     *
     * A transform that does not want to consume anything but instead just wants to see the content
     * of some inputs should return an empty set in {@link #getScopes()}, and what it wants to
     * see in {@link #getReferencedScopes()}.
     *
     * <p/>
     * Even though a transform's {@link Transform#isIncremental()} returns true, this method may
     * be receive <code>false</code> in <var>isIncremental</var>. This can be due to
     * <ul>
     *     <li>a change in secondary files ({@link #getSecondaryFileInputs()},
     * {@link #getSecondaryFileOutputs()}, {@link #getSecondaryDirectoryOutputs()})</li>
     *     <li>a change to a non file input ({@link #getParameterInputs()})</li>
     *     <li>an unexpected change to the output files/directories. This should not happen unless
     *     tasks are improperly configured and clobber each other's output.</li>
     *     <li>a file deletion that the transform mechanism could not match to a previous input.
     *     This should not happen in most case, except in some cases where dependencies have
     *     changed.</li>
     * </ul>
     * In such an event, when <var>isIncremental</var> is false, the inputs will not have any
     * incremental change information:
     * <ul>
     *     <li>{@link JarInput#getStatus()} will return {@link Status#NOTCHANGED} even though
     *     the file may be added/changed.</li>
     *     <li>{@link DirectoryInput#getChangedFiles()} will return an empty map even though
     *     some files may be added/changed.</li>
     * </ul>
     *
     * @param context the context in which the transform is run.
     * @param inputs the inputs/outputs of the transform.
     * @param referencedInputs the referenced-only inputs.
     * @param outputProvider the output provider allowing to create content.
     * @param isIncremental whether the transform execution is incremental.
     * @throws IOException if an IO error occurs.
     * @throws InterruptedException
     * @throws TransformException Generic exception encapsulating the cause.
     */
    @Override
    void transform(Context context, Collection<TransformInput> inputs,
            Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider,
            boolean isIncremental) throws IOException, TransformException, InterruptedException {
        // inputs 就是输入文件的集合
        // outputProvider可以获取 outputs 的路径

        // Transfrom的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each {TransformInput input ->

            input.directoryInputs.each {DirectoryInput directoryInput->

                //TODO 这里可以对input的文件做处理，比如代码注入！

                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            input.jarInputs.each {JarInput jarInput->

                //TODO 这里可以对input的文件做处理，比如代码注入！

                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if(jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0,jarName.length()-4)
                }
                def dest = outputProvider.getContentLocation(jarName+md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}