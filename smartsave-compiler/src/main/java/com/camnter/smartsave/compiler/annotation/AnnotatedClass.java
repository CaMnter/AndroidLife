package com.camnter.smartsave.compiler.annotation;

import com.camnter.smartsave.compiler.SaveHelper;
import com.camnter.smartsave.compiler.SaveType;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * @author CaMnter
 */

public class AnnotatedClass {

    private final Elements elements;
    private final TypeElement annotatedElement;
    private final List<SaveField> saveFields;
    private final List<SaveColorField> saveColorFields;
    private final List<SaveOnClickMethod> saveOnClickMethods;
    private final List<SaveDimensionField> saveDimensionFields;

    private final TypeMirror annotatedElementType;
    private final String annotatedElementSimpleName;


    public AnnotatedClass(TypeElement annotatedElement,
                          Elements elements) {
        this.elements = elements;
        this.annotatedElement = annotatedElement;
        this.annotatedElementType = this.annotatedElement.asType();
        this.annotatedElementSimpleName = this.annotatedElement.getSimpleName().toString();
        this.saveFields = new ArrayList<>();
        this.saveColorFields = new ArrayList<>();
        this.saveOnClickMethods = new ArrayList<>();
        this.saveDimensionFields = new ArrayList<>();
    }


    public void addSaveField(SaveField saveField) {
        this.saveFields.add(saveField);
    }


    public void addSaveColorField(SaveColorField saveColorField) {
        this.saveColorFields.add(saveColorField);
    }


    public void addSaveOnClickMethod(SaveOnClickMethod saveOnClickMethod) {
        this.saveOnClickMethods.add(saveOnClickMethod);
    }


    public void addSaveDimensionField(SaveDimensionField saveDimensionField) {
        this.saveDimensionFields.add(saveDimensionField);
    }


    public JavaFile getJavaFile() {

        // void save(T target, Adapter adapter)
        MethodSpec.Builder saveMethod = MethodSpec
            .methodBuilder("save")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .addParameter(TypeVariableName.get("T"), "target", Modifier.FINAL)
            .addParameter(SaveType.ADAPTER, "adapter", Modifier.FINAL);

        // findViewById, getString, getDrawable
        for (SaveField saveField : this.saveFields) {
            final TypeMirror fieldType = saveField.getFieldType();
            final String fieldTypeString;
            if (SaveHelper.isSubtypeOfType(fieldType, SaveType.ANDROID_VIEW.toString())) {
                // findViewById
                saveMethod.addStatement(
                    "target.$N = ($T) (adapter.findViewById(target, $L))",
                    saveField.getFieldName(),
                    ClassName.get(fieldType),
                    saveField.getResId()
                );
            } else if ((SaveType.STRING.toString()).equals(
                (fieldTypeString = fieldType.toString()))) {
                // getString
                saveMethod.addStatement(
                    "target.$N = adapter.getString(target, $L)",
                    saveField.getFieldName(),
                    saveField.getResId()
                );
            } else if ((SaveType.ANDROID_DRAWABLE.toString()).equals(fieldTypeString)) {
                // getDrawable
                saveMethod.addStatement(
                    "target.$N = adapter.getDrawable(target, $L)",
                    saveField.getFieldName(),
                    saveField.getResId()
                );
            }
        }

        // getColor
        for (SaveColorField saveColorField : this.saveColorFields) {
            saveMethod.addStatement(
                "target.$N = adapter.getColor(target, $L)",
                saveColorField.getFieldName(),
                saveColorField.getResId()
            );
        }

        // getDimension, getDimensionPixelSize
        TypeName typeName;
        for (SaveDimensionField saveDimensionField : this.saveDimensionFields) {
            if ((typeName = TypeName.get(saveDimensionField.getFieldType())).equals(
                TypeName.FLOAT)) {
                // getDimension
                saveMethod.addStatement(
                    "target.$N = adapter.getDimension(target, $L)",
                    saveDimensionField.getFieldName(),
                    saveDimensionField.getResId()
                );
            } else if (typeName.equals(TypeName.INT)) {
                // getDimensionPixelSize
                saveMethod.addStatement(
                    "target.$N = adapter.getDimensionPixelSize(target, $L)",
                    saveDimensionField.getFieldName(),
                    saveDimensionField.getResId()
                );
            }
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
                                .addParameter(SaveType.ANDROID_VIEW, "view", Modifier.FINAL)
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

        // _Save
        TypeSpec saveClass = TypeSpec.classBuilder(
            this.annotatedElementSimpleName + "_Save")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(
                TypeVariableName.get("T", TypeName.get(this.annotatedElementType)))
            .addSuperinterface(ParameterizedTypeName.get(SaveType.SAVE,
                TypeVariableName.get("T")))
            .addMethod(saveMethod.build())
            .build();

        final String packageName = this.elements
            .getPackageOf(this.annotatedElement).getQualifiedName().toString();

        return JavaFile.builder(packageName, saveClass).build();

    }


    public String getFullClassName() {
        return this.annotatedElement.getQualifiedName().toString();
    }

}
