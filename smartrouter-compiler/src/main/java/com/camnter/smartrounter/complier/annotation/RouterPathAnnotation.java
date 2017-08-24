package com.camnter.smartrounter.complier.annotation;

import com.camnter.smartrouter.annotation.RouterPath;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

/**
 * @author CaMnter
 */

public class RouterPathAnnotation {

    private String[] host;
    private Element element;


    public RouterPathAnnotation(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            throw new IllegalArgumentException(
                String.format(
                    "Only class can be annotated with [@%1$s]",
                    RouterPath.class.getSimpleName()
                )
            );
        }
        this.element = element;
        RouterPath routerPath = this.element.getAnnotation(RouterPath.class);
        this.host = routerPath.value();
        if (this.host.length == 0) {
            throw new IllegalArgumentException(
                String.format(
                    "value() in [%1$s] for field [%2$s] is not valid",
                    RouterPath.class.getSimpleName(),
                    element.getSimpleName())
            );
        }
    }


    TypeMirror getFieldType() {
        return this.element.asType();
    }


    String[] getHost() {
        return this.host;
    }


    Name getFieldName() {
        return this.element.getSimpleName();
    }


    public Element getElement() {
        return this.element;
    }

}
