package com.alibaba.android.arouter.facade.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for field, which need autowired.
 *
 * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
 * @version 1.0
 * @since 2017/2/20 下午4:26
 *
 * 类似于 Spring Autowired 注解，但是这里用于解析路由协议中的键值对
 *
 * scheme://host/path?param=?? 协议中的 param
 * 用于指定在 field 上，表示从 协议中取出值的 key name
 * 不指定 name 的话，就取 field name 为 key name
 * 从 协议中获取 值
 *
 * 还有一个 required，这个值规定这个 field 从协议中取不到值就会抛出一个 RuntimeException
 * 会崩。提供一个选择这个 field 必须能从 协议中取到值
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.CLASS)
public @interface Autowired {

    // Mark param's name or service name.
    String name() default "";

    // If required, app will be crash when value is null.
    // Primitive type wont be check!
    boolean required() default false;

    // Description of the field
    String desc() default "No desc.";
}
