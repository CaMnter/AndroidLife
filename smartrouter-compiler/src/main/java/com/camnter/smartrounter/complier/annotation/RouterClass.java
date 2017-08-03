package com.camnter.smartrounter.complier.annotation;

import com.camnter.smartrounter.complier.RouterType;
import com.camnter.smartrounter.complier.core.BaseAnnotatedClass;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import static com.camnter.smartrounter.complier.RouterType.ANDROID_INTENT;
import static com.camnter.smartrounter.complier.RouterType.ANDROID_TEXT_UTILS;
import static com.camnter.smartrounter.complier.RouterType.ANDROID_URI;
import static com.camnter.smartrounter.complier.RouterType.BOOLEAN;
import static com.camnter.smartrounter.complier.RouterType.BOXED_BOOLEAN;
import static com.camnter.smartrounter.complier.RouterType.BOXED_BYTE;
import static com.camnter.smartrounter.complier.RouterType.BOXED_CHAR;
import static com.camnter.smartrounter.complier.RouterType.BOXED_DOUBLE;
import static com.camnter.smartrounter.complier.RouterType.BOXED_FLOAT;
import static com.camnter.smartrounter.complier.RouterType.BOXED_INT;
import static com.camnter.smartrounter.complier.RouterType.BOXED_LONG;
import static com.camnter.smartrounter.complier.RouterType.BOXED_SHORT;
import static com.camnter.smartrounter.complier.RouterType.BYTE;
import static com.camnter.smartrounter.complier.RouterType.CHAR;
import static com.camnter.smartrounter.complier.RouterType.DOUBLE;
import static com.camnter.smartrounter.complier.RouterType.FLOAT;
import static com.camnter.smartrounter.complier.RouterType.INT;
import static com.camnter.smartrounter.complier.RouterType.JAVA_BOXED_BOOLEAN;
import static com.camnter.smartrounter.complier.RouterType.JAVA_BOXED_BYTE;
import static com.camnter.smartrounter.complier.RouterType.JAVA_BOXED_DOUBLE;
import static com.camnter.smartrounter.complier.RouterType.JAVA_BOXED_FLOAT;
import static com.camnter.smartrounter.complier.RouterType.JAVA_BOXED_INT;
import static com.camnter.smartrounter.complier.RouterType.JAVA_BOXED_LONG;
import static com.camnter.smartrounter.complier.RouterType.JAVA_BOXED_SHORT;
import static com.camnter.smartrounter.complier.RouterType.LONG;
import static com.camnter.smartrounter.complier.RouterType.SHORT;
import static com.camnter.smartrounter.complier.RouterType.SMART_ROUTERS;
import static com.camnter.smartrounter.complier.RouterType.STRING;

/**
 * @author CaMnter
 */

public class RouterClass extends BaseAnnotatedClass {

    private final List<RouterHostAnnotation> routerHostAnnotationList;
    private final List<RouterFieldAnnotation> routerFieldAnnotationList;


    public RouterClass(Element annotatedElement,
                       Elements elements,
                       String fullClassName) {
        super(annotatedElement, elements, fullClassName);
        this.routerHostAnnotationList = new ArrayList<>();
        this.routerFieldAnnotationList = new ArrayList<>();
    }


    public void addRouterHostAnnotation(RouterHostAnnotation routerHostAnnotation) {
        this.routerHostAnnotationList.add(routerHostAnnotation);
    }


    public void addRouterFieldAnnotation(RouterFieldAnnotation routerFieldAnnotation) {
        this.routerFieldAnnotationList.add(routerFieldAnnotation);
    }


    /**
     * get the JavaFile
     *
     * @return JavaFile
     */
    @Override
    public JavaFile javaFile() {

        final String className = this.annotatedElementSimpleName + "_SmartRouter";

        /*
         * _SmartRouter
         * public class ???ActivitySmartRouter extends BaseActivityRouter implements Router<???Activity>
         */
        TypeSpec smartRouterClass = TypeSpec
            .classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .superclass(RouterType.BASE_ACTIVITY_ROUTER)
            .addSuperinterface(
                ParameterizedTypeName.get(
                    RouterType.ROUTER,
                    TypeVariableName.get(this.annotatedElementSimpleName)
                )
            )
            .addJavadoc("Generated code from SmartRouter. Do not modify !\n\n")
            .addJavadoc("@author CaMnter\n")
            /*
             * private static final SmartRouter REGISTER_INSTANCE = new SmartRouter("");
             * static {
             * -   SmartRouters.register(REGISTER_INSTANCE);
             * }
             */
            .addField(this.staticFieldBuilder(className).build())
            .addStaticBlock(this.staticBlockBuilder().build())
            /*
             * SmartRouter(@NonNull final String host)
             * SmartRouter # register(@NonNull final Map<String, Class<? extends Activity>> routerMapping)
             * SmartRouter # setFieldValue(@NonNull final Activity activity)
             * SmartRouter # putValue(final int value)
             */
            .addMethod(this.constructorBuilder().build())
            .addMethod(this.registerMethodBuilder().build())
            .addMethod(this.setFieldValueMethodBuilder().build())
            .addMethods(this.putFieldMethodBuilder())
            .build();

        return JavaFile.builder(this.annotatedElementPackageName, smartRouterClass).build();
    }


