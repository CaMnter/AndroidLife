package com.camnter.smartrounter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author CaMnter
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RouterHost {
    String[] value();
}
