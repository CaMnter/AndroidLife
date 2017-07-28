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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

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
import static com.camnter.smartrounter.complier.RouterType.LONG;
import static com.camnter.smartrounter.complier.RouterType.SHORT;
import static com.camnter.smartrounter.complier.RouterType.STRING;

/**
 * @author CaMnter
 */

public class AnnotatedClass extends BaseAnnotatedClass {

    private final List<RouterHostAnnotation> routerHostAnnotationList;
    private final List<RouterFieldAnnotation> routerFieldAnnotationList;


    public AnnotatedClass(TypeElement annotatedElement,
                          Elements elements) {
        super(annotatedElement, elements);
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
        /*
         * _SmartRouter
         * public class ???ActivitySmartRouter extends BaseActivityRouter implements Router<???Activity>
         */
        TypeSpec smartRouterClass = TypeSpec
            .classBuilder(this.annotatedElementSimpleName + "_SmartRouter")
            .addModifiers(Modifier.PUBLIC)
            .superclass(RouterType.BASE_ACTIVITY_ROUTER)
            .addSuperinterface(
                ParameterizedTypeName.get(
                    RouterType.ROUTER,
                    TypeVariableName.get(this.annotatedElementSimpleName)
                )
            )
            /*
             * private static final SmartRouter REGISTER_INSTANCE = new SmartRouter("");
             * static {
             * -   SmartRouters.register(REGISTER_INSTANCE);
             * }
             */
            .addField(this.staticFieldBuilder().build())
            .addStaticBlock(this.staticBlockBuilder().build())
            /*
             * SmartRouter(@NonNull final String host)
             * SmartRouter # register(@NonNull final Map<String, Class<? extends Activity>> routerMapping)
             * SmartRouter # setFieldValue(@NonNull final Activity activity)
             */
            .addMethod(this.constructorBuilder().build())
            .addMethod(this.registerMethodBuilder().build())
            .addMethod(this.setFieldValueMethodBuilder().build())
            .build();

        final String packageName = this.getPackageName();
        return JavaFile.builder(packageName, smartRouterClass).build();
    }


    /**
     * private static final SmartRouter REGISTER_INSTANCE = new SmartRouter("");
     *
     * @return FieldSpec.Builder
     */
    private FieldSpec.Builder staticFieldBuilder() {
        return
            FieldSpec.builder(this.annotatedElementTypeName, "REGISTER_INSTANCE")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T(\"\")", this.annotatedElementTypeName);
    }


    /**
     * static {
     * -   SmartRouters.register(REGISTER_INSTANCE);
     * }
     *
     * @return CodeBlock.Builder
     */
    private CodeBlock.Builder staticBlockBuilder() {
        return CodeBlock.builder().add("SmartRouters.register(REGISTER_INSTANCE)");
    }


    /**
     * SmartRouter(@NonNull final String host)
     *
     * @return MethodSpec.Builder
     */
    private MethodSpec.Builder constructorBuilder() {
        return
            MethodSpec.constructorBuilder()
                .addParameter(
                    this.createNonNullParameter(
                        TypeName.get(String.class),
                        "host",
                        Modifier.FINAL
                    )
                )
                .addCode("super(host)");
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
                this.createNonNullParameter(
                    ParameterizedTypeName.get(ClassName.get(Map.class),
                        ClassName.get(String.class),
                        WildcardTypeName.subtypeOf(RouterType.ANDROID_ACTIVITY)),
                    "routerMapping",
                    Modifier.FINAL
                )
            );

        // routerMapping.put($1L, $2L.class)
        for (RouterHostAnnotation routerHostAnnotation : this.routerHostAnnotationList) {
            final String activitySimpleName = routerHostAnnotation.getVariableElement()
                .getSimpleName()
                .toString();
            for (String host : routerHostAnnotation.getHost()) {
                registerMethodBuilder.addCode("routerMapping.put($1L, $2L.class)", host,
                    activitySimpleName);
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
                this.createNonNullParameter(
                    this.annotatedElementTypeName,
                    "activity",
                    Modifier.FINAL
                )
            );

        for (RouterFieldAnnotation routerFieldAnnotation : this.routerFieldAnnotationList) {
            final String fieldTypeString = routerFieldAnnotation.getFieldType().toString();
            final String fieldName = routerFieldAnnotation.getFieldName().toString();
            CodeBlock codeBlock = null;
            switch (fieldTypeString) {
                case CHAR:
                case BOXED_CHAR:
                    codeBlock = CodeBlock.of("intent.getCharExtra($S, (char) 0)", fieldName);
                    break;
                case BYTE:
                case BOXED_BYTE:
                    codeBlock = CodeBlock.of("intent.getCharExtra($S, (byte) 0)", fieldName);
                    break;
                case SHORT:
                case BOXED_SHORT:
                    codeBlock = CodeBlock.of("intent.getCharExtra($S, (short) 0)", fieldName);
                    break;
                case INT:
                case BOXED_INT:
                    codeBlock = CodeBlock.of("intent.getCharExtra($S, 0)", fieldName);
                    break;
                case FLOAT:
                case BOXED_FLOAT:
                    codeBlock = CodeBlock.of("intent.getCharExtra($S, 0f)", fieldName);
                    break;
                case DOUBLE:
                case BOXED_DOUBLE:
                    codeBlock = CodeBlock.of("intent.getCharExtra($S, 0d)", fieldName);
                    break;
                case LONG:
                case BOXED_LONG:
                    codeBlock = CodeBlock.of("intent.getCharExtra($S, 0L)", fieldName);
                    break;
                case BOOLEAN:
                case BOXED_BOOLEAN:
                    codeBlock = CodeBlock.of("intent.getCharExtra($S, false)", fieldName);
                    break;
                case STRING:
                    codeBlock = CodeBlock.of("intent.getCharExtra($S)", fieldName);
                    break;
            }
            if (codeBlock == null) {
                continue;
            }
            setFieldValueMethodBuilder.addCode(codeBlock);
        }

        return setFieldValueMethodBuilder;
    }

}
