package com.camnter.annotation.processor.compiler.simple;

import com.camnter.annotation.processor.annotation.SaveOnClick;
import com.camnter.annotation.processor.annotation.SaveString;
import com.camnter.annotation.processor.annotation.SaveView;
import com.camnter.annotation.processor.compiler.core.BaseProcessor;
import com.camnter.annotation.processor.compiler.simple.annotation.AnnotatedClass;
import com.camnter.annotation.processor.compiler.simple.annotation.SaveOnClickMethod;
import com.camnter.annotation.processor.compiler.simple.annotation.SaveStringField;
import com.camnter.annotation.processor.compiler.simple.annotation.SaveViewField;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    private Map<String, AnnotatedClass> annotatedClassHashMap = new HashMap<>();


    private String getPackageName(final TypeElement type) {
        return this.elements
            .getPackageOf(type)
            .getQualifiedName()
            .toString();
    }


    /**
     * 规定需要处理的注解
     *
     * @return Set
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {
            {
                this.add(SaveView.class.getCanonicalName());
                this.add(SaveOnClick.class.getCanonicalName());
                this.add(SaveString.class.getCanonicalName());
            }
        };
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
        this.annotatedClassHashMap.clear();
        try {
            this.processSaveView(roundEnv);
            this.processSaveOnClick(roundEnv);
            this.processSaveString(roundEnv);
        } catch (Exception e) {
            this.e(e.getMessage());
            e.printStackTrace();
            return true;
        }
        for (AnnotatedClass annotatedClass : this.annotatedClassHashMap.values()) {
            try {
                this.i("[SaveProcessor]   [process]   [annotatedClass] = %1$s",
                    annotatedClass.getFullClassName());
                annotatedClass.getJavaFile().writeTo(this.filer);
            } catch (IOException e) {
                this.i("[SaveProcessor]   [process]   [IOException] = %1$s",
                    e.getMessage());
                return true;
            }
        }
        return true;
    }


    private void processSaveView(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(SaveView.class)) {
            AnnotatedClass annotatedClass = this.getAnnotatedClass(element);
            SaveViewField saveViewField = new SaveViewField(element);
            annotatedClass.addSaveViewField(saveViewField);
        }
    }


    private void processSaveOnClick(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(SaveOnClick.class)) {
            AnnotatedClass annotatedClass = this.getAnnotatedClass(element);
            SaveOnClickMethod saveOnClickMethod = new SaveOnClickMethod(element);
            annotatedClass.addSaveOnClickMethod(saveOnClickMethod);
        }
    }


    private void processSaveString(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(SaveString.class)) {
            AnnotatedClass annotatedClass = this.getAnnotatedClass(element);
            SaveStringField saveStringField = new SaveStringField(element);
            annotatedClass.addSaveStringField(saveStringField);
        }
    }


    private AnnotatedClass getAnnotatedClass(Element element) {
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        String fullClassName = classElement.getQualifiedName().toString();
        AnnotatedClass annotatedClass = this.annotatedClassHashMap.get(fullClassName);
        if (annotatedClass == null) {
            annotatedClass = new AnnotatedClass(classElement, this.elements);
            annotatedClassHashMap.put(fullClassName, annotatedClass);
        }
        return annotatedClass;
    }

}
