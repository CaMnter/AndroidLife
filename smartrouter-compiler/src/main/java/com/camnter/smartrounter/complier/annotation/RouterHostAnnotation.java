package com.camnter.smartrounter.complier.annotation;

import com.camnter.smartrouter.annotation.RouterHost;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

/**
 * @author CaMnter
 */

public class RouterHostAnnotation {

    private String[] host;
    private Element element;


    public RouterHostAnnotation(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            throw new IllegalArgumentException(
                String.format(
                    "Only class can be annotated with [@%1$s]",
                    RouterHost.class.getSimpleName()
                )
            );
        }
        this.element = element;
        RouterHost routerHost = this.element.getAnnotation(RouterHost.class);
        this.host = routerHost.value();
        if (this.host.length == 0) {
            throw new IllegalArgumentException(
                String.format(
                    "value() in [%1$s] for field [%2$s] is not valid",
                    RouterHost.class.getSimpleName(),
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
