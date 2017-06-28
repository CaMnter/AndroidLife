package com.camnter.smartsave.compiler.annotation;

import com.camnter.smartsave.annotation.Save;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author CaMnter
 */

public class SaveField {

    private final int resId;
    private final VariableElement variableElement;


    public SaveField(Element element) {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException(
                String.format(
                    "Only fields can be annotated with [@%1$s]",
                    Save.class.getSimpleName()
                )
            );
        }
        this.variableElement = (VariableElement) element;
        Save save = this.variableElement.getAnnotation(Save.class);
        this.resId = save.value();
        if (this.resId < 0) {
            throw new IllegalArgumentException(
                String.format(
                    "value() in [%1$s] for field [%2$s] is not valid",
                    Save.class.getSimpleName(),
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
