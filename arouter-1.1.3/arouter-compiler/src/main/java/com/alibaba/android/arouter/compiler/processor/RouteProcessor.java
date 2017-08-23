package com.alibaba.android.arouter.compiler.processor;

import com.alibaba.android.arouter.compiler.utils.Consts;
import com.alibaba.android.arouter.compiler.utils.Logger;
import com.alibaba.android.arouter.compiler.utils.TypeUtils;
import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.enums.RouteType;
import com.alibaba.android.arouter.facade.model.RouteMeta;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
import javax.lang.model.util.Types;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import static com.alibaba.android.arouter.compiler.utils.Consts.ACTIVITY;
import static com.alibaba.android.arouter.compiler.utils.Consts.ANNOTATION_TYPE_AUTOWIRED;
import static com.alibaba.android.arouter.compiler.utils.Consts.ANNOTATION_TYPE_ROUTE;
import static com.alibaba.android.arouter.compiler.utils.Consts.FRAGMENT;
import static com.alibaba.android.arouter.compiler.utils.Consts.IPROVIDER_GROUP;
import static com.alibaba.android.arouter.compiler.utils.Consts.IROUTE_GROUP;
import static com.alibaba.android.arouter.compiler.utils.Consts.ITROUTE_ROOT;
import static com.alibaba.android.arouter.compiler.utils.Consts.KEY_MODULE_NAME;
import static com.alibaba.android.arouter.compiler.utils.Consts.METHOD_LOAD_INTO;
import static com.alibaba.android.arouter.compiler.utils.Consts.NAME_OF_GROUP;
import static com.alibaba.android.arouter.compiler.utils.Consts.NAME_OF_PROVIDER;
import static com.alibaba.android.arouter.compiler.utils.Consts.NAME_OF_ROOT;
import static com.alibaba.android.arouter.compiler.utils.Consts.PACKAGE_OF_GENERATE_FILE;
import static com.alibaba.android.arouter.compiler.utils.Consts.SEPARATOR;
import static com.alibaba.android.arouter.compiler.utils.Consts.SERVICE;
import static com.alibaba.android.arouter.compiler.utils.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * A processor used for find route.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/15 下午10:08
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
@SupportedAnnotationTypes({ ANNOTATION_TYPE_ROUTE, ANNOTATION_TYPE_AUTOWIRED })
public class RouteProcessor extends AbstractProcessor {

    /**
     * RouteMeta 缓存
     *
     * key   = group name
     * value = RouteMeta 集合
     */
    private Map<String, Set<RouteMeta>> groupMap = new HashMap<>(); // ModuleName and routeMeta.
    /**
     * key   = group name
     * value = JavaPoet 生成的 java file name（ ARouter$$Group$$??? ）
     */
    private Map<String, String> rootMap = new TreeMap<>();
    // 用于生产 .java 文件
    private Filer mFiler;       // File util, write class file into disk.
    // 用于在 annotationProcessor 过程中打 log
    private Logger logger;
    // 用于鉴别 类型
    private Types types;
    // 用于获取 Element 或 类型
    private Elements elements;
    // 用于转换 Element 的类型为 TypeKind enum 类型（ 自定义枚举类型 ）
    private TypeUtils typeUtils;
    /*
     * 用于获取 ProcessingEnvironment#getOptions()
     * key = moduleName 的值
     * 即，@SupportedOptions(KEY_MODULE_NAME) 加入到 options 的值
     */
    private String moduleName = null;   // Module name, maybe its 'app' or others
    // 保存 IProvider 的 TypeMirror
    private TypeMirror iProvider = null;


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
     * 3. 保存 IProvider 的 TypeMirror
     *
     * @param processingEnv environment to access facilities the tool framework
     * provides to the processor
     * @throws IllegalStateException if this method is called more than once.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mFiler = processingEnv.getFiler();                  // Generate class.
        types = processingEnv.getTypeUtils();            // Get type utils.
        elements = processingEnv.getElementUtils();      // Get class meta.

        typeUtils = new TypeUtils(types, elements);
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

        iProvider = elements.getTypeElement(Consts.IPROVIDER).asType();

