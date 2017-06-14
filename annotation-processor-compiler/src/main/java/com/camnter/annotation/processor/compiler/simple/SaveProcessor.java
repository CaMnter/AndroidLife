package com.camnter.annotation.processor.compiler.simple;

import com.camnter.annotation.processor.annotation.SaveActivity;
import com.camnter.annotation.processor.compiler.core.BaseProcessor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * @author CaMnter
 */

@AutoService(Processor.class)
public class SaveProcessor extends BaseProcessor {

    private String getPackageName(final TypeElement type) {
        return this.elements
            .getPackageOf(type)
            .getQualifiedName()
            .toString();
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        // 规定需要处理的注解
        return Collections.singleton(SaveActivity.class.getCanonicalName());
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
        this.i(">>>>>>>> [SaveProcessor]   [process] :");
        Set<? extends Element> elementSet = roundEnv.getElementsAnnotatedWith(SaveActivity.class);
        for (Element element : elementSet) {
            // SaveActivity 注解的类
            TypeElement annotationElement = (TypeElement) element;
            List<? extends Element> memberList = this.elements.getAllMembers(annotationElement);
            this.i(">>>>>>>> [annotationElement] = [%1$s]",
                annotationElement.getQualifiedName());
            TypeSpec classType = new SaveClassCompiler(
                annotationElement,
                new SaveViewCompiler(annotationElement, memberList).compile()
            ).compile();
            JavaFile javaFile = JavaFile
                .builder(this.getPackageName(annotationElement), classType)
                .build();
            try {
                javaFile.writeTo(this.filer);
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }
        return true;
    }

}
