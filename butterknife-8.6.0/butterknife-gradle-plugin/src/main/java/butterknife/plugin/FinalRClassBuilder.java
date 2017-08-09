package butterknife.plugin;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Generates a class that contains all supported field names in an R file as final values.
 * Also enables adding support annotations to indicate the type of resource for every field.
 */
public final class FinalRClassBuilder {
    private static final String SUPPORT_ANNOTATION_PACKAGE = "android.support.annotation";
    private static final String[] SUPPORTED_TYPES = {
        "array", "attr", "bool", "color", "dimen", "drawable", "id", "integer", "string"
    };


    private FinalRClassBuilder() { }


    /**
     * JavaPoet 生成 R2
     *
     * @param rFile R.java File
     * @param outputDir R2.java 输出文件夹
     * @param packageName 包名
     * @param className R2 name
     * @throws Exception
     */
    public static void brewJava(File rFile, File outputDir, String packageName, String className)
        throws Exception {
    /*
     * JavaParser 解析 R.java File
     * 获取到 TypeDeclaration
     */
        CompilationUnit compilationUnit = JavaParser.parse(rFile);
        TypeDeclaration resourceClass = compilationUnit.getTypes().get(0);

    /*
     * 定义 R2.java class
     */
        TypeSpec.Builder result =
            TypeSpec.classBuilder(className).addModifiers(PUBLIC).addModifiers(FINAL);

    /*
     * 遍历 R.java File 的每一个节点（ 内部类或者接口 --> ClassOrInterfaceDeclaration ）
     * 添加到 R2.java 内
     * 这里是给 TypeSpec 添加生成 内部类 的 语句
     */
        for (Node node : resourceClass.getChildNodes()) {
            if (node instanceof ClassOrInterfaceDeclaration) {
                addResourceType(Arrays.asList(SUPPORTED_TYPES), result,
                    (ClassOrInterfaceDeclaration) node);
            }
        }

        JavaFile finalR = JavaFile.builder(packageName, result.build())
            .addFileComment("Generated code from Butter Knife gradle plugin. Do not modify!")
            .build();

        finalR.writeTo(outputDir);
    }


    /**
     * 复制资源 内部 class 到 R2 class 中
     *
     * 这里是给 R2 的 TypeSpec 添加生成局域
     *
     * @param supportedTypes 支持的类型（ "array", "attr", "bool", "color", "dimen", "drawable", "id",
     * "integer", "string" ）
     * @param result R2 的 TypeSpec
     * @param node R 的 接口或者类 ClassOrInterfaceDeclaration
     */
    private static void addResourceType(List<String> supportedTypes, TypeSpec.Builder result,
                                        ClassOrInterfaceDeclaration node) {

        // 判断是否是资源内部类（ "array", "attr", "bool", "color", "dimen", "drawable", "id", "integer", "string" ）
        if (!supportedTypes.contains(node.getNameAsString())) {
            return;
        }

        // 创建 R2 的内部类 TypeSpec，为了进行资源复制
        String type = node.getNameAsString();
        TypeSpec.Builder resourceType = TypeSpec.classBuilder(type)
            .addModifiers(PUBLIC, STATIC, FINAL);

        /*
         * 遍历 R 内部类的每个资源 field
         * 然后给 R2 的内部类 TypeSpec 添加 field 生成语句
         */
        for (BodyDeclaration field : node.getMembers()) {
            if (field instanceof FieldDeclaration) {
                addResourceField(resourceType, ((FieldDeclaration) field).getVariables().get(0),
                    getSupportAnnotationClass(type));
            }
        }

        // R2 的内部类 TypeSpec 添加到 R2 TypeSpec 内
        result.addType(resourceType.build());
    }


    /**
     * R2 的内部类 TypeSpec 添加 field 生成语句
     *
     * JavaParser 解析元素
     *
     * // 非 AbstractProcessor 情况下获取元素 name
     * variable.getNameAsString();
     * // 非 AbstractProcessor 情况下获取元素 值
     * variable.getInitializer().map(Node::toString).orElse(null);
     *
     * @param resourceType R2 的内部类 TypeSpec
     * @param variable 变量元素
     * @param annotation 注解
     */
    private static void addResourceField(TypeSpec.Builder resourceType, VariableDeclarator variable,
                                         ClassName annotation) {
        String fieldName = variable.getNameAsString();
        String fieldValue = variable.getInitializer().map(Node::toString).orElse(null);
        FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(int.class, fieldName)
            .addModifiers(PUBLIC, STATIC, FINAL)
            .initializer(fieldValue);

        if (annotation != null) {
            fieldSpecBuilder.addAnnotation(annotation);
        }

        resourceType.addField(fieldSpecBuilder.build());
    }


    /**
     * 生成对应的资源注解
     *
     * * "array", "attr", "bool", "color", "dimen", "drawable", "id", "integer", "string"
     *
     * 对应着
     *
     * * @ArrayRes，@AttrRes，@BoolRes，@ColorRes，@DimenRes，@DrawableRes，@IdRes，@IntegerRes，@StringRes
     *
     * @param type "array", "attr", "bool", "color", "dimen", "drawable", "id", "integer", "string"
     * @return @ArrayRes，@AttrRes，@BoolRes，@ColorRes，@DimenRes，@DrawableRes，@IdRes，@IntegerRes，@StringRes
     */
    private static ClassName getSupportAnnotationClass(String type) {
        return ClassName.get(SUPPORT_ANNOTATION_PACKAGE, capitalize(type) + "Res");
    }


    /**
     * 生成对应的资源注解名前缀
     *
     * "array", "attr", "bool", "color", "dimen", "drawable", "id", "integer", "string"
     *
     * 对应着
     *
     * Array，Attr，Bool，Color，Dimen，Drawable，Id，Integer，String
     *
     * @param word "array", "attr", "bool", "color", "dimen", "drawable", "id", "integer", "string"
     * @return Array，Attr，Bool，Color，Dimen，Drawable，Id，Integer，String
     */
    private static String capitalize(String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }
}
