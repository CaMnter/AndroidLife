package com.camnter.annotation.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author CaMnter
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface SaveActivity {
}
