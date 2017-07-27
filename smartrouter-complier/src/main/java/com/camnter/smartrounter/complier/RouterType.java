package com.camnter.smartrounter.complier;

import com.squareup.javapoet.ClassName;

/**
 * @author CaMnter
 */

public interface RouterType {

    ClassName ANDROID_ACTIVITY = ClassName.get("android.app", "Activity");

    ClassName ANDROID_SUPPORT_ANNOTATION_NONNULL = ClassName.get("android.support.annotation", "NonNull");

}