    /**
     * private static final SmartRouter REGISTER_INSTANCE = new SmartRouter("");
     *
     * @return FieldSpec.Builder
     */
    private FieldSpec.Builder staticFieldBuilder(String className) {
        final ClassName fieldClassName = ClassName.get(this.getPackageName(), className);
        return
            FieldSpec.builder(fieldClassName, "REGISTER_INSTANCE")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T(\"\")", fieldClassName);
    }


    /**
     * static {
     * -   SmartRouters.register(REGISTER_INSTANCE);
     * }
     *
     * @return CodeBlock.Builder
     */
    private CodeBlock.Builder staticBlockBuilder() {
        return CodeBlock.builder().add("$T.register(REGISTER_INSTANCE);\n", SMART_ROUTERS);
    }


    /**
     * SmartRouter(@NonNull final String host)
     *
     * @return MethodSpec.Builder
     */
    private MethodSpec.Builder constructorBuilder() {
        return
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(
                    createNonNullParameter(
                        TypeName.get(String.class),
                        "host",
                        Modifier.FINAL
                    )
                )
                .addCode("super(host);\n");
    }


    /**
     * SmartRouter # register(@NonNull final Map<String, Class<? extends Activity>>
     * routerMapping)
     *
     * @return MethodSpec.Builder
     */
    private MethodSpec.Builder registerMethodBuilder() {

        // public void register(@NonNull final Map<String, Class<? extends Activity>> routerMapping)
        final MethodSpec.Builder registerMethodBuilder = MethodSpec.methodBuilder("register")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.VOID)
            .addParameter(
                createNonNullParameter(
                    ParameterizedTypeName.get(
                        ClassName.get(Map.class),
                        ClassName.get(String.class),
                        ParameterizedTypeName.get(
                            ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(RouterType.ANDROID_ACTIVITY)
                        )
                    ),
                    "routerMapping",
                    Modifier.FINAL
                )
            );

        // routerMapping.put($1L, $2L.class)
        for (RouterHostAnnotation routerHostAnnotation : this.routerHostAnnotationList) {
            final String activitySimpleName = routerHostAnnotation.getElement()
                .getSimpleName()
                .toString();
            for (String host : routerHostAnnotation.getHost()) {
                registerMethodBuilder.addCode(
                    "routerMapping.put($T.getScheme() + \"://\" + $S, $L.class);\n",
                    SMART_ROUTERS,
                    host,
                    activitySimpleName
                );
            }
        }

