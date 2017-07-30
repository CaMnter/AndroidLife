package com.camnter.smartrounter.complier.annotation;

import com.camnter.smartrounter.complier.core.BaseAnnotatedInterface;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;

/**
 * @author CaMnter
 */

public class RouterManagerClass implements BaseAnnotatedInterface {

    private static final String PACKAGE_NAME = "com.camnter.smartrouter";

    private final String className;
    private final Map<String, RouterClass> routerClassHashMap;


    public RouterManagerClass(String targetModuleName,
                              Map<String, RouterClass> routerClassHashMap) {
        final String targetModuleName1 = targetModuleName == null ? "Main" : targetModuleName;
        this.className = targetModuleName1 + "RouterManagerClass";
        this.routerClassHashMap = routerClassHashMap;
    }


    /**
     * get the JavaFile
     *
     * @return JavaFile
     */
    @Override
    public JavaFile javaFile() {
        TypeSpec routerManagerClass = TypeSpec
            .classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("Generated code from SmartRouter. Do not modify !\n\n")
            .addJavadoc("@author CaMnter\n")
            /*
             * static {
             * -   loadingClass;
             * -   ...
             * }
             */
            .addStaticBlock(this.staticBlockBuilder().build())
            // public void loadingClass()
            .addMethods(this.loadingClassMethod())
            .build();

        return JavaFile.builder(PACKAGE_NAME, routerManagerClass).build();
    }


    /**
     * static {
     * -   loadingClass;
     * -   ...
     * }
     *
     * @return CodeBlock.Builder
     */
    private CodeBlock.Builder staticBlockBuilder() {
        CodeBlock.Builder staticBlockBuilder = CodeBlock.builder();
        for (RouterClass routerClass : this.routerClassHashMap.values()) {
            staticBlockBuilder.add(
                "loading" + routerClass.getSimpleName() + "SmartRouter" + "();\n");
        }
        return staticBlockBuilder;
    }


    /**
     * public void loadingClass()
     *
     * @return List<MethodSpec>
     */
    private List<MethodSpec> loadingClassMethod() {
        final List<MethodSpec> loadingClassMethods = new ArrayList<>();
        for (RouterClass routerClass : this.routerClassHashMap.values()) {
            final MethodSpec.Builder loadingClassMethodBuilder = MethodSpec
                .methodBuilder("loading" + routerClass.getSimpleName() + "SmartRouter")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addCode("try {\n")
                .addCode("    Class.forName($S);\n",
                    routerClass.getFullClassName() + "_SmartRouter")
                .addCode("} catch (ClassNotFoundException e) {\n")
                .addCode("    e.printStackTrace();\n")
                .addCode("}\n");
            loadingClassMethods.add(loadingClassMethodBuilder.build());
        }
        return loadingClassMethods;
    }

}
