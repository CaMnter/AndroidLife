package com.camnter.smartsave.compiler;

import com.squareup.javapoet.ClassName;

/**
 * @author CaMnter
 */

public interface SaveType {

    ClassName ADAPTER = ClassName.get("com.camnter.smartsave.adapter",
        "Adapter");

    ClassName SAVE = ClassName.get("com.camnter.smartsave.save",
        "Save");

    ClassName ANDROID_ON_CLICK_LISTENER = ClassName.get("android.view", "View", "OnClickListener");

    ClassName ANDROID_VIEW = ClassName.get("android.view", "View");

    ClassName STRING = ClassName.get("java.lang", "String");

    ClassName ANDROID_DRAWABLE = ClassName.get("android.graphics.drawable", "Drawable");

}
