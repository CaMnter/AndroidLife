package com.camnter.annotation.processor.compiler.simple;

import com.camnter.annotation.processor.annotation.SaveView;
import com.camnter.annotation.processor.compiler.core.MethodCompiler;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @author CaMnter
 */

class SaveViewCompiler implements MethodCompiler {

    private final TypeElement annotationElement;
    private final List<? extends Element> memberList;


    SaveViewCompiler(TypeElement annotationElement,
                     List<? extends Element> memberList) {
        this.annotationElement = annotationElement;
        this.memberList = memberList;
    }


    @Override public MethodSpec.Builder compile() {
        MethodSpec.Builder saveViewMethodBuilder = MethodSpec
            .methodBuilder("saveView")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeName.VOID)
            .addParameter(ClassName.get(this.annotationElement.asType()), "activity");
        for (Element member : this.memberList) {
            // SaveView 注解的成员变量
            SaveView saveView = member.getAnnotation(SaveView.class);
            if (saveView == null) {
                continue;
            }
            saveViewMethodBuilder.addStatement(
                String.format("activity.%s = (%s) activity.findViewById(%s)",
                        /* Simple view name */
                    member.getSimpleName(),
                        /* Full view name */
                    ClassName.get(member.asType()).toString(),
                        /* R.id */
                    saveView.value()
                )
            );
        }
        return saveViewMethodBuilder;
    }

}
