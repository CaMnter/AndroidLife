package com.camnter.smartsave.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author CaMnter
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface SaveColor {

    int value() default 0;

}
