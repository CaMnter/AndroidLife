package com.camnter.smartsave.compiler.scanner;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

/**
 * 封装的 Id，可以识别是不是 android 资源 id
 *
 * 根据 ClassName.get("android", "R") 来判断传入进来的 id
 * 是 android.R 还是 int
 *
 * @author CaMnter
 */

final class Id {

    private static final ClassName ANDROID_R = ClassName.get("android", "R");

    final int value;
    final CodeBlock code;
    final boolean qualifed;


    Id(int value) {
        this.value = value;
        this.code = CodeBlock.of("$L", value);
        this.qualifed = false;
    }


    Id(int value, ClassName className, String resourceName) {
        this.value = value;
        this.code = className.topLevelClassName().equals(ANDROID_R)
                    ? CodeBlock.of("$L.$N", className, resourceName)
                    : CodeBlock.of("$T.$N", className, resourceName);
        this.qualifed = true;
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof Id && value == ((Id) o).value;
    }


    @Override
    public int hashCode() {
        return value;
    }


    @Override
    public String toString() {
        throw new UnsupportedOperationException("Please use value or code explicitly");
    }

}
