package com.camnter.smartsave.compiler.annotation;

import com.camnter.smartsave.annotation.SaveString;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;

/**
 * @author CaMnter
 */

public class SaveStringField {

    private final int resId;
    private final VariableElement variableElement;


    public SaveStringField(Element element) {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException(
                String.format(
                    "Only fields can be annotated with [@%1$s]",
                    SaveString.class.getSimpleName()
                )
            );
        }
        this.variableElement = (VariableElement) element;
        SaveString saveString = this.variableElement.getAnnotation(SaveString.class);
        this.resId = saveString.value();

        if (this.resId < 0) {
            throw new IllegalArgumentException(
                String.format(
                    "value() in [%1$s] for field [%2$s] is not valid",
                    SaveString.class.getSimpleName(),
                    element.getSimpleName())
            );
        }
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
