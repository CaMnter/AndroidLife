package com.camnter.smartrounter.complier.annotation;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import java.util.ArrayList;
import java.util.List;
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

    private final TypeMirror annotatedElementType;
    private final String annotatedElementSimpleName;


    public AnnotatedClass(TypeElement annotatedElement,
                          Elements elements) {
        this.elements = elements;
        this.annotatedElement = annotatedElement;
        this.annotatedElementType = this.annotatedElement.asType();
        this.annotatedElementSimpleName = this.annotatedElement.getSimpleName().toString();
        this.routerHostAnnotationList = new ArrayList<>();
    }


    public void addRouterHostAnnotation(RouterHostAnnotation routerHostAnnotation) {
        this.routerHostAnnotationList.add(routerHostAnnotation);
    }


    public JavaFile getJavaFile() {
        // TODO
        return null;
    }


    private MethodSpec.Builder setFieldValueMethodBuilder() {
        // TODO
        return null;
    }


    public String getFullClassName() {
        return this.annotatedElement.getQualifiedName().toString();
    }

}
