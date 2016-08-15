import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import org.apache.commons.io.FileUtils

/**
 * 注入代码分为两种情况，一种是目录，需要遍历里面的class进行注入
 * 另外一种是jar包，需要先解压jar包，注入代码之后重新打包成jar
 * */
public class Inject {

    private static ClassPool pool = ClassPool.getDefault()

    /**
     * 添加classPath到ClassPool
     * @param libPath
     */
    public static void appendClassPath(String libPath) {
        pool.appendClassPath(libPath)
    }

    /**
     * 遍历该目录下的所有class，对所有class进行代码注入。
     * 其中以下class是不需要注入代码的：
     * --- 1. R文件相关
     * --- 2. 配置文件相关（BuildConfig）
     * --- 3. Application
     * @param path 目录的路径
     */
    public static void injectDir(String path) {
        pool.appendClassPath(path)
        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->

                String filePath = file.absolutePath
                if (filePath.endsWith(".class") && !filePath.contains('R$') &&
                        !filePath.contains('R.class') &&
                        !filePath.contains("BuildConfig.class")
                        // 这里是application的名字，可以通过解析清单文件获得，先写死了
                        &&
                        !filePath.contains("HotPatchApplication.class")) {
                    // 这里是应用包名，也能从清单文件中获取，先写死
                    int index = filePath.indexOf("com\\camnter\\newlife")
                    if (index != -1) {
                        int end = filePath.length() - 6
                        // .class = 6
                        String className = filePath.substring(index, end).
                                replace('\\', '.').
                                replace('/', '.')
                        injectClass(className, path)
                    }
                }
            }
        }
    }

    /**
     * 这里需要将jar包先解压，注入代码后再重新生成jar包
     * @path jar包的绝对路径
     */
    public static void injectJar(String path) {
        if (path.endsWith(".jar")) {
            File jarFile = new File(path)

            // jar包解压后的保存路径
            String jarZipDir = jarFile.getParent() + "/" + jarFile.getName().replace('.jar', '')

            // 解压jar包, 返回jar包中所有class的完整类名的集合（带.class后缀）
            List classNameList = JarZipUtil.unzipJar(path, jarZipDir)

            // 删除原来的jar包
            jarFile.delete()

            // 注入代码
            pool.appendClassPath(jarZipDir)
            for (String className : classNameList) {
                if (className.endsWith(".class") && !className.contains('R$') &&
                        !className.contains('R.class') &&
                        !className.contains("BuildConfig.class")) {
                    className = className.substring(0, className.length() - 6)
                    injectClass(className, jarZipDir)
                }
            }

            // 从新打包jar
            JarZipUtil.zipJar(jarZipDir, path)

            // 删除目录
            FileUtils.deleteDirectory(new File(jarZipDir))
        }
    }

    private static void injectClass(String className, String path) {
        CtClass c = pool.getCtClass(className)
        if (c.isFrozen()) {
            c.defrost()
        }

        CtConstructor[] cts = c.getDeclaredConstructors()

        if (cts == null || cts.length == 0) {
            insertNewConstructor(c)
        } else {
            cts[0].insertBeforeBody("System.out.println(com.camnter.hack.AntilazyLoad.class);")
        }
        c.writeFile(path)
        c.detach()
    }

    private static void insertNewConstructor(CtClass c) {
        CtConstructor constructor = new CtConstructor(new CtClass[0], c)
        constructor.insertBeforeBody("System.out.println(com.camnter.hack.AntilazyLoad.class);")
        c.addConstructor(constructor)
    }
}