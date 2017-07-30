package com.camnter.smartrounter.complier;

import com.camnter.smartrounter.complier.annotation.RouterClass;
import com.camnter.smartrounter.complier.annotation.RouterFieldAnnotation;
import com.camnter.smartrounter.complier.annotation.RouterHostAnnotation;
import com.camnter.smartrounter.complier.annotation.RouterManagerClass;
import com.camnter.smartrounter.complier.core.BaseProcessor;
import com.camnter.smartrouter.annotation.RouterField;
import com.camnter.smartrouter.annotation.RouterHost;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
public class RouterProcessor extends BaseProcessor {

    private Map<String, RouterClass> routerClassHashMap = new HashMap<>();


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
                this.add(RouterHost.class.getCanonicalName());
                this.add(RouterField.class.getCanonicalName());
            }
        };
    }


    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        return new LinkedHashSet<Class<? extends Annotation>>() {
            {
                this.add(RouterHost.class);
                this.add(RouterField.class);
            }
        };
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        this.routerClassHashMap.clear();
        try {
            this.processRouterHost(roundEnv);
            this.processRouterField(roundEnv);
        } catch (Exception e) {
            this.e(e.getMessage());
            e.printStackTrace();
            return true;
        }
        for (RouterClass routerClass : this.routerClassHashMap.values()) {
            try {
                this.i("[RouterProcessor]   [RouterClass]   [process]   [annotatedClass] = %1$s",
                    routerClass.getFullClassName());
                routerClass.javaFile().writeTo(this.filer);
            } catch (IOException e) {
                this.i("[RouterProcessor]   [RouterClass]   [process]   [IOException] = %1$s",
                    e.getMessage());
                return true;
            }
        }
        // RouterManagerClass
        try {
            new RouterManagerClass(this.targetModuleName, this.routerClassHashMap)
                .javaFile()
                .writeTo(this.filer);
        } catch (IOException e) {
            this.i("[RouterProcessor]   [RouterManagerClass]   [process]   [IOException] = %1$s",
                e.getMessage());
            return true;
        }
        return true;
    }


    private void processRouterHost(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(RouterHost.class)) {
            RouterClass routerClass = this.getAnnotatedClass(element);
            RouterHostAnnotation routerHostAnnotation = new RouterHostAnnotation(element);
            routerClass.addRouterHostAnnotation(routerHostAnnotation);
        }
    }


    private void processRouterField(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(RouterField.class)) {
            RouterClass routerClass = this.getAnnotatedClass(element);
            RouterFieldAnnotation routerFieldAnnotation = new RouterFieldAnnotation(element);
            routerClass.addRouterFieldAnnotation(routerFieldAnnotation);
        }
    }


    private RouterClass getAnnotatedClass(Element element) {
        final String fullClassName = this.getAnnotatedClassFullName(element);
        RouterClass routerClass = this.routerClassHashMap.get(fullClassName);
        if (routerClass == null) {
            routerClass = new RouterClass(element, this.elements, fullClassName);
            this.routerClassHashMap.put(fullClassName, routerClass);
        }
        return routerClass;
    }

}
