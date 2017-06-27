package com.camnter.annotation.processor.compiler.simple.annotation;

import com.camnter.annotation.processor.annotation.SaveString;
import com.camnter.annotation.processor.compiler.simple.ValueIllegalArgumentException;
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
            throw new ValueIllegalArgumentException(SaveString.class);
        }
        this.variableElement = (VariableElement) element;
        SaveString saveString = this.variableElement.getAnnotation(SaveString.class);
        this.resId = saveString.value();

        if (this.resId < 0) {
            throw new ValueIllegalArgumentException(SaveString.class, this.variableElement);
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