        return registerMethodBuilder;
    }


    /**
     * SmartRouter # setFieldValue(@NonNull final Activity activity)
     *
     * @return MethodSpec.Builder
     */
    private MethodSpec.Builder setFieldValueMethodBuilder() {

        // public void setFieldValue(@NonNull Activity activity)
        final MethodSpec.Builder setFieldValueMethodBuilder = MethodSpec.methodBuilder(
            "setFieldValue")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.VOID)
            .addParameter(
                createNonNullParameter(
                    this.annotatedElementTypeName,
                    "activity",
                    Modifier.FINAL
                )
            );

        // final Intent intent = activity.getIntent()
        if (!this.routerFieldAnnotationList.isEmpty()) {
            setFieldValueMethodBuilder.addCode(
                CodeBlock.of("final $T intent = activity.getIntent();\n", ANDROID_INTENT)
            );
            setFieldValueMethodBuilder.addCode(
                CodeBlock.of("final $T uri = intent.getData();\n", ANDROID_URI)
            );
            setFieldValueMethodBuilder.addCode(
                CodeBlock.of("if (uri == null) return;\n\n")
            );
        }

        final List<CodeBlock> codeBlocks = new ArrayList<>();
        for (RouterFieldAnnotation routerFieldAnnotation : this.routerFieldAnnotationList) {
            final String fieldTypeString = routerFieldAnnotation.getFieldType().toString();
            final String fieldName = routerFieldAnnotation.getFieldName().toString();
            final String fieldValue = routerFieldAnnotation.getFieldValue();
            codeBlocks.clear();
            switch (fieldTypeString) {
                case CHAR:
                case BOXED_CHAR:
                    final String uriParameterName = "uri" +
                        fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    codeBlocks.add(CodeBlock.of(
                        "final String $L = uri.getQueryParameter($S);\n",
                        uriParameterName,
                        fieldValue
                    ));
                    codeBlocks.add(CodeBlock.of(
                        "if (!$T.isEmpty($L)) {\n",
                        ANDROID_TEXT_UTILS,
                        uriParameterName
                    ));
                    codeBlocks.add(CodeBlock.of(
                        "    activity.$1L = $2L.charAt(0);\n",
                        fieldName,
                        uriParameterName
                    ));
                    codeBlocks.add(CodeBlock.of("}\n"));
                    break;
                case BYTE:
                case BOXED_BYTE:
                    codeBlocks.addAll(
                        this.getFieldValueCodeBlock(JAVA_BOXED_BYTE, fieldName, fieldValue)
                    );
                    break;
                case SHORT:
                case BOXED_SHORT:
                    codeBlocks.addAll(
                        this.getFieldValueCodeBlock(JAVA_BOXED_SHORT, fieldName, fieldValue)
                    );
                    break;
                case INT:
                case BOXED_INT:
                    codeBlocks.addAll(
                        this.getFieldValueCodeBlock(JAVA_BOXED_INT, fieldName, fieldValue)
                    );
                    break;
                case FLOAT:
                case BOXED_FLOAT:
                    codeBlocks.addAll(
                        this.getFieldValueCodeBlock(JAVA_BOXED_FLOAT, fieldName, fieldValue)
                    );
                    break;
                case DOUBLE:
                case BOXED_DOUBLE:
                    codeBlocks.addAll(
                        this.getFieldValueCodeBlock(JAVA_BOXED_DOUBLE, fieldName, fieldValue)
                    );
                    break;
                case LONG:
                case BOXED_LONG:
                    codeBlocks.addAll(
                        this.getFieldValueCodeBlock(JAVA_BOXED_LONG, fieldName, fieldValue)
                    );
                    break;
                case BOOLEAN:
                case BOXED_BOOLEAN:
                    codeBlocks.addAll(
                        this.getFieldValueCodeBlock(JAVA_BOXED_BOOLEAN, fieldName, fieldValue)
                    );
                    break;
                case STRING:
                    codeBlocks.add(CodeBlock.of(
                        "activity.$L = uri.getQueryParameter($S);\n",
                        fieldName,
                        fieldValue
                    ));
                    break;
            }
            if (codeBlocks.isEmpty()) {
                continue;
            }
            for (CodeBlock block : codeBlocks) {
                setFieldValueMethodBuilder.addCode(block);
            }
        }

        return setFieldValueMethodBuilder;
    }


    /**
     * Byte，Short, Integer, Float, Double, Long, Boolean
     *
     * try {
     * -   activity.fieldName = $T.valueOf(uri.getQueryParameter("fieldValue"));
     * } catch (Exception e) {
     * -   // ignore
     * }
     *
     * @param className className
     * @param fieldName fieldName
     * @param fieldValue fieldValue
     * @return List<CodeBlock>
     */
    private List<CodeBlock> getFieldValueCodeBlock(ClassName className,
                                                   String fieldName,
                                                   String fieldValue) {
        final List<CodeBlock> codeBlocks = new ArrayList<>();
        codeBlocks.add(CodeBlock.of("try {\n"));
        codeBlocks.add(CodeBlock.of(
            "    activity.$L = $T.valueOf(uri.getQueryParameter($S));\n",
            fieldName,
            className,
            fieldValue
        ));
        codeBlocks.add(CodeBlock.of("} catch (Exception e) {\n"));
        codeBlocks.add(CodeBlock.of("    // ignore\n"));
        codeBlocks.add(CodeBlock.of("}\n"));
        return codeBlocks;
    }


    /**
     * SmartRouter # putValue(final int value)
     * SmartRouter # putValue(@NonNull final Integer value)
     *
     * @return List<MethodSpec>
     */
    private List<MethodSpec> putFieldMethodBuilder() {

        final List<MethodSpec> putMethods = new ArrayList<>();

        for (RouterFieldAnnotation routerFieldAnnotation : this.routerFieldAnnotationList) {
            final TypeMirror fieldTypeMirror = routerFieldAnnotation.getFieldType();
            final TypeName fieldTypeName = TypeName.get(fieldTypeMirror);
            final String fieldTypeString = fieldTypeMirror.toString();
            final String fieldName = routerFieldAnnotation.getFieldName().toString();

            final String expectName = "put" + fieldName.substring(0, 1).toUpperCase() +
                fieldName.substring(1);

            final MethodSpec.Builder putMethodBuilder = MethodSpec
                .methodBuilder(expectName)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addCode(CodeBlock.of("this.put($S, value);\n", fieldName));

            switch (fieldTypeString) {
                case CHAR:
                case BYTE:
                case SHORT:
                case INT:
                case FLOAT:
                case DOUBLE:
                case LONG:
                case BOOLEAN:
                    putMethodBuilder.addParameter(fieldTypeName, "value", Modifier.FINAL);
                    putMethods.add(putMethodBuilder.build());
                    break;
                case BOXED_CHAR:
                case BOXED_BYTE:
                case BOXED_SHORT:
                case BOXED_INT:
                case BOXED_FLOAT:
                case BOXED_DOUBLE:
                case BOXED_LONG:
                case BOXED_BOOLEAN:
                case STRING:
                    putMethodBuilder.addParameter(
                        createNonNullParameter(
                            fieldTypeName,
                            "value",
                            Modifier.FINAL
                        )
                    );
                    putMethods.add(putMethodBuilder.build());
                    break;
            }
        }
        return putMethods;
    }

}
