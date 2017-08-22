package com.alibaba.android.arouter.compiler.processor;

import com.alibaba.android.arouter.compiler.utils.Consts;
import com.alibaba.android.arouter.compiler.utils.Logger;
import com.alibaba.android.arouter.facade.annotation.Interceptor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import static com.alibaba.android.arouter.compiler.utils.Consts.ANNOTATION_TYPE_INTECEPTOR;
import static com.alibaba.android.arouter.compiler.utils.Consts.IINTERCEPTOR;
import static com.alibaba.android.arouter.compiler.utils.Consts.IINTERCEPTOR_GROUP;
import static com.alibaba.android.arouter.compiler.utils.Consts.KEY_MODULE_NAME;
import static com.alibaba.android.arouter.compiler.utils.Consts.METHOD_LOAD_INTO;
import static com.alibaba.android.arouter.compiler.utils.Consts.NAME_OF_INTERCEPTOR;
import static com.alibaba.android.arouter.compiler.utils.Consts.PACKAGE_OF_GENERATE_FILE;
import static com.alibaba.android.arouter.compiler.utils.Consts.SEPARATOR;
import static com.alibaba.android.arouter.compiler.utils.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Process the annotation of #{@link Interceptor}
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/23 14:11
 *
 * {@link InterceptorProcessor#init(ProcessingEnvironment)}
 * 初始化方法（ 覆写 ）
 * 1. 初始化所有工具
 * 2. 从 Options 中获取 moduleName
 * 3. 保存 IInterceptor 的 TypeMirror
 *
 * {@link InterceptorProcessor#process(Set, RoundEnvironment)}
 * 处理 @Interceptor 元素
 *
 * {@link InterceptorProcessor#parseInterceptors(Set)}
 * 处理 @Interceptor 元素
 * 1. 编译每个 @Interceptor 元素
 * 2. 验证每个 @Interceptor 元素
 * 3. 根据优先级 = key，@Interceptor 元素 为 value，缓存到 TreeMap 内
 * 4. JavaPoet 生成 java class
 *
 * {@link InterceptorProcessor#verify(Element)}
 * 验证 @Interceptor 的元素
 * 1. 元素 是否有 @Interceptor
 * 2. 元素是否是 iInterceptor 的 实现类
 */
@SuppressWarnings("DanglingJavadoc")
// annotationProcessor 必备注解，自动运行
@AutoService(Processor.class)
/**
 * module build.gradle
 *
 * android {
 *
 *   ...
 *
 *   defaultConfig {
 *
 *       ...
 *
 *       javaCompileOptions {
 *           annotationProcessorOptions {
 *               arguments = [ moduleName : project.getName() ]
 *           }
 *       }
 *
 *       ...
 *
 *   }
 *
 *   ...
 *
 * }
 *
 * KEY_MODULE_NAME = moduleName
 * 会在{@link ProcessingEnvironment#getOptions()}中，放入
 * 从 build.gradle 设置的  key = moduleName，value = project.getName()
 */
@SupportedOptions(KEY_MODULE_NAME)
// 取代了 覆写 getSupportedSourceVersion
@SupportedSourceVersion(SourceVersion.RELEASE_7)
// 取代了 覆写 getSupportedAnnotationTypes
@SupportedAnnotationTypes(ANNOTATION_TYPE_INTECEPTOR)
public class InterceptorProcessor extends AbstractProcessor {

    /*
     * key   = 拦截器的 优先级
     * value = 拦截器的 Element，类 Element
     */
    private Map<Integer, Element> interceptors = new TreeMap<>();
    // 用于生产 .java 文件
    private Filer mFiler;       // File util, write class file into disk.
    // 用于在 annotationProcessor 过程中打 log
    private Logger logger;
    // 用于获取 Element 或 类型
    private Elements elementUtil;
    /*
     * 用于获取 ProcessingEnvironment#getOptions()
     * key = moduleName 的值
     * 即，@SupportedOptions(KEY_MODULE_NAME) 加入到 options 的值
     */
    private String moduleName = null;   // Module name, maybe its 'app' or others
    // 保存 IInterceptor 的 TypeMirror
    private TypeMirror iInterceptor = null;


    /**
     * Initializes the processor with the processing environment by
     * setting the {@code processingEnv} field to the value of the
     * {@code processingEnv} argument.  An {@code
     * IllegalStateException} will be thrown if this method is called
     * more than once on the same object.
     *
     * 初始化方法（ 覆写 ）
     *
     * 1. 初始化所有工具
     * 2. 从 Options 中获取 moduleName
     * 3. 保存 IInterceptor 的 TypeMirror
     *
     * @param processingEnv environment to access facilities the tool framework
     * provides to the processor
     * @throws IllegalStateException if this method is called more than once.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mFiler = processingEnv.getFiler();                  // Generate class.
        elementUtil = processingEnv.getElementUtils();      // Get class meta.
        logger = new Logger(processingEnv.getMessager());   // Package the log utils.

        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(KEY_MODULE_NAME);
        }

        if (StringUtils.isNotEmpty(moduleName)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
            logger.info("The user has configuration the module name, it was [" + moduleName + "]");
        } else {
            logger.error("These no module name, at 'build.gradle', like :\n" +
                "apt {\n" +
                "    arguments {\n" +
                "        moduleName project.getName();\n" +
                "    }\n" +
                "}\n");
            throw new RuntimeException(
                "ARouter::Compiler >>> No module name, for more information, look at gradle log.");
        }

        iInterceptor = elementUtil.getTypeElement(Consts.IINTERCEPTOR).asType();

        logger.info(">>> InterceptorProcessor init. <<<");
    }


    /**
     * Annotation Processor 的处理方法（ 覆写 ）
     *
     * 处理 @Interceptor 元素
     *
     * @param annotations Set<? extends TypeElement>
     * @param roundEnv RoundEnvironment
     * @return 返回是否这些注释由该处理器声明
     * 如果 true 返回，则会声明注释，并且不会要求后续处理器处理它们
     * 如果 false 返回，注释是无人认领的，后续处理器可能被要求处理它们。处理器可以总是返回相同的布尔值，或者可以根据所选择的标准来改变结果
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (CollectionUtils.isNotEmpty(annotations)) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Interceptor.class);
            try {
                parseInterceptors(elements);
            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }

        return false;
    }


    /**
     * Parse tollgate.
     *
     * 处理 @Interceptor 元素
     *
     * 1. 编译每个 @Interceptor 元素
     * 2. 验证每个 @Interceptor 元素
     * 3. 根据优先级 = key，@Interceptor 元素 为 value，缓存到 TreeMap 内
     * 4. JavaPoet 生成 java class
     *
     * @param elements elements of tollgate.
     * @throws IOException IOException
     */
    private void parseInterceptors(Set<? extends Element> elements) throws IOException {
        if (CollectionUtils.isNotEmpty(elements)) {
            logger.info(">>> Found interceptors, size is " + elements.size() + " <<<");

            /**
             * 1. 编译每个 @Interceptor 元素
             * 2. 验证每个 @Interceptor 元素
             * 3. 根据优先级 = key，@Interceptor 元素 为 value，缓存到 TreeMap 内
             */
            // Verify and cache, sort incidentally.
            for (Element element : elements) {
                if (verify(element)) {  // Check the interceptor meta
                    logger.info("A interceptor verify over, its " + element.asType());
                    Interceptor interceptor = element.getAnnotation(Interceptor.class);

                    Element lastInterceptor = interceptors.get(interceptor.priority());
                    if (null != lastInterceptor) { // Added, throw exceptions
                        throw new IllegalArgumentException(
                            String.format(Locale.getDefault(),
                                "More than one interceptors use same priority [%d], They are [%s] and [%s].",
                                interceptor.priority(),
                                lastInterceptor.getSimpleName(),
                                element.getSimpleName())
                        );
                    }

                    interceptors.put(interceptor.priority(), element);
                } else {
                    logger.error("A interceptor verify failed, its " + element.asType());
                }
            }

            /**
             * 获取 IInterceptor 的 TypeElement
             * 获取 IInterceptorGroup 的 TypeElement
             */
            // Interface of ARouter.
            TypeElement type_ITollgate = elementUtil.getTypeElement(IINTERCEPTOR);
            TypeElement type_ITollgateGroup = elementUtil.getTypeElement(IINTERCEPTOR_GROUP);

            /**
             *  Build input type, format as :
             *
             *  ```Map<Integer, Class<? extends ITollgate>>```
             *
             *  @Override
             *  public void loadInto(Map<Integer, Class<? extends IInterceptor>> interceptors)
             *  中的参数类型
             *  Map<Integer, Class<? extends IInterceptor>>
             */
            ParameterizedTypeName inputMapTypeOfTollgate = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(Integer.class),
                ParameterizedTypeName.get(
                    ClassName.get(Class.class),
                    WildcardTypeName.subtypeOf(ClassName.get(type_ITollgate))
                )
            );

            /**
             *  @Override
             *  public void loadInto(Map<Integer, Class<? extends IInterceptor>> interceptors)
             *  中的参数
             *  Map<Integer, Class<? extends IInterceptor>> interceptors
             */
            // Build input param name.
            ParameterSpec tollgateParamSpec = ParameterSpec.builder(inputMapTypeOfTollgate,
                "interceptors").build();

            /**
             * 整个 loadInto 方法
             *  @Override
             *  public void loadInto(Map<Integer, Class<? extends IInterceptor>> interceptors)
             */
            // Build method : 'loadInto'
            MethodSpec.Builder loadIntoMethodOfTollgateBuilder = MethodSpec.methodBuilder(
                METHOD_LOAD_INTO)
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(tollgateParamSpec);

            /**
             * 添加 拦截器 缓存
             * eg: interceptors.put(7, Test1Interceptor.class);
             */
            // Generate
            if (null != interceptors && interceptors.size() > 0) {
                // Build method body
                for (Map.Entry<Integer, Element> entry : interceptors.entrySet()) {
                    loadIntoMethodOfTollgateBuilder.addStatement(
                        "interceptors.put(" + entry.getKey() + ", $T.class)",
                        ClassName.get((TypeElement) entry.getValue()));
                }
            }

            /**
             * 生成 java class
             *
             * public class ???$$Interceptors$$app implements IInterceptorGroup
             */
            // Write to disk(Write file even interceptors is empty.)
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                TypeSpec.classBuilder(NAME_OF_INTERCEPTOR + SEPARATOR + moduleName)
                    .addModifiers(PUBLIC)
                    .addJavadoc(WARNING_TIPS)
                    .addMethod(loadIntoMethodOfTollgateBuilder.build())
                    .addSuperinterface(ClassName.get(type_ITollgateGroup))
                    .build()
            ).build().writeTo(mFiler);

            logger.info(">>> Interceptor group write over. <<<");
        }

    }


    /**
     * Verify inteceptor meta
     *
     * 验证 @Interceptor 的元素
     *
     * 1. 元素 是否有 @Interceptor
     * 2. 元素是否是 iInterceptor 的 实现类
     *
     * @param element Interceptor taw type
     * @return verify result
     */
    private boolean verify(Element element) {
        Interceptor interceptor = element.getAnnotation(Interceptor.class);
        // It must be implement the interface IInterceptor and marked with annotation Interceptor.
        return null != interceptor &&
            ((TypeElement) element).getInterfaces().contains(iInterceptor);
    }

}
