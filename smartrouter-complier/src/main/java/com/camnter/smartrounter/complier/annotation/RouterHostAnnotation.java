package com.camnter.smartrounter.complier.annotation;

import com.camnter.smartrounter.annotation.RouterHost;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author CaMnter
 */

public class RouterHostAnnotation {

    private String[] host;
    private VariableElement variableElement;


    public RouterHostAnnotation(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            throw new IllegalArgumentException(
                String.format(
                    "Only interface, class, @interface and annotation can be annotated with [@%1$s]",
                    RouterHost.class.getSimpleName()
                )
            );
        }
        this.variableElement = (VariableElement) element;
        RouterHost routerHost = this.variableElement.getAnnotation(RouterHost.class);
        this.host = routerHost.value();
        if (host.length == 0) {
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


    String[] getHost() {
        return this.host;
    }


    Name getFieldName() {
        return this.variableElement.getSimpleName();
    }


    public VariableElement getVariableElement() {
        return this.variableElement;
    }

}