        logger.info(">>> RouteProcessor init. <<<");
    }


    /**
     * Annotation Processor 的处理方法（ 覆写 ）
     *
     * 处理 @Route 元素
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
            Set<? extends Element> routeElements = roundEnv.getElementsAnnotatedWith(Route.class);
            try {
                logger.info(">>> Found routes, start... <<<");
                this.parseRoutes(routeElements);

            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }

        return false;
    }


    /**
     * 处理 @Route 元素
     *
     * 0. 准备一下生成 ARouter$$Group$$???，ARouter$$Providers$$??? 和 ARouter$$Root$$???
     * -  java class 需要的 TypeMirror，TypeElement，ClassName，ParameterizedTypeName，ParameterSpec
     * -  以及 MethodSpec
     *
     * 1. 遍历每一个 @Route 元素
     * 2. 拿到每一个 @Route 元素 的 TypeMirror
     *
     * 3.1 判断是否是 Activity 的子类，是的话
     * -   3.1.1 获取该 Element 下的所有 子元素，提取出所有 @Autowired 的 子元素
     * -   3.1.2 根据 所有 @Autowired 的 子元素，拿到 所有 @Autowired 的 子元素 的 TypeMirror
     * -   3.1.3 根据 所有 @Autowired 的 TypeMirror，获取到 所有 @Autowired 的 子元素 的 TypeKind enum 类型
     * -   3.1.4 key = @Autowired 的 name，value = TypeKind enum 类型。缓存成一个 HashMap
     * -   3.1.5 用该 HashMap 生成一个 RouteType = Activity 的 RouteMeta
     * 3.2 判断是否是 IProvider 的子类，是的话，生成一个 RouteType = IProvider 的 RouteMeta
     * 3.3 判断是否是 Service 的子类，是的话，生成一个 RouteType = Service 的 RouteMeta
     * 3.4 判断是否是 Fragment 的子类，是的话，生成一个 RouteType = Fragment 的 RouteMeta
     *
     * 4. 将 RouteMeta 归类
     *
     * 5. 生成 ARouter$$Group$$??? java class 及其中内容
     * 6. 生成 ARouter$$Providers$$??? java class 及其中内容
     * 7. 生成 ARouter$$Root$$??? java class 及其中内容
     *
     * @param routeElements Set<? extends Element>
     * @throws IOException IOException
     */
    private void parseRoutes(Set<? extends Element> routeElements) throws IOException {
        if (CollectionUtils.isNotEmpty(routeElements)) {
            // Perpare the type an so on.

            logger.info(">>> Found routes, size is " + routeElements.size() + " <<<");

            rootMap.clear();

            /**
             * 实例化 android.app.Activity 的 TypeMirror
             * 实例化 android.app.Service 的 TypeMirror
             * 实例化 android.app.Fragment 的 TypeMirror
             * 实例化 android.support.v4.app.Fragment 的 TypeMirror
             */
            TypeMirror type_Activity = elements.getTypeElement(ACTIVITY).asType();
            TypeMirror type_Service = elements.getTypeElement(SERVICE).asType();
            TypeMirror fragmentTm = elements.getTypeElement(FRAGMENT).asType();
            TypeMirror fragmentTmV4 = elements.getTypeElement(Consts.FRAGMENT_V4).asType();

            /**
             * Interface of ARouter
             *
             * 实例化 IRouteGroup 的 TypeElement
             * 实例化 IProviderGroup 的 TypeElement
             * 实例化 RouteMeta 的 ClassName
             * 实例化 RouteType 的 ClassName
             */
            TypeElement type_IRouteGroup = elements.getTypeElement(IROUTE_GROUP);
            TypeElement type_IProviderGroup = elements.getTypeElement(IPROVIDER_GROUP);
            ClassName routeMetaCn = ClassName.get(RouteMeta.class);
            ClassName routeTypeCn = ClassName.get(RouteType.class);

            /**
             * Build input type, format as :
             *
             * ```Map<String, Class<? extends IRouteGroup>>```
             *
             * （ 用于 ARouter$$Root$$??? ）
             *  @Override
             *  public void loadInto(Map<String, Class<? extends IRouteGroup>> routes)
             *  中的参数类型
             *  Map<String, Class<? extends IRouteGroup>>
             */
            ParameterizedTypeName inputMapTypeOfRoot = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(
                    ClassName.get(Class.class),
                    WildcardTypeName.subtypeOf(ClassName.get(type_IRouteGroup))
                )
            );

            /**
             * ```Map<String, RouteMeta>```
             *
             * （ 用于 ARouter$$Group$$??? ）
             *  @Override
             *  public void loadInto(Map<String, RouteMeta> atlas)
             *  中的参数类型
             *  Map<String, RouteMeta>
             */
            ParameterizedTypeName inputMapTypeOfGroup = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouteMeta.class)
            );

            /**
             * Build input param name.
             *
             * 实例化参数：
             *
             * Map<String, Class<? extends IRouteGroup>> routes（ 用于 ARouter$$Root$$??? ）
             * Map<String, RouteMeta> atlas（ 用于 ARouter$$Group$$??? ）
             * Map<String, RouteMeta> providers（ 用于 ARouter$$Providers$$??? ）
             */
            ParameterSpec rootParamSpec = ParameterSpec.builder(inputMapTypeOfRoot, "routes")
                .build();
            ParameterSpec groupParamSpec = ParameterSpec.builder(inputMapTypeOfGroup, "atlas")
                .build();
            ParameterSpec providerParamSpec = ParameterSpec.builder(inputMapTypeOfGroup,
                "providers").build();  // Ps. its param type same as groupParamSpec!

            /**
             * Build method : 'loadInto'
             *
             * 整个 loadInto 方法（ 用于 ARouter$$Root$$??? ）
             * @Override
             * public void loadInto(Map<String, Class<? extends IRouteGroup>> routes)
             */
            MethodSpec.Builder loadIntoMethodOfRootBuilder = MethodSpec.methodBuilder(
                METHOD_LOAD_INTO)
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(rootParamSpec);

            /**
             * Follow a sequence, find out metas of group first, generate java file, then statistics them as root.
             *
             * 1. 遍历每一个 @Route 元素
             * 2. 拿到每一个 @Route 元素 的 TypeMirror
             *
             * 3.1 判断是否是 Activity 的子类，是的话
             * -   3.1.1 获取该 Element 下的所有 子元素，提取出所有 @Autowired 的 子元素
             * -   3.1.2 根据 所有 @Autowired 的 子元素，拿到 所有 @Autowired 的 子元素 的 TypeMirror
             * -   3.1.3 根据 所有 @Autowired 的 TypeMirror，获取到 所有 @Autowired 的 子元素 的 TypeKind enum 类型
             * -   3.1.4 key = @Autowired 的 name，value = TypeKind enum 类型。缓存成一个 HashMap
             * -   3.1.5 用该 HashMap 生成一个 RouteType = Activity 的 RouteMeta
             * 3.2 判断是否是 IProvider 的子类，是的话，生成一个 RouteType = IProvider 的 RouteMeta
             * 3.3 判断是否是 Service 的子类，是的话，生成一个 RouteType = Service 的 RouteMeta
             * 3.4 判断是否是 Fragment 的子类，是的话，生成一个 RouteType = Fragment 的 RouteMeta
             *
             * 4. 将 RouteMeta 归类
             *
             * 5. 生成 ARouter$$Group$$??? java class 及其中内容
             * 6. 生成 ARouter$$Providers$$??? java class 及其中内容
             * 7. 生成 ARouter$$Root$$??? java class 及其中内容
             */
            for (Element element : routeElements) {
                TypeMirror tm = element.asType();
                Route route = element.getAnnotation(Route.class);
                RouteMeta routeMete = null;

                if (types.isSubtype(tm, type_Activity)) {                 // Activity
                    logger.info(">>> Found activity route: " + tm.toString() + " <<<");

                    // Get all fields annotation by @Autowired
                    Map<String, Integer> paramsType = new HashMap<>();
                    for (Element field : element.getEnclosedElements()) {
                        if (field.getKind().isField() &&
                            field.getAnnotation(Autowired.class) != null &&
                            !types.isSubtype(field.asType(), iProvider)) {
                            // It must be field, then it has annotation, but it not be provider.
                            Autowired paramConfig = field.getAnnotation(Autowired.class);
                            paramsType.put(
                                StringUtils.isEmpty(paramConfig.name()) ? field.getSimpleName()
                                    .toString() : paramConfig.name(),
                                typeUtils.typeExchange(field));
                        }
                    }
                    routeMete = new RouteMeta(route, element, RouteType.ACTIVITY, paramsType);
                } else if (types.isSubtype(tm, iProvider)) {         // IProvider
                    logger.info(">>> Found provider route: " + tm.toString() + " <<<");
                    routeMete = new RouteMeta(route, element, RouteType.PROVIDER, null);
                } else if (types.isSubtype(tm, type_Service)) {           // Service
                    logger.info(">>> Found service route: " + tm.toString() + " <<<");
                    routeMete = new RouteMeta(route, element, RouteType.parse(SERVICE), null);
                } else if (types.isSubtype(tm, fragmentTm) || types.isSubtype(tm, fragmentTmV4)) {
                    logger.info(">>> Found fragment route: " + tm.toString() + " <<<");
                    routeMete = new RouteMeta(route, element, RouteType.parse(FRAGMENT), null);
                }

                categories(routeMete);
                // if (StringUtils.isEmpty(moduleName)) {   // Hasn't generate the module name.
                //     moduleName = ModuleUtils.generateModuleName(element, logger);
                // }
            }

            /**
             * Build method : 'loadInto'
             *
             * 整个 loadInto 方法（ 用于 ARouter$$Providers$$??? ）
             * @Override
             * public void loadInto(Map<String, RouteMeta> providers)
             */
            MethodSpec.Builder loadIntoMethodOfProviderBuilder = MethodSpec.methodBuilder(
                METHOD_LOAD_INTO)
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(providerParamSpec);

            /**
             * Start generate java source, structure is divided into upper and lower levels, used for demand initialization.
             */
            for (Map.Entry<String, Set<RouteMeta>> entry : groupMap.entrySet()) {
                String groupName = entry.getKey();

                /**
                 * 整个 loadInto 方法（ 用于 ARouter$$Group$$??? ）
                 * @Override
                 * public void loadInto(Map<String, RouteMeta> atlas)
                 */
                MethodSpec.Builder loadIntoMethodOfGroupBuilder = MethodSpec.methodBuilder(
                    METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(groupParamSpec);

                // Build group method body
                Set<RouteMeta> groupData = entry.getValue();
                for (RouteMeta routeMeta : groupData) {
                    switch (routeMeta.getType()) {
                        /**
                         * 在 ARouter$$Providers$$??? # loadInto(Map<String, RouteMeta> providers)
                         * 添加 缓存语句
                         * eg:
                         * providers.put("com.alibaba.android.arouter.demo.testservice.HelloService", RouteMeta.build(RouteType.PROVIDER, HelloServiceImpl.class, "/service/hello", "service", null, -1, -2147483648));
                         */
                        case PROVIDER:  // Need cache provider's super class
                            List<? extends TypeMirror> interfaces
                                = ((TypeElement) routeMeta.getRawType()).getInterfaces();
                            for (TypeMirror tm : interfaces) {
                                if (types.isSameType(tm,
                                    iProvider)) {   // Its implements iProvider interface himself.
                                    // This interface extend the IProvider, so it can be used for mark provider
                                    loadIntoMethodOfProviderBuilder.addStatement(
                                        "providers.put($S, $T.build($T." + routeMeta.getType() +
                                            ", $T.class, $S, $S, null, " + routeMeta.getPriority() +
                                            ", " + routeMeta.getExtra() + "))",
                                        (routeMeta.getRawType()).toString(),
                                        routeMetaCn,
                                        routeTypeCn,
                                        ClassName.get((TypeElement) routeMeta.getRawType()),
                                        routeMeta.getPath(),
                                        routeMeta.getGroup());
                                } else if (types.isSubtype(tm, iProvider)) {
                                    // This interface extend the IProvider, so it can be used for mark provider
                                    loadIntoMethodOfProviderBuilder.addStatement(
                                        "providers.put($S, $T.build($T." + routeMeta.getType() +
                                            ", $T.class, $S, $S, null, " + routeMeta.getPriority() +
                                            ", " + routeMeta.getExtra() + "))",
                                        tm.toString(),
                                        // So stupid, will duplicate only save class name.
                                        routeMetaCn,
                                        routeTypeCn,
                                        ClassName.get((TypeElement) routeMeta.getRawType()),
                                        routeMeta.getPath(),
                                        routeMeta.getGroup());
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    /**
                     * Make map body for paramsType
                     *
                     * 在 ARouter$$Group$$??? # loadInto(Map<String, RouteMeta> atlas)
                     * 中的 缓存语句 键值对语句
                     * eg:
                     * atlas.put("/test/activity1", RouteMeta.build(RouteType.ACTIVITY, Test1Activity.class, "/test/activity1", "test", new java.util.HashMap<String, Integer>(){{put("pac", 9); put("obj", 10); put("name", 8); put("boy", 0); put("age", 3); put("url", 8); }}, -1, -2147483648));
                     * 键值对语句：
                     * put("pac", 9); put("obj", 10); put("name", 8); put("boy", 0); put("age", 3); put("url", 8);
                     */
                    StringBuilder mapBodyBuilder = new StringBuilder();
                    Map<String, Integer> paramsType = routeMeta.getParamsType();
                    if (MapUtils.isNotEmpty(paramsType)) {
                        for (Map.Entry<String, Integer> types : paramsType.entrySet()) {
                            mapBodyBuilder.append("put(\"")
                                .append(types.getKey())
                                .append("\", ")
                                .append(types.getValue())
                                .append("); ");
                        }
                    }
                    String mapBody = mapBodyBuilder.toString();

                    /**
                     * 在 ARouter$$Group$$??? # loadInto(Map<String, RouteMeta> atlas)
                     * 添加 缓存语句
                     * eg:
                     * atlas.put("/test/activity1", RouteMeta.build(RouteType.ACTIVITY, Test1Activity.class, "/test/activity1", "test", new java.util.HashMap<String, Integer>(){{put("pac", 9); put("obj", 10); put("name", 8); put("boy", 0); put("age", 3); put("url", 8); }}, -1, -2147483648));
                     */
                    loadIntoMethodOfGroupBuilder.addStatement(
                        "atlas.put($S, $T.build($T." + routeMeta.getType() +
                            ", $T.class, $S, $S, " + (StringUtils.isEmpty(mapBody)
                                                      ? null
                                                      : ("new java.util.HashMap<String, Integer>(){{" +
                                                          mapBodyBuilder.toString() + "}}")) +
                            ", " + routeMeta.getPriority() + ", " + routeMeta.getExtra() + "))",
                        routeMeta.getPath(),
                        routeMetaCn,
                        routeTypeCn,
                        ClassName.get((TypeElement) routeMeta.getRawType()),
                        routeMeta.getPath().toLowerCase(),
                        routeMeta.getGroup().toLowerCase());
                }

                /**
                 * Generate groups
                 *
                 * 生成 java class
                 * public class ARouter$$Group$$??? implements IRouteGroup
                 */
                String groupFileName = NAME_OF_GROUP + groupName;
                JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(groupFileName)
                        .addJavadoc(WARNING_TIPS)
                        .addSuperinterface(ClassName.get(type_IRouteGroup))
                        .addModifiers(PUBLIC)
                        .addMethod(loadIntoMethodOfGroupBuilder.build())
                        .build()
                ).build().writeTo(mFiler);

                logger.info(">>> Generated group: " + groupName + "<<<");
                rootMap.put(groupName, groupFileName);
            }

            /**
             * 在 ARouter$$Root$$??? # (Map<String, Class<? extends IRouteGroup>> routes)
             * 添加 缓存语句
             * eg:
             * routes.put("service", ARouter$$Group$$service.class);
             */
            if (MapUtils.isNotEmpty(rootMap)) {
                // Generate root meta by group name, it must be generated before root, then I can find out the class of group.
                for (Map.Entry<String, String> entry : rootMap.entrySet()) {
                    loadIntoMethodOfRootBuilder.addStatement("routes.put($S, $T.class)",
                        entry.getKey(), ClassName.get(PACKAGE_OF_GENERATE_FILE, entry.getValue()));
                }
            }

            /**
             * Write provider into disk
             *
             * 生成 java class
             * public class ARouter$$Providers$$??? implements IProviderGroup
             */
            String providerMapFileName = NAME_OF_PROVIDER + SEPARATOR + moduleName;
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                TypeSpec.classBuilder(providerMapFileName)
                    .addJavadoc(WARNING_TIPS)
                    .addSuperinterface(ClassName.get(type_IProviderGroup))
                    .addModifiers(PUBLIC)
                    .addMethod(loadIntoMethodOfProviderBuilder.build())
                    .build()
            ).build().writeTo(mFiler);

            logger.info(">>> Generated provider map, name is " + providerMapFileName + " <<<");

            /**
             * Write provider into disk
             *
             * 生成 java class
             * public class ARouter$$Root$$??? implements IRouteRoot
             */
            String rootFileName = NAME_OF_ROOT + SEPARATOR + moduleName;
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                TypeSpec.classBuilder(rootFileName)
                    .addJavadoc(WARNING_TIPS)
                    .addSuperinterface(ClassName.get(elements.getTypeElement(ITROUTE_ROOT)))
                    .addModifiers(PUBLIC)
                    .addMethod(loadIntoMethodOfRootBuilder.build())
                    .build()
            ).build().writeTo(mFiler);

            logger.info(">>> Generated root, name is " + rootFileName + " <<<");
        }
    }


    /**
     * Sort metas in group.
     *
     * 将 RouteMeta 分类
     * 1. 验证 RouteMeta 数据
     * 2. 根据 RouteMeta group 去，RouteMeta 缓存 中查找 RouteMeta 的集合
     * 3.1 如果不存在集合，则创建集合，并且添加 RouteMeta 后，将集合添加到 RouteMeta 的集合
     * 3.2 如果存在集合，添加 RouteMeta 后，将集合添加到 RouteMeta 的集合
     *
     * 注: 这些集合，都是 TreeMap，根据 RouteMeta path 进行排序的
     *
     * @param routeMete metas.
     */
    private void categories(RouteMeta routeMete) {
        // 验证 RouteMeta 数据
        if (routeVerify(routeMete)) {
            logger.info(">>> Start categories, group = " + routeMete.getGroup() + ", path = " +
                routeMete.getPath() + " <<<");
            // 根据 RouteMeta group 去，RouteMeta 缓存 中查找 RouteMeta 的集合
            Set<RouteMeta> routeMetas = groupMap.get(routeMete.getGroup());
            /*
             * 如果不存在集合，则创建集合，并且添加 RouteMeta 后，将集合添加到 RouteMeta 的集合
             * 如果存在集合，添加 RouteMeta 后，将集合添加到 RouteMeta 的集合
             */
            if (CollectionUtils.isEmpty(routeMetas)) {
                Set<RouteMeta> routeMetaSet = new TreeSet<>(new Comparator<RouteMeta>() {
                    @Override
                    public int compare(RouteMeta r1, RouteMeta r2) {
                        try {
                            return r1.getPath().compareTo(r2.getPath());
                        } catch (NullPointerException npe) {
                            logger.error(npe.getMessage());
                            return 0;
                        }
                    }
                });
                routeMetaSet.add(routeMete);
                groupMap.put(routeMete.getGroup(), routeMetaSet);
            } else {
                routeMetas.add(routeMete);
            }
        } else {
            logger.warning(
                ">>> Route meta verify error, group is " + routeMete.getGroup() + " <<<");
        }
    }


    /**
     * Verify the route meta
     *
     * 验证 RouteMeta 数据
     *
     * 1. 验证 RouteMeta path 是否以 / 开头
     * 2. 验证 RouteMeta group 是否为 null or ""
     *
     * @param meta raw meta
     */
    private boolean routeVerify(RouteMeta meta) {
        String path = meta.getPath();

        // 验证 RouteMeta path 是否以 / 开头
        if (StringUtils.isEmpty(path) ||
            !path.startsWith("/")) {   // The path must be start with '/' and not empty!
            return false;
        }

        // 验证 RouteMeta group 是否为 null or ""
        if (StringUtils.isEmpty(meta.getGroup())) { // Use default group(the first word in path)
            try {
                String defaultGroup = path.substring(1, path.indexOf("/", 1));
                if (StringUtils.isEmpty(defaultGroup)) {
                    return false;
                }

                meta.setGroup(defaultGroup);
                return true;
            } catch (Exception e) {
                logger.error("Failed to extract default group! " + e.getMessage());
                return false;
            }
        }

        return true;
    }

}
