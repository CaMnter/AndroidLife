/*
 * Copyright (C) 2012 Square, Inc.
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.camnter.otto;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Helper methods for finding methods annotated with {@link Produce} and {@link Subscribe}.
 *
 * @author Cliff Biffle
 * @author Louis Wasserman
 * @author Jake Wharton
 */
final class AnnotatedHandlerFinder {

    /**
     * Cache event bus producer methods for each class.
     * 缓存 @Produce 方法
     */
    private static final ConcurrentMap<Class<?>, Map<Class<?>, Method>> PRODUCERS_CACHE =
            new ConcurrentHashMap<Class<?>, Map<Class<?>, Method>>();

    /**
     * Cache event bus subscriber methods for each class.
     * 缓存 @Subscribe 方法
     */
    private static final ConcurrentMap<Class<?>, Map<Class<?>, Set<Method>>> SUBSCRIBERS_CACHE =
            new ConcurrentHashMap<Class<?>, Map<Class<?>, Set<Method>>>();

    private static void loadAnnotatedProducerMethods(Class<?> listenerClass,
                                                     Map<Class<?>, Method> producerMethods) {
        Map<Class<?>, Set<Method>> subscriberMethods = new HashMap<Class<?>, Set<Method>>();
        loadAnnotatedMethods(listenerClass, producerMethods, subscriberMethods);
    }

    private static void loadAnnotatedSubscriberMethods(Class<?> listenerClass,
                                                       Map<Class<?>, Set<Method>> subscriberMethods) {
        Map<Class<?>, Method> producerMethods = new HashMap<Class<?>, Method>();
        loadAnnotatedMethods(listenerClass, producerMethods, subscriberMethods);
    }

