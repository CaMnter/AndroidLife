package com.alibaba.android.arouter.core;

import android.content.Context;
import android.net.Uri;
import com.alibaba.android.arouter.exception.HandlerException;
import com.alibaba.android.arouter.exception.NoRouteFoundException;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.enums.TypeKind;
import com.alibaba.android.arouter.facade.model.RouteMeta;
import com.alibaba.android.arouter.facade.template.IInterceptorGroup;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.android.arouter.facade.template.IProviderGroup;
import com.alibaba.android.arouter.facade.template.IRouteGroup;
import com.alibaba.android.arouter.facade.template.IRouteRoot;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.android.arouter.utils.ClassUtils;
import com.alibaba.android.arouter.utils.Consts;
import com.alibaba.android.arouter.utils.MapUtils;
import com.alibaba.android.arouter.utils.TextUtils;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static com.alibaba.android.arouter.launcher.ARouter.logger;
import static com.alibaba.android.arouter.utils.Consts.DOT;
import static com.alibaba.android.arouter.utils.Consts.ROUTE_ROOT_PAKCAGE;
import static com.alibaba.android.arouter.utils.Consts.SDK_NAME;
import static com.alibaba.android.arouter.utils.Consts.SEPARATOR;
import static com.alibaba.android.arouter.utils.Consts.SUFFIX_INTERCEPTORS;
import static com.alibaba.android.arouter.utils.Consts.SUFFIX_PROVIDERS;
import static com.alibaba.android.arouter.utils.Consts.SUFFIX_ROOT;
import static com.alibaba.android.arouter.utils.Consts.TAG;

/**
 * LogisticsCenter contain all of the map.
 * <p>
 * 1. Create instance when it first used.
 * 2. Handler Multi-Module relationship map(*)
 * 3. Complex logic to solve duplicate group definition
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/23 15:02
 *
 * {@link LogisticsCenter#init(Context, ThreadPoolExecutor)}
 * 1. 获取所有 com.alibaba.android.arouter.routes JavaPoet 生成的 class name
 * 2. 遍历改目录下所有 class name
 *
 * 2.1 是否 name 以 com.alibaba.android.arouter.routes.ARouter$$Root 开头
 * -   反射实例，强转为 IRouteRoot 后调用 loadInto，缓存 路由组
 *
 * 2.2 是否 name 以 com.alibaba.android.arouter.routes.ARouter$$Interceptors 开头
 * -   反射实例，强转为 IInterceptorGroup 后调用 loadInto，缓存 拦截器
 *
 * 2.3 是否 name 以 com.alibaba.android.arouter.routes.ARouter$$Providers 开头
 * -   反射实例，强转为 IInterceptorGroup 后调用 loadInto，缓存 功能 service 的 RouteMeta
 *
 * {@link LogisticsCenter#buildProvider(String)}
 * 通过一个 服务 name 获取一个 关系类
 *
 * {@link LogisticsCenter#completion(Postcard)}
 *
 * 往 关系类 中，添加参数。反射实例化所需要的 IProvider，并初始化
 *
 * 0. 根据关系类，拿到 路径，根据 路径 从缓存中查找 路由信息类
 *
 * 如果从缓存中 没有查到 路由信息类
 *
 * 1. 从关系类中，拿到 路由组 name，根据 路由组 name 从缓存中查找 路由组 class
 * 1.1 没有，抛异常
 * 1.2 有，继续
 *
 * 2. 根据  路由组 class 反射构造 路由组 实例
 * 3. 路由组 实例 调用 loadInto，缓存 路由信息类
 * 4. 移除该路由组缓存
 * 5. 没报异常的话，重新加载
 *
 * 重新加载后，应该不会走该系列流程
 * 缓存中能查到对应的 路由信息类
 *
 * ---
 *
 * 如果从缓存中 没有查到 路由信息类
 *
 * 1. 从路由信息类中获取到，跳转目标 class，路由类型，优先级 和 额外数据
 * -  添加到 关系类 中
 *
 *
 * 2.1 从 关系类 中，拿到 Uri
 * 2.2 如果为 null，跳出 2 到 3
 * 2.3 根据 uri，拿到该 uri 下的所有参数键值对（ key，value ）
 * 2.4 从 路由信息类 中，获取所有参数 值类型
 *
 * 2.5 如果 值类型 不为 null
 * 2.5.1 将所有参数键值对，保存到 关系类 的 bundle 内
 * 2.5.2 同时，在 关系类的 extra 上，保存 自动注入的类型（ key = ARouter.AUTO_INJECT ）
 *
 * 2.6 最后，在 关系类的 bundle 上，保存 uri（ key = ARouter.RAW_URI ）
 *
 *
 * 3. 根据 路由信息类 中，路由的类型
 *
 * 3.1.1 如果是 IProvider 类型，从 路由信息类 中拿到 跳转目标 class
 * 3.1.2 根据该 class，从缓存中获取到 IProvider 实例
 * 3.1.3 如果 IProvider 实例 为 null，反射实例化，调用 初始化方法 后放入缓存
 * 3.1.4 在 关系类 中加入该 IProvider
 * 3.1.5 在 关系类 中打开 绿色通道
 *
 * 3.2 如果是 Fragment 类型，在 关系类 中打开 绿色通道
 *
 * {@link LogisticsCenter#setValue(Postcard, Integer, String, String)}
 * 将所有参数键值对，保存到 关系类 的 bundle 内
 *
 * {@link LogisticsCenter#suspend()}
 * 清空所有路由缓存
 */
