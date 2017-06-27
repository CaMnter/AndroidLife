package com.camnter.annotation.processor.compiler.simple;

import javax.lang.model.element.Element;

/**
 * @author CaMnter
 */

public class ValueIllegalArgumentException extends IllegalArgumentException {

    public ValueIllegalArgumentException(Class<?> annotationClass) {
        super(
            String.format("Only fields can be annotated with [@%1$s]",
                annotationClass.getSimpleName())
        );
    }


    public ValueIllegalArgumentException(Class<?> annotationClass,
                                         Element element) {
        super(
            String.format("value() in [%1$s] for field [%2$s] is not valid",
                annotationClass.getSimpleName(),
                element.getSimpleName())
        );
    }

}
