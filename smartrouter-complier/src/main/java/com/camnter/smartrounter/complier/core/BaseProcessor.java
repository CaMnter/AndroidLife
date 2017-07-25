package com.camnter.smartrounter.complier.core;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
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


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.processingEnvironment = processingEnvironment;
        this.filer = processingEnvironment.getFiler();
        this.elements = processingEnvironment.getElementUtils();
        this.messager = processingEnvironment.getMessager();
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

}
