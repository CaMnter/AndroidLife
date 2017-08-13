package com.alibaba.android.arouter.facade.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a interceptor to interception the route.
 * BE ATTENTION : This annotation can be mark the implements of #{IInterceptor} ONLY!!!
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/23 14:03
 *
 * 路由拦截器注解，类注解
 * 这里强烈说明了只能在实现 IInterceptor 接口类上，才能标注
 * 可以设置优先级，这样的话多个过滤器在一个地址下，会有先后顺序地过滤
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
public @interface Interceptor {
    /**
     * The priority of interceptor, ARouter will be excute them follow the priority.
     */
    int priority();

    /**
     * The name of interceptor, may be used to generate javadoc.
     */
    String name() default "Default";
}
