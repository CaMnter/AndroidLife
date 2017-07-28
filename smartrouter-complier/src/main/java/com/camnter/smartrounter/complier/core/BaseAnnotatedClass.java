package com.camnter.smartrounter.complier.core;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * @author CaMnter
 */

public abstract class BaseAnnotatedClass {

    private static final ClassName ANDROID_SUPPORT_ANNOTATION_NONNULL = ClassName.get(
        "android.support.annotation",
        "NonNull"
    );

    private final Elements elements;
    private final TypeElement annotatedElement;

    protected final TypeMirror annotatedElementType;
    protected final TypeName annotatedElementTypeName;
    protected final String annotatedElementSimpleName;


    public BaseAnnotatedClass(TypeElement annotatedElement,
                              Elements elements) {
        this.elements = elements;
        this.annotatedElement = annotatedElement;
        this.annotatedElementType = this.annotatedElement.asType();
        this.annotatedElementTypeName = TypeName.get(this.annotatedElementType);
        this.annotatedElementSimpleName = this.annotatedElement.getSimpleName().toString();
    }


    /**
     * get the JavaFile
     *
     * @return JavaFile
     */
    public abstract JavaFile javaFile();


    /**
     * android.support.annotation.NonNull parameter
     *
     * @param type TypeName
     * @param name String
     * @param modifiers Modifier...
     */
    protected ParameterSpec createNonNullParameter(TypeName type, String name, Modifier... modifiers) {
        return ParameterSpec.builder(type, name, modifiers)
            .addAnnotation(ANDROID_SUPPORT_ANNOTATION_NONNULL)
            .build();
    }


    public String getFullClassName() {
        return this.annotatedElement
            .getQualifiedName()
            .toString();
    }


    protected String getPackageName() {
        return this.elements
            .getPackageOf(this.annotatedElement)
            .getQualifiedName()
            .toString();
    }

}
