package com.camnter.annotation.processor.compiler.simple.annotation;

import com.camnter.annotation.processor.compiler.simple.SaveType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * @author CaMnter
 */

public class AnnotatedClass {

    private final TypeElement annotatedElement;
    private final List<SaveViewField> saveViewFields;
    private final List<SaveOnClickMethod> saveOnClickMethods;
    private final Elements elements;
    private final Messager messager;


    public AnnotatedClass(TypeElement annotatedElement,
                          Elements elements,
                          Messager messager) {
        this.annotatedElement = annotatedElement;
        this.messager = messager;
        this.saveViewFields = new ArrayList<>();
        this.saveOnClickMethods = new ArrayList<>();
        this.elements = elements;
    }


    public String getQualifiedName() {
        return this.annotatedElement.getQualifiedName().toString();
    }


    public void addSaveViewField(SaveViewField saveViewField) {
        this.saveViewFields.add(saveViewField);
    }


    public void addSaveOnClickMethod(SaveOnClickMethod saveOnClickMethod) {
        this.saveOnClickMethods.add(saveOnClickMethod);
    }


    public JavaFile getJavaFile() {

        // void save(T target, Adapter adapter)
        MethodSpec.Builder saveMethod = MethodSpec
            .methodBuilder("save")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .addParameter(TypeName.get(this.annotatedElement.asType()), "target", Modifier.FINAL)
            .addParameter(SaveType.ADAPTER, "adapter", Modifier.FINAL);

        // findViewById
        for (SaveViewField saveViewField : this.saveViewFields) {
            saveMethod.addStatement("target.$N = ($T) (adapter.findViewById(target, $L))",
                saveViewField.getFieldName(),
                ClassName.get(saveViewField.getFieldType()), saveViewField.getResId());
        }

        // setOnClickListener
        for (SaveOnClickMethod saveOnClickMethod : this.saveOnClickMethods) {
            final boolean firstParamterViewExist = saveOnClickMethod.isFirstParamterViewExist();
            for (final int id : saveOnClickMethod.getIds()) {
                saveMethod.addStatement("adapter.findViewById(target, $L).setOnClickListener($L)",
                    id, TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(SaveType.ANDROID_ON_CLICK_LISTENER)
                        .addMethod(
                            MethodSpec
                                .methodBuilder("onClick")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.VOID)
                                .addParameter(SaveType.ANDROID_VIEW, "view")
                                // onClick(View) or onClick()
                                .addStatement(
                                    firstParamterViewExist ? "target.$N(view)" : "target.$N()",
                                    saveOnClickMethod.getMethodName())
                                .build()
                        )
                        .build()
                );
            }
        }

        // _Save
        TypeSpec saveClass = TypeSpec.classBuilder(
            this.annotatedElement.getSimpleName() + "_Save")
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ParameterizedTypeName.get(SaveType.SAVE,
                TypeName.get(this.annotatedElement.asType())))
            .addMethod(saveMethod.build())
            .build();

        String packageName = this.elements
            .getPackageOf(this.annotatedElement).getQualifiedName().toString();

        return JavaFile.builder(packageName, saveClass).build();

    }


    public String getFullClassName() {
        return this.annotatedElement.getQualifiedName().toString();
    }

}
