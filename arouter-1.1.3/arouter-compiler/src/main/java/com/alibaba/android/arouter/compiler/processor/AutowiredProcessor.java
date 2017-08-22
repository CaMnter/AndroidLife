package com.alibaba.android.arouter.compiler.processor;

import com.alibaba.android.arouter.compiler.utils.Consts;
import com.alibaba.android.arouter.compiler.utils.Logger;
import com.alibaba.android.arouter.compiler.utils.TypeUtils;
import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.enums.TypeKind;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import static com.alibaba.android.arouter.compiler.utils.Consts.ANNOTATION_TYPE_AUTOWIRED;
import static com.alibaba.android.arouter.compiler.utils.Consts.ISYRINGE;
import static com.alibaba.android.arouter.compiler.utils.Consts.JSON_SERVICE;
import static com.alibaba.android.arouter.compiler.utils.Consts.KEY_MODULE_NAME;
import static com.alibaba.android.arouter.compiler.utils.Consts.METHOD_INJECT;
import static com.alibaba.android.arouter.compiler.utils.Consts.NAME_OF_AUTOWIRED;
import static com.alibaba.android.arouter.compiler.utils.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Processor used to create autowired helper
 *
 * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
 * @version 1.0
 * @since 2017/2/20 下午5:56
 *
 * {@link AutowiredProcessor#init(ProcessingEnvironment)}
 * 初始化方法（ 覆写 ）
 * 初始化所有工具
 *
 * {@link AutowiredProcessor#process(Set, RoundEnvironment)}
 * Annotation Processor 的处理方法（ 覆写 ）
 * 1. 先对所有 @Autowired 元素 归类整理
 * -  以 类 Element 作为 key，value 为属于该 类 Element 的所有 @Autowired 元素
 * 2. 生成 ???$$ARouter$$Autowired 类
 *
 * {@link AutowiredProcessor#generateHelper()}
 * 生成 ???$$ARouter$$Autowired 类
 *
 * {@link AutowiredProcessor#buildStatement(String, int, boolean)}
 * 根据不同的 TypeKind 类型，生成不同的 intent getter 语句
 *
 * {@link AutowiredProcessor#categories(Set)}
 * 将 所有 @Autowired 元素 归类
 * 以 类 Element 作为 key，value 为属于该 类 Element 的所有 @Autowired 元素
 *
 * 1. 如果 @Autowired 的元素集合不为 null，继续
 * 2. 遍历每个 @Autowired 的元素，拿到每个元素的父 Element。即，.java 类 Element（ 包含内部类，外部类 ）
 * 3. 如果类，属于私有，抛异常
 * 4.1 如果存在 key = 类 Element，拿到 value（ 集合 ），将当前元素添加到集合内
 * 4.2 如果不存在 key，则创建一个集合，作为 value，将当前元素添加到集合内。然后 类 Element 为 key，一起添加进去
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
@SupportedAnnotationTypes({ ANNOTATION_TYPE_AUTOWIRED })
public class AutowiredProcessor extends AbstractProcessor {

    // 用于生产 .java 文件
    private Filer mFiler;       // File util, write class file into disk.
    // 用于在 annotationProcessor 过程中打 log
    private Logger logger;
    // 用于鉴别 类型
    private Types types;
    // 用于转换 Element 的类型为 TypeKind enum 类型（ 自定义枚举类型 ）
    private TypeUtils typeUtils;
    // 用于获取 Element 或 类型
    private Elements elements;
    /*
     * @Autowired 元素 归类 Map
     * 以 类 Element 作为 key，value 为属于该 类 Element 的所有 @Autowired 元素
     */
    private Map<TypeElement, List<Element>> parentAndChild = new HashMap<>();
    // Contain field need autowired and his super class.
    // 定义 ARouter 的 ClassName 对象，以便 JavaPoet 使用
    private static final ClassName ARouterClass = ClassName.get(
        "com.alibaba.android.arouter.launcher", "ARouter");
    // 定义 Log 的 ClassName 对象，以便 JavaPoet 使用
    private static final ClassName AndroidLog = ClassName.get("android.util", "Log");


    /**
     * 初始化方法（ 覆写 ）
     *
     * 初始化所有工具
     *
     * @param processingEnvironment ProcessingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mFiler = processingEnv.getFiler();                  // Generate class.
        types = processingEnv.getTypeUtils();            // Get type utils.
        elements = processingEnv.getElementUtils();      // Get class meta.

        typeUtils = new TypeUtils(types, elements);

        logger = new Logger(processingEnv.getMessager());   // Package the log utils.

        logger.info(">>> AutowiredProcessor init. <<<");
    }


    /**
     * Annotation Processor 的处理方法（ 覆写 ）
     *
     * 1. 先对所有 @Autowired 元素 归类整理
     * -  以 类 Element 作为 key，value 为属于该 类 Element 的所有 @Autowired 元素
     * 2. 生成 ???$$ARouter$$Autowired 类
     *
     * @param set Set<? extends TypeElement>
     * @param roundEnvironment RoundEnvironment
     * @return 返回是否这些注释由该处理器声明
     * 如果 true 返回，则会声明注释，并且不会要求后续处理器处理它们
     * 如果 false 返回，注释是无人认领的，后续处理器可能被要求处理它们。处理器可以总是返回相同的布尔值，或者可以根据所选择的标准来改变结果
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (CollectionUtils.isNotEmpty(set)) {
            try {
                logger.info(">>> Found autowired field, start... <<<");
                categories(roundEnvironment.getElementsAnnotatedWith(Autowired.class));
                generateHelper();

            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }

        return false;
    }


    /**
     * 生成 ???$$ARouter$$Autowired 类
     *
     * @throws IOException IOException
     * @throws IllegalAccessException IllegalAccessException
     */
    private void generateHelper() throws IOException, IllegalAccessException {
        TypeElement type_ISyringe = elements.getTypeElement(ISYRINGE);
        TypeElement type_JsonService = elements.getTypeElement(JSON_SERVICE);
        TypeMirror iProvider = elements.getTypeElement(Consts.IPROVIDER).asType();
        TypeMirror activityTm = elements.getTypeElement(Consts.ACTIVITY).asType();
        TypeMirror fragmentTm = elements.getTypeElement(Consts.FRAGMENT).asType();
        TypeMirror fragmentTmV4 = elements.getTypeElement(Consts.FRAGMENT_V4).asType();

        // Build input param name.
        /**
         * 生成 inject 方法参数
         *
         * @Override
         * Object target
         */
        ParameterSpec objectParamSpec = ParameterSpec.builder(TypeName.OBJECT, "target").build();

        if (MapUtils.isNotEmpty(parentAndChild)) {
            for (Map.Entry<TypeElement, List<Element>> entry : parentAndChild.entrySet()) {
                // Build method : 'inject'
                /**
                 * 生成 inject 方法
                 *
                 * @Override
                 * public void inject(Object target)
                 */
                MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder(METHOD_INJECT)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(objectParamSpec);

                /**
                 * 每个 @Autowired 的 类 Element
                 * 每个 @Autowired 的 Element
                 */
                TypeElement parent = entry.getKey();
                List<Element> childs = entry.getValue();

                /**
                 * 每个 @Autowired 的 类 Element 的完整 name，包括 package name
                 * 每个 @Autowired 的 类 Element 的 package name
                 * 每个 @Autowired 的 类 Element 要生成的 java class name
                 */
                String qualifiedName = parent.getQualifiedName().toString();
                String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
                String fileName = parent.getSimpleName() + NAME_OF_AUTOWIRED;

                logger.info(
                    ">>> Start process " + childs.size() + " field in " + parent.getSimpleName() +
                        " ... <<<");

                /**
                 * 生成 java class
                 *
                 * public class ???$$ARouter$$Autowired implements ISyringe
                 */
                TypeSpec.Builder helper = TypeSpec.classBuilder(fileName)
                    .addJavadoc(WARNING_TIPS)
                    .addSuperinterface(ClassName.get(type_ISyringe))
                    .addModifiers(PUBLIC);

                /**
                 * 生成 SerializationService Field
                 *
                 * private SerializationService serializationService;
                 */
                FieldSpec jsonServiceField = FieldSpec.builder(
                    TypeName.get(type_JsonService.asType()), "serializationService",
                    Modifier.PRIVATE).build();
                helper.addField(jsonServiceField);

                /**
                 * 生成 inject 方法的语句
                 *
                 * serializationService = ARouter.getInstance().navigation(SerializationService.class);;
                 * ???Activity substitute = (???Activity)target;
                 */
                injectMethodBuilder.addStatement(
                    "serializationService = $T.getInstance().navigation($T.class);", ARouterClass,
                    ClassName.get(type_JsonService));
                injectMethodBuilder.addStatement("$T substitute = ($T)target",
                    ClassName.get(parent), ClassName.get(parent));

                /**
                 * 生成 inject 方法的语句
                 *
                 * 第一种情况：
                 * eg: substitute.name = substitute.getIntent().getStringExtra("name");
                 *
                 * 第二种情况：
                 * eg:
                 * if (null != serializationService){
                 *   substitute.obj = serializationService.json2Object(substitute.getIntent().getStringExtra("obj"), TestObj.class);
                 * } else {
                 *   Log.e("ARouter::", "You want automatic inject the field 'obj' in class 'Test1Activity' , then you should implement 'SerializationService' to support object auto inject!");
                 * }
                 *
                 * 第三种情况：
                 * eg: substitute.helloService = ARouter.getInstance().navigation(HelloService.class);
                 *
                 * 第四种情况:
                 * eg: substitute.HelloActivity = (HelloActivity)ARouter.getInstance().build("HelloActivity").navigation();;
                 *
                 * 第五种情况：
                 * eg:
                 * if (null == substitute.obj) {
                 *    Log.e("ARouter::", "The field 'obj' is null, in class '" + BlankFragment.class.getName() + "!");
                 * }
                 *
                 * 第六种情况：
                 * 如果 field 打上了 @Autowired(required = true) ，required 表示 field 不能为 null
                 * eg:
                 * if(substitute.obj == null){
                 *    throw new RuntimeException("The field obj  is null, in class HelloActivity);
                 * }
                 *
                 */
                // Generate method body, start inject.
                for (Element element : childs) {
                    Autowired fieldConfig = element.getAnnotation(Autowired.class);
                    String fieldName = element.getSimpleName().toString();
                    if (types.isSubtype(element.asType(), iProvider)) {  // It's provider
                        if ("".equals(
                            fieldConfig.name())) {    // User has not set service path, then use byType.

                            // Getter
                            injectMethodBuilder.addStatement(
                                "substitute." + fieldName +
                                    " = $T.getInstance().navigation($T.class)",
                                ARouterClass,
                                ClassName.get(element.asType())
                            );
                        } else {    // use byName
                            // Getter
                            injectMethodBuilder.addStatement(
                                "substitute." + fieldName +
                                    " = ($T)$T.getInstance().build($S).navigation();",
                                ClassName.get(element.asType()),
                                ARouterClass,
                                fieldConfig.name()
                            );
                        }

                        // Validater
                        if (fieldConfig.required()) {
                            injectMethodBuilder.beginControlFlow(
                                "if (substitute." + fieldName + " == null)");
                            injectMethodBuilder.addStatement(
                                "throw new RuntimeException(\"The field '" + fieldName +
                                    "' is null, in class '\" + $T.class.getName() + \"!\")",
                                ClassName.get(parent));
                            injectMethodBuilder.endControlFlow();
                        }
                    } else {    // It's normal intent value
                        String statment = "substitute." + fieldName + " = substitute.";
                        boolean isActivity = false;
                        if (types.isSubtype(parent.asType(),
                            activityTm)) {  // Activity, then use getIntent()
                            isActivity = true;
                            statment += "getIntent().";
                        } else if (types.isSubtype(parent.asType(), fragmentTm) ||
                            types.isSubtype(parent.asType(),
                                fragmentTmV4)) {   // Fragment, then use getArguments()
                            statment += "getArguments().";
                        } else {
                            throw new IllegalAccessException("The field [" + fieldName +
                                "] need autowired from intent, its parent must be activity or fragment!");
                        }

                        statment = buildStatement(statment, typeUtils.typeExchange(element),
                            isActivity);
                        if (statment.startsWith("serializationService.")) {   // Not mortals
                            injectMethodBuilder.beginControlFlow(
                                "if (null != serializationService)");
                            injectMethodBuilder.addStatement(
                                "substitute." + fieldName + " = " + statment,
                                (StringUtils.isEmpty(fieldConfig.name())
                                 ? fieldName
                                 : fieldConfig.name()),
                                ClassName.get(element.asType())
                            );
                            injectMethodBuilder.nextControlFlow("else");
                            injectMethodBuilder.addStatement(
                                "$T.e(\"" + Consts.TAG +
                                    "\", \"You want automatic inject the field '" + fieldName +
                                    "' in class '$T' , then you should implement 'SerializationService' to support object auto inject!\")",
                                AndroidLog, ClassName.get(parent));
                            injectMethodBuilder.endControlFlow();
                        } else {
                            injectMethodBuilder.addStatement(statment,
                                StringUtils.isEmpty(fieldConfig.name())
                                ? fieldName
                                : fieldConfig.name());
                        }

                        // Validator
                        if (fieldConfig.required() && !element.asType()
                            .getKind()
                            .isPrimitive()) {  // Primitive wont be check.
                            injectMethodBuilder.beginControlFlow(
                                "if (null == substitute." + fieldName + ")");
                            injectMethodBuilder.addStatement(
                                "$T.e(\"" + Consts.TAG + "\", \"The field '" + fieldName +
                                    "' is null, in class '\" + $T.class.getName() + \"!\")",
                                AndroidLog, ClassName.get(parent));
                            injectMethodBuilder.endControlFlow();
                        }
                    }
                }

                helper.addMethod(injectMethodBuilder.build());

                /**
                 * 生成 Java file
                 */
                // Generate autowire helper
                JavaFile.builder(packageName, helper.build()).build().writeTo(mFiler);

                logger.info(">>> " + parent.getSimpleName() + " has been processed, " + fileName +
                    " has been generated. <<<");
            }

            logger.info(">>> Autowired processor stop. <<<");
        }
    }


    /**
     * 根据不同的 TypeKind 类型，生成不同的 intent getter 语句
     *
     * @param statment statment
     * @param type type
     * @param isActivity isActivity
     * @return intent getter 语句
     */
    private String buildStatement(String statment, int type, boolean isActivity) {
        if (type == TypeKind.BOOLEAN.ordinal()) {
            statment += (isActivity ? ("getBooleanExtra($S, false)") : ("getBoolean($S)"));
        } else if (type == TypeKind.BYTE.ordinal()) {
            statment += (isActivity ? ("getByteExtra($S, (byte) 0)") : ("getByte($S)"));
        } else if (type == TypeKind.SHORT.ordinal()) {
            statment += (isActivity ? ("getShortExtra($S, (short) 0)") : ("getShort($S)"));
        } else if (type == TypeKind.INT.ordinal()) {
            statment += (isActivity ? ("getIntExtra($S, 0)") : ("getInt($S)"));
        } else if (type == TypeKind.LONG.ordinal()) {
            statment += (isActivity ? ("getLongExtra($S, 0)") : ("getLong($S)"));
        } else if (type == TypeKind.FLOAT.ordinal()) {
            statment += (isActivity ? ("getFloatExtra($S, 0)") : ("getFloat($S)"));
        } else if (type == TypeKind.DOUBLE.ordinal()) {
            statment += (isActivity ? ("getDoubleExtra($S, 0)") : ("getDouble($S)"));
        } else if (type == TypeKind.STRING.ordinal()) {
            statment += (isActivity ? ("getStringExtra($S)") : ("getString($S)"));
        } else if (type == TypeKind.PARCELABLE.ordinal()) {
            statment += (isActivity ? ("getParcelableExtra($S)") : ("getParcelable($S)"));
        } else if (type == TypeKind.OBJECT.ordinal()) {
            statment = "serializationService.json2Object(substitute." +
                (isActivity ? "getIntent()." : "getArguments().") +
                (isActivity ? "getStringExtra($S)" : "getString($S)") + ", $T.class)";
        }

        return statment;
    }


    /**
     * Categories field, find his papa.
     *
     * 将 所有 @Autowired 元素 归类
     * 以 类 Element 作为 key，value 为属于该 类 Element 的所有 @Autowired 元素
     *
     * 1. 如果 @Autowired 的元素集合不为 null，继续
     * 2. 遍历每个 @Autowired 的元素，拿到每个元素的父 Element。即，.java 类 Element（ 包含内部类，外部类 ）
     * 3. 如果类，属于私有，抛异常
     * 4.1 如果存在 key = 类 Element，拿到 value（ 集合 ），将当前元素添加到集合内
     * 4.2 如果不存在 key，则创建一个集合，作为 value，将当前元素添加到集合内。然后 类 Element 为 key，一起添加进去
     *
     * @param elements Field need autowired
     */
    private void categories(Set<? extends Element> elements) throws IllegalAccessException {
        /*
         * 1. 如果 @Autowired 的元素集合不为 null，继续
         * 2. 遍历每个 @Autowired 的元素，拿到每个元素的父 Element。即，.java 类 Element（ 包含内部类，外部类 ）
         * 3. 如果类，属于私有，抛异常
         * 4.1 如果存在 key = 类 Element，拿到 value（ 集合 ），将当前元素添加到集合内
         * 4.2 如果不存在 key，则创建一个集合，作为 value，将当前元素添加到集合内。然后 类 Element 为 key，一起添加进去
         */
        if (CollectionUtils.isNotEmpty(elements)) {
            for (Element element : elements) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                if (element.getModifiers().contains(Modifier.PRIVATE)) {
                    throw new IllegalAccessException(
                        "The autowired fields CAN NOT BE 'private'!!! please check field ["
                            + element.getSimpleName() + "] in class [" +
                            enclosingElement.getQualifiedName() + "]");
                }

                if (parentAndChild.containsKey(enclosingElement)) { // Has categries
                    parentAndChild.get(enclosingElement).add(element);
                } else {
                    List<Element> childs = new ArrayList<>();
                    childs.add(element);
                    parentAndChild.put(enclosingElement, childs);
                }
            }

            logger.info("categories finished.");
        }
    }

}
