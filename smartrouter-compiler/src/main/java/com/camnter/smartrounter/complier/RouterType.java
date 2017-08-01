package com.camnter.smartrounter.complier;

import com.squareup.javapoet.ClassName;

/**
 * @author CaMnter
 */

public interface RouterType {

    // SmartRouter
    ClassName ROUTER = ClassName.get("com.camnter.smartrouter.core", "Router");
    ClassName SMART_ROUTERS = ClassName.get("com.camnter.smartrouter", "SmartRouters");
    ClassName BASE_ACTIVITY_ROUTER = ClassName.get("com.camnter.smartrouter", "BaseActivityRouter");

    // Android
    ClassName ANDROID_URI = ClassName.get("android.net", "Uri");
    ClassName ANDROID_INTENT = ClassName.get("android.content", "Intent");
    ClassName ANDROID_ACTIVITY = ClassName.get("android.app", "Activity");
    ClassName ANDROID_TEXT_UTILS = ClassName.get("android.text", "TextUtils");

    // Java
    ClassName JAVA_BOXED_BYTE = ClassName.get("java.lang", "Byte");
    ClassName JAVA_BOXED_SHORT = ClassName.get("java.lang", "Short");
    ClassName JAVA_BOXED_INT = ClassName.get("java.lang", "Integer");
    ClassName JAVA_BOXED_FLOAT = ClassName.get("java.lang", "Float");
    ClassName JAVA_BOXED_DOUBLE = ClassName.get("java.lang", "Double");
    ClassName JAVA_BOXED_LONG = ClassName.get("java.lang", "Long");
    ClassName JAVA_BOXED_BOOLEAN = ClassName.get("java.lang", "Boolean");

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
