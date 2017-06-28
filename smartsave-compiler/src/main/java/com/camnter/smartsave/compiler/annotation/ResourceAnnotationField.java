package com.camnter.smartsave.compiler.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author CaMnter
 */

class ResourceAnnotationField<A extends Annotation> {

    private int resId;
    private VariableElement variableElement;


    ResourceAnnotationField(Element element,
                            Class<A> clazz) {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException(
                String.format(
                    "Only fields can be annotated with [@%1$s]",
                    clazz.getSimpleName()
                )
            );
        }
        this.variableElement = (VariableElement) element;
        A annotation = this.variableElement.getAnnotation(clazz);
        try {
            Method value = annotation.annotationType().getDeclaredMethod("value");
            value.setAccessible(true);
            this.resId = (int) value.invoke(annotation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.resId <= 0) {
            throw new IllegalArgumentException(
                String.format(
                    "value() in [%1$s] for field [%2$s] is not valid",
                    clazz.getSimpleName(),
                    element.getSimpleName())
            );
        }
    }


    TypeMirror getFieldType() {
        return this.variableElement.asType();
    }


    int getResId() {
        return this.resId;
    }


    Name getFieldName() {
        return this.variableElement.getSimpleName();
    }


    public VariableElement getVariableElement() {
        return this.variableElement;
    }

}
