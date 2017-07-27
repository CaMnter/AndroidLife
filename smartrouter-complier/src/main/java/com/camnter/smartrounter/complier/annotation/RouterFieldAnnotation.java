package com.camnter.smartrounter.complier.annotation;

import com.camnter.smartrouter.annotation.RouterField;
import com.camnter.smartrouter.annotation.RouterHost;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author CaMnter
 */

public class RouterFieldAnnotation {

    private String fieldValue;
    private VariableElement variableElement;


    public RouterFieldAnnotation(Element element) {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException(
                String.format(
                    "Only field can be annotated with [@%1$s]",
                    RouterField.class.getSimpleName()
                )
            );
        }
        this.variableElement = (VariableElement) element;
        RouterField routerField = this.variableElement.getAnnotation(RouterField.class);
        this.fieldValue = routerField.value();
        if ("".equals(this.fieldValue)) {
            throw new IllegalArgumentException(
                String.format(
                    "value() in [%1$s] for field [%2$s] is not valid",
                    RouterHost.class.getSimpleName(),
                    element.getSimpleName())
            );
        }
    }


    TypeMirror getFieldType() {
        return this.variableElement.asType();
    }


    String getFieldValue() {
        return this.fieldValue;
    }


    Name getFieldName() {
        return this.variableElement.getSimpleName();
    }


    public VariableElement getVariableElement() {
        return this.variableElement;
    }

}