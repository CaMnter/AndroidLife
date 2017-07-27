package com.camnter.smartrounter.complier;

import com.squareup.javapoet.ClassName;

/**
 * @author CaMnter
 */

public interface RouterType {

    ClassName ROUTER = ClassName.get("com.camnter.smartrouter.core", "Router");
    ClassName BASE_ACTIVITY_ROUTER = ClassName.get("com.camnter.smartrouter", "BaseActivityRouter");

    ClassName ANDROID_ACTIVITY = ClassName.get("android.app", "Activity");
    ClassName ANDROID_SUPPORT_ANNOTATION_NONNULL = ClassName.get("android.support.annotation",
        "NonNull");

    String CHAR = "char";
    String BYTE = "byte";
    String SHORT = "short";
    String INT = "int";
    String FLOAT = "float";
    String DOUBLE = "double";
    String LONG = "long";
    String BOOLEAN = "boolean";

    String BOXED_CHAR = "java.lang.Character";
    String BOXED_BYTE = "java.lang.Byte";
    String BOXED_SHORT = "java.lang.Short";
    String BOXED_INT = "java.lang.Integer";
    String BOXED_FLOAT = "java.lang.Float";
    String BOXED_DOUBLE = "java.lang.Double";
    String BOXED_LONG = "java.lang.Long";
    String BOXED_BOOLEAN = "java.lang.Boolean";
    String STRING = "java.lang.String";

}
