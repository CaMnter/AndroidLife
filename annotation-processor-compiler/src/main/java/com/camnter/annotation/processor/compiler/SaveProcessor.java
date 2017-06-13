package com.camnter.annotation.processor.compiler;

import com.camnter.annotation.processor.annotation.SaveActivity;
import com.camnter.annotation.processor.annotation.SaveView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * @author CaMnter
 */

@AutoService(Processor.class)
public class SaveProcessor extends AbstractProcessor {

    private Elements elements;


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


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elements = processingEnv.getElementUtils();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("[SaveProcessor]   [process]");
        Set<? extends Element> elementSet = roundEnv.getElementsAnnotatedWith(SaveActivity.class);
        for (Element element : elementSet) {
            // SaveActivity 注解的类
            TypeElement typeElement = (TypeElement) element;
            List<? extends Element> memberList = this.elements.getAllMembers(typeElement);
            MethodSpec.Builder saveViewMethodBuilder = MethodSpec
                .methodBuilder("saveView")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(ClassName.get(typeElement.asType()), "activity");
            for (Element item : memberList) {
                // SaveView 注解的成员变量
                SaveView saveView = item.getAnnotation(SaveView.class);
                if (saveView == null) {
                    continue;
                }
                saveViewMethodBuilder.addStatement(
                    String.format("activity.%s = (%s) activity.findViewById(%s)",
                        /* Simple view name */
                        item.getSimpleName(),
                        /* Full view name */
                        ClassName.get(item.asType()).toString(),
                        /* R.id */
                        saveView.value()
                    )
                );
            }
            TypeSpec classType = TypeSpec.classBuilder(element.getSimpleName() + "Save")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(saveViewMethodBuilder.build())
                .build();
            JavaFile javaFile = JavaFile
                .builder(this.getPackageName(typeElement), classType)
                .build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
