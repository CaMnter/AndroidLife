package com.camnter.annotation.processor.compiler.simple.annotation;

import com.camnter.annotation.processor.annotation.SaveOnClick;
import com.camnter.annotation.processor.compiler.simple.SaveType;
import com.squareup.javapoet.ClassName;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;

/**
 * @author CaMnter
 */

public class SaveOnClickMethod {

    private int[] ids;
    private List<? extends VariableElement> parameters;
    private Name methodName;
    private boolean firstParameterViewExist = false;


    public SaveOnClickMethod(Element element) throws IllegalArgumentException {
        if (element.getKind() != ElementKind.METHOD) {
            throw new IllegalArgumentException(
                String.format("Only methods can be annotated with [@%1$s]",
                    SaveOnClick.class.getSimpleName()));
        }

        final ExecutableElement methodElement = (ExecutableElement) element;
        this.methodName = methodElement.getSimpleName();
        this.ids = methodElement.getAnnotation(SaveOnClick.class).value();
        this.parameters = methodElement.getParameters();

        for (int id : ids) {
            if (id < 0) {
                throw new IllegalArgumentException(
                    String.format("Must set valid id for [@%1$s]",
                        SaveOnClick.class.getSimpleName()));
            }
        }
        if (this.parameters.size() == 0) return;
        VariableElement parameter;
        if (this.parameters.size() > 1) {
            throw new IllegalArgumentException(
                String.format(
                    "The method annotated with [@%1$s] must have only one parameters (View view)",
                    SaveOnClick.class.getSimpleName()));
        }
        if (this.parameters.size() == 1 && (parameter = this.parameters.get(0)) != null &&
            !SaveType.ANDROID_VIEW.toString()
                .equals(ClassName.get(parameter.asType()).toString())) {
            throw new IllegalArgumentException(
                String.format(
                    "The method annotated with [@%1$s] must have only one parameters (View view)",
                    SaveOnClick.class.getSimpleName()));
        }
        this.firstParameterViewExist = true;
    }


    Name getMethodName() {
        return this.methodName;
    }


    int[] getIds() {
        return this.ids;
    }


    public boolean isFirstParameterViewExist() {
        return this.firstParameterViewExist;
    }

}
