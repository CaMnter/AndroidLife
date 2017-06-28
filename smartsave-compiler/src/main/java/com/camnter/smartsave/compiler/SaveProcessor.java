package com.camnter.smartsave.compiler;

import com.camnter.smartsave.annotation.Save;
import com.camnter.smartsave.annotation.SaveColor;
import com.camnter.smartsave.annotation.SaveOnClick;
import com.camnter.smartsave.compiler.annotation.AnnotatedClass;
import com.camnter.smartsave.compiler.annotation.SaveColorField;
import com.camnter.smartsave.compiler.annotation.SaveField;
import com.camnter.smartsave.compiler.annotation.SaveOnClickMethod;
import com.camnter.smartsave.compiler.core.BaseProcessor;
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
                this.add(Save.class.getCanonicalName());
                this.add(SaveColor.class.getCanonicalName());
                this.add(SaveOnClick.class.getCanonicalName());
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
            this.processSave(roundEnv);
            this.processSaveColor(roundEnv);
            this.processSaveOnClick(roundEnv);
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


    private void processSave(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(Save.class)) {
            AnnotatedClass annotatedClass = this.getAnnotatedClass(element);
            SaveField saveField = new SaveField(element, Save.class);
            annotatedClass.addSaveField(saveField);
        }
    }


    private void processSaveColor(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(SaveColor.class)) {
            AnnotatedClass annotatedClass = this.getAnnotatedClass(element);
            SaveColorField saveColorField = new SaveColorField(element, SaveColor.class);
            annotatedClass.addSaveColorField(saveColorField);
        }
    }


    private void processSaveOnClick(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(SaveOnClick.class)) {
            AnnotatedClass annotatedClass = this.getAnnotatedClass(element);
            SaveOnClickMethod saveOnClickMethod = new SaveOnClickMethod(element);
            annotatedClass.addSaveOnClickMethod(saveOnClickMethod);
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
