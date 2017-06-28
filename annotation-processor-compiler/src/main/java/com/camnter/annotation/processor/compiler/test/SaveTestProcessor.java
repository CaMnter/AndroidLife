package com.camnter.annotation.processor.compiler.test;

import com.camnter.annotation.processor.annotation.SaveTest;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @author CaMnter
 */

@AutoService(Processor.class)
public class SaveTestProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(SaveTest.class.getCanonicalName());
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        MethodSpec main = MethodSpec
            .methodBuilder("main")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(void.class)
            .addParameter(String[].class, "args")
            .addStatement("$T.out.println($S)", System.class, "Save you from anything")
            .build();
        TypeSpec saveTest = TypeSpec
            .classBuilder("SaveTest")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(main)
            .build();
        JavaFile javaFile = JavaFile
            .builder("com.camnter.annotation.processor.output", saveTest)
            .build();
        try {
            javaFile.writeTo(this.processingEnv.getFiler());
        } catch (IOException e) {
            // e.printStackTrace();
        }
        return false;
    }

}
