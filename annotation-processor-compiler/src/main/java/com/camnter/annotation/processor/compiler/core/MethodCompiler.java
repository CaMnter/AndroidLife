package com.camnter.annotation.processor.compiler.core;

import com.squareup.javapoet.MethodSpec;

/**
 * @author CaMnter
 */

public interface MethodCompiler {

    MethodSpec.Builder compile();

}
