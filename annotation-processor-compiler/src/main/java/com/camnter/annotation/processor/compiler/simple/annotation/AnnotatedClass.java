package com.camnter.annotation.processor.compiler.simple.annotation;

import com.camnter.annotation.processor.compiler.simple.SaveType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * @author CaMnter
 */

public class AnnotatedClass {

    private final Elements elements;
    private final TypeElement annotatedElement;
    private final List<SaveViewField> saveViewFields;
    private final List<SaveStringField> saveStringFields;
    private final List<SaveOnClickMethod> saveOnClickMethods;


    public AnnotatedClass(TypeElement annotatedElement,
                          Elements elements) {
        this.elements = elements;
        this.annotatedElement = annotatedElement;
        this.saveViewFields = new ArrayList<>();
        this.saveStringFields = new ArrayList<>();
        this.saveOnClickMethods = new ArrayList<>();
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


    public void addSaveStringField(SaveStringField saveStringField) {
        this.saveStringFields.add(saveStringField);
    }


    public JavaFile getJavaFile() {

        // void save(T target, Adapter adapter)
        MethodSpec.Builder saveMethod = MethodSpec
            .methodBuilder("save")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .addParameter(TypeVariableName.get("T"), "target", Modifier.FINAL)
            .addParameter(SaveType.ADAPTER, "adapter", Modifier.FINAL);

        // findViewById
        for (SaveViewField saveViewField : this.saveViewFields) {
            saveMethod.addStatement(
                "target.$N = ($T) (adapter.findViewById(target, $L))",
                saveViewField.getFieldName(),
                ClassName.get(saveViewField.getFieldType()),
                saveViewField.getResId()
            );
        }

        // setOnClickListener
        for (SaveOnClickMethod saveOnClickMethod : this.saveOnClickMethods) {
            final boolean firstParameterViewExist = saveOnClickMethod.isFirstParameterViewExist();
            for (final int id : saveOnClickMethod.getIds()) {
                saveMethod.addStatement(
                    "adapter.findViewById(target, $L).setOnClickListener($L)",
                    id,
                    TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(SaveType.ANDROID_ON_CLICK_LISTENER)
                        .addMethod(
                            MethodSpec
                                .methodBuilder("onClick")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.VOID)
                                .addParameter(SaveType.ANDROID_VIEW, "view")
                                // onClick(View view) or onClick()
                                .addStatement(
                                    firstParameterViewExist ? "target.$N(view)" : "target.$N()",
                                    saveOnClickMethod.getMethodName())
                                .build()
                        )
                        .build()
                );
            }
        }

        // getString
        for (SaveStringField saveStringField : this.saveStringFields) {
            saveMethod.addStatement(
                "target.$N = adapter.getString(target, $L)",
                saveStringField.getFieldName(),
                saveStringField.getResId()
            );
        }

        // _Save
        TypeSpec saveClass = TypeSpec.classBuilder(
            this.annotatedElement.getSimpleName() + "_Save")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(
                TypeVariableName.get("T", TypeName.get(this.annotatedElement.asType())))
            .addSuperinterface(ParameterizedTypeName.get(SaveType.SAVE,
                TypeVariableName.get("T")))
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
