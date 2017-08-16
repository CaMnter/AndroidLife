package com.alibaba.android.arouter.facade.template;

/**
 * Template of syringe
 *
 * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
 * @version 1.0
 * @since 2017/2/20 下午4:41
 *
 * 注入接口
 * 会在标记上 @Autowired 注解的类，在用 JavaPoet 生成的 class 的过程中，去实现 ISyringe
 */
public interface ISyringe {
    void inject(Object target);
}
