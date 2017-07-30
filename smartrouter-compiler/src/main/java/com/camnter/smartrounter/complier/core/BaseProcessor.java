package com.camnter.smartrounter.complier.core;

import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * @author CaMnter
 */

public abstract class BaseProcessor extends AbstractProcessor {

    protected ProcessingEnvironment processingEnvironment;

    protected Filer filer;
    protected Elements elements;
    protected Messager messager;
    protected String targetModuleName;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.processingEnvironment = processingEnvironment;
        this.filer = processingEnvironment.getFiler();
        this.elements = processingEnvironment.getElementUtils();
        this.messager = processingEnvironment.getMessager();

        final Map<String, String> optionsMap = this.processingEnv.getOptions();
        final Set<String> keySet = optionsMap.keySet();
        for (String key : keySet) {
            if ("targetModuleName".equals(key)) {
                this.targetModuleName = optionsMap.get(key);
            }
        }
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    public void e(String messageFormat, Object... args) {
        this.messager.printMessage(Diagnostic.Kind.ERROR, String.format(messageFormat, args));
    }


    public void i(String messageFormat, Object... args) {
        this.messager.printMessage(Diagnostic.Kind.NOTE, String.format(messageFormat, args));
    }


    public void w(String messageFormat, Object... args) {
        this.messager.printMessage(Diagnostic.Kind.WARNING, String.format(messageFormat, args));
    }


    protected String getAnnotatedClassFullName(Element element) {
        final ElementKind elementKind = element.getKind();
        switch (elementKind) {
            case CLASS:
            case INTERFACE:
                return this.getPackageName(element) + "." + element.getSimpleName();
            case PACKAGE:
                return this.getPackageName(element);
            case ANNOTATION_TYPE:
                final Element annotationTypeEnclosingElement = element.getEnclosingElement();
                switch (annotationTypeEnclosingElement.getKind()) {
                    case CLASS:
                    case INTERFACE:
                    case ANNOTATION_TYPE:
                        return this.getPackageName(element) + "." + element.getSimpleName();
                    case PACKAGE:
                        return this.getPackageName(element);
                    default:
                    case FIELD:
                        return ((TypeElement) element.getEnclosingElement()).getQualifiedName()
                            .toString();
                }
            default:
            case FIELD:
                return ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString();
        }
    }


    private String getPackageName(Element element) {
        return this.elements
            .getPackageOf(element)
            .getQualifiedName()
            .toString();
    }

}