public class LogisticsCenter {
    private static Context mContext;
    static ThreadPoolExecutor executor;


    /**
     * LogisticsCenter init, load all metas in memory. Demand initialization
     *
     * 反射初始化所有 JavaPoet 生成 com.alibaba.android.arouter.routes 包下的 类
     * 然后初始化缓存所有的 路由组，拦截器 和 service 的 RouteMeta
     *
     * 1. 获取所有 com.alibaba.android.arouter.routes JavaPoet 生成的 class name
     * 2. 遍历改目录下所有 class name
     *
     * 2.1 是否 name 以 com.alibaba.android.arouter.routes.ARouter$$Root 开头
     * -   反射实例，强转为 IRouteRoot 后调用 loadInto，缓存 路由组
     *
     * 2.2 是否 name 以 com.alibaba.android.arouter.routes.ARouter$$Interceptors 开头
     * -   反射实例，强转为 IInterceptorGroup 后调用 loadInto，缓存 拦截器
     *
     * 2.3 是否 name 以 com.alibaba.android.arouter.routes.ARouter$$Providers 开头
     * -   反射实例，强转为 IInterceptorGroup 后调用 loadInto，缓存 功能 service 的 RouteMeta
     *
     * @param context Context
     * @param tpe ThreadPoolExecutor
     * @throws HandlerException exception
     */
    public synchronized static void init(Context context, ThreadPoolExecutor tpe)
        throws HandlerException {
        mContext = context;
        executor = tpe;

        try {
            // These class was generate by arouter-compiler.
            List<String> classFileNames = ClassUtils.getFileNameByPackageName(mContext,
                ROUTE_ROOT_PAKCAGE);

            for (String className : classFileNames) {
                if (className.startsWith(
                    ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
                    // This one of root elements, load root.
                    ((IRouteRoot) (Class.forName(className)
                        .getConstructor()
                        .newInstance())).loadInto(Warehouse.groupsIndex);
                } else if (className.startsWith(
                    ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_INTERCEPTORS)) {
                    // Load interceptorMeta
                    ((IInterceptorGroup) (Class.forName(className)
                        .getConstructor()
                        .newInstance())).loadInto(Warehouse.interceptorsIndex);
                } else if (className.startsWith(
                    ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_PROVIDERS)) {
                    // Load providerIndex
                    ((IProviderGroup) (Class.forName(className)
                        .getConstructor()
                        .newInstance())).loadInto(Warehouse.providersIndex);
                }
            }

            if (Warehouse.groupsIndex.size() == 0) {
                logger.error(TAG, "No mapping files were found, check your configuration please!");
            }

            if (ARouter.debuggable()) {
                logger.debug(TAG, String.format(Locale.getDefault(),
                    "LogisticsCenter has already been loaded, GroupIndex[%d], InterceptorIndex[%d], ProviderIndex[%d]",
                    Warehouse.groupsIndex.size(), Warehouse.interceptorsIndex.size(),
                    Warehouse.providersIndex.size()));
            }
        } catch (Exception e) {
            throw new HandlerException(
                TAG + "ARouter init logistics center exception! [" + e.getMessage() + "]");
        }
    }


    /**
     * Build postcard by serviceName
     *
     * 通过一个 服务 name 获取一个 关系类
     *
     * @param serviceName interfaceName
     * @return postcard
     */
    public static Postcard buildProvider(String serviceName) {
        RouteMeta meta = Warehouse.providersIndex.get(serviceName);

        if (null == meta) {
            return null;
        } else {
            return new Postcard(meta.getPath(), meta.getGroup());
        }
    }


    /**
     * Completion the postcard by route metas
     *
     * 往 关系类 中，添加参数。反射实例化所需要的 IProvider，并初始化
     *
     * 0. 根据关系类，拿到 路径，根据 路径 从缓存中查找 路由信息类
     *
     * 如果从缓存中 没有查到 路由信息类
     *
     * 1. 从关系类中，拿到 路由组 name，根据 路由组 name 从缓存中查找 路由组 class
     * 1.1 没有，抛异常
     * 1.2 有，继续
     *
     * 2. 根据  路由组 class 反射构造 路由组 实例
     * 3. 路由组 实例 调用 loadInto，缓存 路由信息类
     * 4. 移除该路由组缓存
     * 5. 没报异常的话，重新加载
     *
     * 重新加载后，应该不会走该系列流程
     * 缓存中能查到对应的 路由信息类
     *
     * ---
     *
     * 如果从缓存中 没有查到 路由信息类
     *
     * 1. 从路由信息类中获取到，跳转目标 class，路由类型，优先级 和 额外数据
     * -  添加到 关系类 中
     *
     *
     * 2.1 从 关系类 中，拿到 Uri
     * 2.2 如果为 null，跳出 2 到 3
     * 2.3 根据 uri，拿到该 uri 下的所有参数键值对（ key，value ）
     * 2.4 从 路由信息类 中，获取所有参数 值类型
     *
     * 2.5 如果 值类型 不为 null
     * 2.5.1 将所有参数键值对，保存到 关系类 的 bundle 内
     * 2.5.2 同时，在 关系类的 extra 上，保存 自动注入的类型（ key = ARouter.AUTO_INJECT ）
     *
     * 2.6 最后，在 关系类的 bundle 上，保存 uri（ key = ARouter.RAW_URI ）
     *
     *
     * 3. 根据 路由信息类 中，路由的类型
     *
     * 3.1.1 如果是 IProvider 类型，从 路由信息类 中拿到 跳转目标 class
     * 3.1.2 根据该 class，从缓存中获取到 IProvider 实例
     * 3.1.3 如果 IProvider 实例 为 null，反射实例化，调用 初始化方法 后放入缓存
     * 3.1.4 在 关系类 中加入该 IProvider
     * 3.1.5 在 关系类 中打开 绿色通道
     *
     * 3.2 如果是 Fragment 类型，在 关系类 中打开 绿色通道
     *
     * @param postcard Incomplete postcard, should completion by this method.
     */
    public synchronized static void completion(Postcard postcard) {
        if (null == postcard) {
            throw new NoRouteFoundException(TAG + "No postcard!");
        }

        // 根据关系类，拿到 路径，根据 路径 从缓存中查找 关系类
        RouteMeta routeMeta = Warehouse.routes.get(postcard.getPath());

        /*
         * 如果从缓存中 没有查到 关系类
         *
         * 1. 从关系类中，拿到 路由组 name，根据 路由组 name 从缓存中查找 路由组 class
         * 1.1 没有，抛异常
         * 1.2 有，继续
         *
         * 2. 根据  路由组 class 反射构造 路由组 实例
         * 3. 路由组 实例 调用 loadInto，缓存 路由信息类
         * 4. 移除该路由组缓存
         * 5. 没报异常的话，重新加载
         *
         * 重新加载后，应该不会走该系列流程
         * 缓存中能查到对应的 路由信息类
         */
        if (null == routeMeta) {    // Maybe its does't exist, or didn't load.
            Class<? extends IRouteGroup> groupMeta = Warehouse.groupsIndex.get(
                postcard.getGroup());  // Load route meta.
            if (null == groupMeta) {
                throw new NoRouteFoundException(
                    TAG + "There is no route match the path [" + postcard.getPath() +
                        "], in group [" + postcard.getGroup() + "]");
            } else {
                // Load route and cache it into memory, then delete from metas.
                try {
                    if (ARouter.debuggable()) {
                        logger.debug(TAG, String.format(Locale.getDefault(),
                            "The group [%s] starts loading, trigger by [%s]", postcard.getGroup(),
                            postcard.getPath()));
                    }

                    IRouteGroup iGroupInstance = groupMeta.getConstructor().newInstance();
                    iGroupInstance.loadInto(Warehouse.routes);
                    Warehouse.groupsIndex.remove(postcard.getGroup());

                    if (ARouter.debuggable()) {
                        logger.debug(TAG, String.format(Locale.getDefault(),
                            "The group [%s] has already been loaded, trigger by [%s]",
                            postcard.getGroup(), postcard.getPath()));
                    }
                } catch (Exception e) {
                    throw new HandlerException(
                        TAG + "Fatal exception when loading group meta. [" + e.getMessage() + "]");
                }

                completion(postcard);   // Reload
            }
        } else {

            /*
             * 如果从缓存中 查到 关系类
             *
             * 1. 从路由信息类中获取到，跳转目标 class，路由类型，优先级 和 额外数据
             * -  添加到 关系类 中
             *
             *
             * 2.1 从 关系类 中，拿到 Uri
             * 2.2 如果为 null，跳出 2 到 3
             * 2.3 根据 uri，拿到该 uri 下的所有参数键值对（ key，value ）
             * 2.4 从 路由信息类 中，获取所有参数 值类型
             *
             * 2.5 如果 值类型 不为 null
             * 2.5.1 将所有参数键值对，保存到 关系类 的 bundle 内
             * 2.5.2 同时，在 关系类的 extra 上，保存 自动注入的类型（ key = ARouter.AUTO_INJECT ）
             *
             * 2.6 最后，在 关系类的 bundle 上，保存 uri（ key = ARouter.RAW_URI ）
             *
             *
             * 3. 根据 路由信息类 中，路由的类型
             *
             * 3.1.1 如果是 IProvider 类型，从 路由信息类 中拿到 跳转目标 class
             * 3.1.2 根据该 class，从缓存中获取到 IProvider 实例
             * 3.1.3 如果 IProvider 实例 为 null，反射实例化，调用 初始化方法 后放入缓存
             * 3.1.4 在 关系类 中加入该 IProvider
             * 3.1.5 在 关系类 中打开 绿色通道
             *
             * 3.2 如果是 Fragment 类型，在 关系类 中打开 绿色通道
             *
             */
            postcard.setDestination(routeMeta.getDestination());
            postcard.setType(routeMeta.getType());
            postcard.setPriority(routeMeta.getPriority());
            postcard.setExtra(routeMeta.getExtra());

            Uri rawUri = postcard.getUri();
            if (null != rawUri) {   // Try to set params into bundle.
                Map<String, String> resultMap = TextUtils.splitQueryParameters(rawUri);
                Map<String, Integer> paramsType = routeMeta.getParamsType();

                if (MapUtils.isNotEmpty(paramsType)) {
                    // Set value by its type, just for params which annotation by @Param
                    for (Map.Entry<String, Integer> params : paramsType.entrySet()) {
                        setValue(postcard,
                            params.getValue(),
                            params.getKey(),
                            resultMap.get(params.getKey()));
                    }

                    // Save params name which need autoinject.
                    postcard.getExtras()
                        .putStringArray(ARouter.AUTO_INJECT,
                            paramsType.keySet().toArray(new String[] {}));
                }

                // Save raw uri
                postcard.withString(ARouter.RAW_URI, rawUri.toString());
            }

            switch (routeMeta.getType()) {
                case PROVIDER:  // if the route is provider, should find its instance
                    // Its provider, so it must be implememt IProvider
                    Class<? extends IProvider> providerMeta = (Class<? extends IProvider>) routeMeta
                        .getDestination();
                    IProvider instance = Warehouse.providers.get(providerMeta);
                    if (null == instance) { // There's no instance of this provider
                        IProvider provider;
                        try {
                            provider = providerMeta.getConstructor().newInstance();
                            provider.init(mContext);
                            Warehouse.providers.put(providerMeta, provider);
                            instance = provider;
                        } catch (Exception e) {
                            throw new HandlerException("Init provider failed! " + e.getMessage());
                        }
                    }
                    postcard.setProvider(instance);
                    postcard.greenChannel();    // Provider should skip all of interceptors
                    break;
                case FRAGMENT:
                    postcard.greenChannel();    // Fragment needn't interceptors
                default:
                    break;
            }
        }
    }


    /**
     * Set value by known type
     *
     * 将所有参数键值对，保存到 关系类 的 bundle 内
     *
     * @param postcard postcard
     * @param typeDef type
     * @param key key
     * @param value value
     */
    private static void setValue(Postcard postcard, Integer typeDef, String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }

        try {
            if (null != typeDef) {
                if (typeDef == TypeKind.BOOLEAN.ordinal()) {
                    postcard.withBoolean(key, Boolean.parseBoolean(value));
                } else if (typeDef == TypeKind.BYTE.ordinal()) {
                    postcard.withByte(key, Byte.valueOf(value));
                } else if (typeDef == TypeKind.SHORT.ordinal()) {
                    postcard.withShort(key, Short.valueOf(value));
                } else if (typeDef == TypeKind.INT.ordinal()) {
                    postcard.withInt(key, Integer.valueOf(value));
                } else if (typeDef == TypeKind.LONG.ordinal()) {
                    postcard.withLong(key, Long.valueOf(value));
                } else if (typeDef == TypeKind.FLOAT.ordinal()) {
                    postcard.withFloat(key, Float.valueOf(value));
                } else if (typeDef == TypeKind.DOUBLE.ordinal()) {
                    postcard.withDouble(key, Double.valueOf(value));
                } else if (typeDef == TypeKind.STRING.ordinal()) {
                    postcard.withString(key, value);
                } else if (typeDef == TypeKind.PARCELABLE.ordinal()) {
                    // TODO : How to description parcelable value with string?
                } else if (typeDef == TypeKind.OBJECT.ordinal()) {
                    postcard.withString(key, value);
                } else {    // Compatible compiler sdk 1.0.3, in that version, the string type = 18
                    postcard.withString(key, value);
                }
            } else {
                postcard.withString(key, value);
            }
        } catch (Throwable ex) {
            logger.warning(Consts.TAG, "LogisticsCenter setValue failed! " + ex.getMessage());
        }
    }


    /**
     * Suspend bussiness, clear cache.
     *
     * 清空所有路由缓存
     */
    public static void suspend() {
        Warehouse.clear();
    }

}