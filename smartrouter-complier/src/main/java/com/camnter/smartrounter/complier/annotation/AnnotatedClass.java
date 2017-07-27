package com.camnter.smartrounter.complier.annotation;

import com.camnter.smartrounter.complier.RouterType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
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

public class AnnotatedClass {

    private final Elements elements;
    private final TypeElement annotatedElement;
    private final List<RouterHostAnnotation> routerHostAnnotationList;
    private final List<RouterFieldAnnotation> routerFieldAnnotationList;

    private final TypeMirror annotatedElementType;
    private final String annotatedElementSimpleName;


    public AnnotatedClass(TypeElement annotatedElement,
                          Elements elements) {
        this.elements = elements;
        this.annotatedElement = annotatedElement;
        this.annotatedElementType = this.annotatedElement.asType();
        this.annotatedElementSimpleName = this.annotatedElement.getSimpleName().toString();
        this.routerHostAnnotationList = new ArrayList<>();
        this.routerFieldAnnotationList = new ArrayList<>();
    }


    public void addRouterHostAnnotation(RouterHostAnnotation routerHostAnnotation) {
        this.routerHostAnnotationList.add(routerHostAnnotation);
    }


    public void addRouterFieldAnnotation(RouterFieldAnnotation routerFieldAnnotation) {
        this.routerFieldAnnotationList.add(routerFieldAnnotation);
    }


    public JavaFile getJavaFile() {
        // TODO
        return null;
    }


    /**
     * SmartRouter(@NonNull final String host)
     *
     * @return MethodSpec.Builder
     */
    private MethodSpec.Builder smartRouterConstructorBuilder() {
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
                    TypeName.get(this.annotatedElementType),
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
                    codeBlock = CodeBlock.of("intent.getCharExtra(\"$L\", (char) 0)", fieldName);
                    break;
                case BYTE:
                case BOXED_BYTE:
                    codeBlock = CodeBlock.of("intent.getCharExtra(\"$L\", (byte) 0)", fieldName);
                    break;
                case SHORT:
                case BOXED_SHORT:
                    codeBlock = CodeBlock.of("intent.getCharExtra(\"$L\", (short) 0)", fieldName);
                    break;
                case INT:
                case BOXED_INT:
                    codeBlock = CodeBlock.of("intent.getCharExtra(\"$L\"L, 0)", fieldName);
                    break;
                case FLOAT:
                case BOXED_FLOAT:
                    codeBlock = CodeBlock.of("intent.getCharExtra(\"$L\", 0f)", fieldName);
                    break;
                case DOUBLE:
                case BOXED_DOUBLE:
                    codeBlock = CodeBlock.of("intent.getCharExtra(\"$L\", 0d)", fieldName);
                    break;
                case LONG:
                case BOXED_LONG:
                    codeBlock = CodeBlock.of("intent.getCharExtra(\"$L\", 0L)", fieldName);
                    break;
                case BOOLEAN:
                case BOXED_BOOLEAN:
                    codeBlock = CodeBlock.of("intent.getCharExtra(\"$L\", false)", fieldName);
                    break;
                case STRING:
                    codeBlock = CodeBlock.of("intent.getCharExtra(\"$L\")", fieldName);
                    break;
            }
            if (codeBlock == null) {
                continue;
            }
            setFieldValueMethodBuilder.addCode(codeBlock);
        }

        return setFieldValueMethodBuilder;
    }


    /**
     * android.support.annotation.NonNull parameter
     *
     * @param type TypeName
     * @param name String
     * @param modifiers Modifier...
     */
    private ParameterSpec createNonNullParameter(TypeName type, String name, Modifier... modifiers) {
        return ParameterSpec.builder(type, name, modifiers)
            .addAnnotation(RouterType.ANDROID_SUPPORT_ANNOTATION_NONNULL)
            .build();
    }


    public String getFullClassName() {
        return this.annotatedElement.getQualifiedName().toString();
    }

}