    /**
     * Load all methods annotated with {@link Produce} or {@link Subscribe} into their respective caches for the
     * specified class.
     * 读取目标类的 所有 @Produce 和 @Subscribe方法
     */
    private static void loadAnnotatedMethods(Class<?> listenerClass,
                                             Map<Class<?>, Method> producerMethods, Map<Class<?>, Set<Method>> subscriberMethods) {
        for (Method method : listenerClass.getDeclaredMethods()) {
            // The compiler sometimes creates synthetic bridge methods as part of the
            // type erasure process. As of JDK8 these methods now include the same
            // annotations as the original declarations. They should be ignored for
            // subscribe/produce.

            /**
             * 判断方式是否桥接，跟泛型有关
             */
            if (method.isBridge()) {
                continue;
            }

            /*************
             * Subscribe *
             *************/

            /**
             * 如果方法的注解是 @Subscribe
             */
            if (method.isAnnotationPresent(Subscribe.class)) {

                /**
                 * 拿到方法的参数
                 */
                Class<?>[] parameterTypes = method.getParameterTypes();

                /**
                 * 方法参数是否为 1 ，为 1 抛出异常
                 */
                if (parameterTypes.length != 1) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation but requires "
                            + parameterTypes.length + " arguments.  Methods must require a single argument.");
                }

                /**
                 * 参数是否是接口 ，是接口抛出异常
                 */
                Class<?> eventType = parameterTypes[0];
                if (eventType.isInterface()) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation on " + eventType
                            + " which is an interface.  Subscription must be on a concrete class type.");
                }

                /**
                 * 方法是否public ， 不是public抛出异常
                 */
                if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation on " + eventType
                            + " but is not 'public'.");
                }

                /**
                 * 通过类去获取原来该类所有的 @Subscribe 注解的方法
                 */
                Set<Method> methods = subscriberMethods.get(eventType);

                /**
                 * 如果原本没有一个方法Set，创建一个新的Set再，直接add
                 * 有Set，则直接add
                 */
                if (methods == null) {
                    methods = new HashSet<Method>();
                    subscriberMethods.put(eventType, methods);
                }
                methods.add(method);
            } else if (method.isAnnotationPresent(Produce.class)) {

                /***********
                 * Produce *
                 ***********/

                /**
                 * 拿到方法的参数
                 */
                Class<?>[] parameterTypes = method.getParameterTypes();

                /**
                 * 方法参数是否为 1 ，为 1 抛出异常
                 */
                if (parameterTypes.length != 0) {
                    throw new IllegalArgumentException("Method " + method + "has @Produce annotation but requires "
                            + parameterTypes.length + " arguments.  Methods must require zero arguments.");
                }

                /**
                 * 方法返回值是否为 Void 类 ，为 Void 类 抛出异常
                 */
                if (method.getReturnType() == Void.class) {
                    throw new IllegalArgumentException("Method " + method
                            + " has a return type of void.  Must declare a non-void type.");
                }

                /**
                 * 拿到方法返回类型
                 */
                Class<?> eventType = method.getReturnType();

                /**
                 *  返回类型是否是一个接口 ， 是接口则抛出异常
                 */
                if (eventType.isInterface()) {
                    throw new IllegalArgumentException("Method " + method + " has @Produce annotation on " + eventType
                            + " which is an interface.  Producers must return a concrete class type.");
                }

                /**
                 *  返回类型是否 为 Void 类型
                 */
                if (eventType.equals(Void.TYPE)) {
                    throw new IllegalArgumentException("Method " + method + " has @Produce annotation but has no return type.");
                }

                /**
                 *  方法是否public ， 不是public抛出异常
                 */
                if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                    throw new IllegalArgumentException("Method " + method + " has @Produce annotation on " + eventType
                            + " but is not 'public'.");
                }

                /**
                 *  如果已经保存 相同 返回类型  @Produce 方法
                 *  则抛出异常
                 */
                if (producerMethods.containsKey(eventType)) {
                    throw new IllegalArgumentException("Producer for type " + eventType + " has already been registered.");
                }

                /**
                 * 走到这，通过上面的条件
                 * 则add
                 */
                producerMethods.put(eventType, method);
            }
        }
        /**
         * 缓存所有 @Produce
         * 缓存所有 @Subscribe
         */
        PRODUCERS_CACHE.put(listenerClass, producerMethods);
        SUBSCRIBERS_CACHE.put(listenerClass, subscriberMethods);
    }

    /**
     * This implementation finds all methods marked with a {@link Produce} annotation.
     * 寻找所有 @Produce 方法，封装成 EventProducer（ @Produce方法 和 一些数据  ）
     */
    static Map<Class<?>, EventProducer> findAllProducers(Object listener) {

        /**
         * 拿到目标类
         */
        final Class<?> listenerClass = listener.getClass();
        Map<Class<?>, EventProducer> handlersInMethod = new HashMap<Class<?>, EventProducer>();

        /**
         * 拿到 @Produce 缓存Map
         */
        Map<Class<?>, Method> methods = PRODUCERS_CACHE.get(listenerClass);

        /**
         * 开始添加那些没被缓存的 @Produce 方法
         */
        if (null == methods) {
            methods = new HashMap<Class<?>, Method>();
            loadAnnotatedProducerMethods(listenerClass, methods);
        }

        /**
         * 缓存 @Produce Map 有内容
         *
         * 逐个将对应的类和Produce方法放入 EventProducer 和 handlersInMethod内
         */
        if (!methods.isEmpty()) {
            for (Map.Entry<Class<?>, Method> e : methods.entrySet()) {
                EventProducer producer = new EventProducer(listener, e.getValue());
                handlersInMethod.put(e.getKey(), producer);
            }
        }

        return handlersInMethod;
    }

    /**
     * This implementation finds all methods marked with a {@link Subscribe} annotation.
     * 寻找所有 @Subscribe 方法，封装成 EventProducer（ @Subscribe方法 和 一些数据  ）
     */
    static Map<Class<?>, Set<EventHandler>> findAllSubscribers(Object listener) {

        /**
         * 拿到目标类
         */
        Class<?> listenerClass = listener.getClass();
        Map<Class<?>, Set<EventHandler>> handlersInMethod = new HashMap<Class<?>, Set<EventHandler>>();

        /**
         * 拿到 @Subscribe 缓存Map
         */
        Map<Class<?>, Set<Method>> methods = SUBSCRIBERS_CACHE.get(listenerClass);

        /**
         * 开始添加那些没被缓存的 @Subscribe 方法
         */
        if (null == methods) {
            methods = new HashMap<Class<?>, Set<Method>>();
            loadAnnotatedSubscriberMethods(listenerClass, methods);
        }

        /**
         * 缓存 @Subscribe Map 有内容
         *
         * 创建一个EventHandler集合，缓存类对应的 @Subscribe 方法
         * 从这个可以看得出 一个类能有 N个 @Subscribe 方法
         */
        if (!methods.isEmpty()) {
            for (Map.Entry<Class<?>, Set<Method>> e : methods.entrySet()) {
                Set<EventHandler> handlers = new HashSet<EventHandler>();
                for (Method m : e.getValue()) {
                    handlers.add(new EventHandler(listener, m));
                }
                handlersInMethod.put(e.getKey(), handlers);
            }
        }

        return handlersInMethod;
    }

    private AnnotatedHandlerFinder() {
        // No instances.
    }

}
