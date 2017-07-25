package com.camnter.smartrouter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author CaMnter
 */

@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface RouterField {

    String value();

}
