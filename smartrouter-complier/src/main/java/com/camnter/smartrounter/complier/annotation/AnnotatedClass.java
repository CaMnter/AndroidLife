package com.camnter.smartrounter.complier.annotation;

import com.camnter.smartrounter.complier.RouterType;
import com.squareup.javapoet.ClassName;
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
     * ???SmartRouter # register(@NonNull final Map<String, Class<? extends Activity>> routerMapping)
     *
     * @return MethodSpec.Builder
     */
    private MethodSpec.Builder registerMethodBuilder() {

        // public void register(@NonNull final Map<String, Class<? extends Activity>> routerMapping)
        MethodSpec.Builder registerMethodBuilder = MethodSpec.methodBuilder("register")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.VOID)
            .addParameter(
                this.createNonNullParameter(
                    ParameterizedTypeName.get(ClassName.get(Map.class),
                        ClassName.get(String.class),
                        WildcardTypeName.subtypeOf(RouterType.ANDROID_ACTIVITY)),
                    "routerMapping",
                    Modifier.FINAL));

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


    private MethodSpec.Builder setFieldValueMethodBuilder() {
        // TODO
        return null;
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
