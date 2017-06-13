package com.camnter.annotation.processor.compiler;

import com.squareup.javapoet.MethodSpec;

/**
 * @author CaMnter
 */

interface MethodCompiler {

    MethodSpec.Builder compile();

}
