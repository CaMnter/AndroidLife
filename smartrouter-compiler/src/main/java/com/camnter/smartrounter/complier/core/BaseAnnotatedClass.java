package com.camnter.smartrounter.complier.core;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * @author CaMnter
 */

public abstract class BaseAnnotatedClass implements BaseAnnotatedInterface {

    private static final ClassName ANDROID_SUPPORT_ANNOTATION_NONNULL = ClassName.get(
        "android.support.annotation",
        "NonNull"
    );

    private final Elements elements;
    private final Element annotatedElement;
    private final String fullClassName;
    private final String simpleName;

    protected final TypeMirror annotatedElementType;
    protected final TypeName annotatedElementTypeName;
    protected final String annotatedElementSimpleName;
    protected final String annotatedElementPackageName;


    public BaseAnnotatedClass(Element annotatedElement,
                              Elements elements,
                              String fullClassName) {
        this.elements = elements;
        this.annotatedElement = annotatedElement;
        this.fullClassName = fullClassName;
        this.simpleName = this.annotatedElement.getSimpleName().toString();
        this.annotatedElementType = this.annotatedElement.asType();
        this.annotatedElementTypeName = TypeName.get(this.annotatedElementType);
        this.annotatedElementSimpleName = this.annotatedElement.getSimpleName().toString();
        this.annotatedElementPackageName = this.getPackageName();
    }


    /**
     * android.support.annotation.NonNull parameter
     *
     * @param type TypeName
     * @param name String
     * @param modifiers Modifier...
     */
    public static ParameterSpec createNonNullParameter(TypeName type, String name, Modifier... modifiers) {
        return ParameterSpec.builder(type, name, modifiers)
            .addAnnotation(ANDROID_SUPPORT_ANNOTATION_NONNULL)
            .build();
    }


    public String getSimpleName() {
        return this.simpleName;
    }


    public String getFullClassName() {
        return this.fullClassName;
    }


    public String getPackageName() {
        return
            this.elements
                .getPackageOf(this.annotatedElement)
                .getQualifiedName()
                .toString();
    }

}
