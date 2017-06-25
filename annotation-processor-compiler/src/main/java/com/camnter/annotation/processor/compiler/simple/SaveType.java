package com.camnter.annotation.processor.compiler.simple;

import com.squareup.javapoet.ClassName;

/**
 * @author CaMnter
 */

public interface SaveType {

    ClassName ADAPTER = ClassName.get("com.camnter.smartsave.adapter",
        "Adapter");

    ClassName SAVE = ClassName.get("com.camnter.smartsave.save",
        "SAVE");

}
