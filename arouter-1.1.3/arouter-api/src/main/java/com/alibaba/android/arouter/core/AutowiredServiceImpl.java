package com.alibaba.android.arouter.core;

import android.content.Context;
import android.util.LruCache;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.service.AutowiredService;
import com.alibaba.android.arouter.facade.template.ISyringe;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.android.arouter.utils.Consts.SUFFIX_AUTOWIRED;

/**
 * Autowired service impl.
 *
 * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
 * @version 1.0
 * @since 2017/2/28 下午6:08
 *
 * 拦截器服务，实现了 AutowiredService 接口
 * 作为一个固定的 拦截器服务，固定地址 /arouter/service/autowired
 *
 * {@link AutowiredServiceImpl#init(Context)}
 * 初始化方法
 * 初始化缓存数据结构，其中 LRU 缓存大小为 66
 *
 * {@link AutowiredServiceImpl#autowire(Object)}
 * 自动注入
 *
 * 先从类名缓存查看是否有
 * 有的话，跳过
 * 无，缓存到类名缓存，和 LRU 类名 与 类 的关系缓存内
 *
 * 这里的类名是元素类名：比如 TestActivity，类名是 TestActvity
 * 但是类是改元素的注入生成类，实现了 ISyringe 接口
 * 即，TestActivity$$ARouter$$Autowired
 */
@Route(path = "/arouter/service/autowired")
public class AutowiredServiceImpl implements AutowiredService {

    /*
     * LRU 缓存 实现 ISyringe 接口的 JavaPoet 生成类 和 类名
     *
     * 这里的类名是元素类名：比如 TestActivity，类名是 TestActvity
     * 但是类是改元素的注入生成类，实现了 ISyringe 接口
     * 即，TestActivity$$ARouter$$Autowired
     */
    private LruCache<String, ISyringe> classCache;
    // 缓存 实现 ISyringe 接口的 JavaPoet 生成类名
    private List<String> blackList;


    /**
     * 初始化方法
     *
     * 初始化缓存数据结构，其中 LRU 缓存大小为 66
     *
     * @param context context
     */
    @Override
    public void init(Context context) {
        classCache = new LruCache<>(66);
        blackList = new ArrayList<>();
    }


    /**
     * 自动注入
     *
     * 先从类名缓存查看是否有
     * 有的话，跳过
     * 无，缓存到类名缓存，和 LRU 类名 与 类 的关系缓存内
     *
     * 这里的类名是元素类名：比如 TestActivity，类名是 TestActvity
     * 但是类是改元素的注入生成类，实现了 ISyringe 接口
     * 即，TestActivity$$ARouter$$Autowired
     *
     * @param instance the instance who need autowired.
     */
    @Override
    public void autowire(Object instance) {
        String className = instance.getClass().getName();
        try {
            if (!blackList.contains(className)) {
                ISyringe autowiredHelper = classCache.get(className);
                if (null == autowiredHelper) {  // No cache.
                    autowiredHelper = (ISyringe) Class.forName(
                        instance.getClass().getName() + SUFFIX_AUTOWIRED)
                        .getConstructor()
                        .newInstance();
                }
                autowiredHelper.inject(instance);
                classCache.put(className, autowiredHelper);
            }
        } catch (Exception ex) {
            blackList.add(className);    // This instance need not autowired.
        }
    }
}
