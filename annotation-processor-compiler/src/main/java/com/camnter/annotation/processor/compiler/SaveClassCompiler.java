package com.camnter.annotation.processor.compiler;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @author CaMnter
 */

public class SaveClassCompiler implements ClassCompiler {

    private final TypeElement annotationElement;
    private final MethodSpec.Builder methodBuilder;


    public SaveClassCompiler(TypeElement annotationElement, MethodSpec.Builder methodBuilder) {
        this.annotationElement = annotationElement;
        this.methodBuilder = methodBuilder;
    }


    @Override public TypeSpec compile() {
        return TypeSpec.classBuilder(this.annotationElement.getSimpleName() + "Save")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(methodBuilder.build())
            .build();
    }

}
