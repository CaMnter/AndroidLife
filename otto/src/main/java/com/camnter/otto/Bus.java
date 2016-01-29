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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Dispatches events to listeners, and provides ways for listeners to register themselves. <p/>
 * <p>The Bus allows publish-subscribe-style communication between components without requiring the
 * components to explicitly register with one another (and thus be aware of each other).  It is
 * designed exclusively to replace traditional Android in-process event distribution using explicit
 * registration or listeners. It is <em>not</em> a general-purpose publish-subscribe system, nor is
 * it intended for interprocess communication. <p/> <h2>Receiving Events</h2> To receive events, an
 * object should: <ol> <li>Expose a public method, known as the <i>event handler</i>, which accepts
 * a single argument of the type of event desired;</li> <li>Mark it with a {@link
 * com.squareup.otto.Subscribe} annotation;</li> <li>Pass itself to an Bus instance's {@link
 * #register(Object)} method. </li> </ol> <p/> <h2>Posting Events</h2> To post an event, simply
 * provide the event object to the {@link #post(Object)} method.  The Bus instance will determine
 * the type of event and route it to all registered listeners. <p/> <p>Events are routed based on
 * their type &mdash; an event will be delivered to any handler for any type to which the event is
 * <em>assignable.</em>  This includes implemented interfaces, all superclasses, and all interfaces
 * implemented by superclasses. <p/> <p>When {@code post} is called, all registered handlers for an
 * event are run in sequence, so handlers should be reasonably quick.  If an event may trigger an
 * extended process (such as a database load), spawn a thread or queue it for later. <p/>
 * <h2>Handler Methods</h2> Event handler methods must accept only one argument: the event. <p/>
 * <p>Handlers should not, in general, throw.  If they do, the Bus will wrap the exception and
 * re-throw it. <p/> <p>The Bus by default enforces that all interactions occur on the main thread.
 * You can provide an alternate enforcement by passing a {@link ThreadEnforcer} to the constructor.
 * <p/> <h2>Producer Methods</h2> Producer methods should accept no arguments and return their event
 * type. When a subscriber is registered for a type that a producer is also already registered for,
 * the subscriber will be called with the return value from the producer. <p/> <h2>Dead Events</h2>
 * If an event is posted, but no registered handlers can accept it, it is considered "dead."  To
 * give the system a second chance to handle dead events, they are wrapped in an instance of {@link
 * com.squareup.otto.DeadEvent} and reposted. <p/> <p>This class is safe for concurrent use.
 *
 * @author Cliff Biffle
 * @author Jake Wharton
 */
public class Bus {
    public static final String DEFAULT_IDENTIFIER = "default";

    /**
     * All registered event handlers, indexed by event type.
     * 缓存 listener 及其对应的 EventHandler
     * 可以看出一个 listener 可以对应多个 EventHandler
     */
    private final ConcurrentMap<Class<?>, Set<EventHandler>> handlersByType =
            new ConcurrentHashMap<Class<?>, Set<EventHandler>>();

    /**
     * All registered event producers, index by event type.
     * 缓存 listener 及其对应的 EventProducer
     * 可以看出一个 listener 对应一个 EventProducer
     */
    private final ConcurrentMap<Class<?>, EventProducer> producersByType =
            new ConcurrentHashMap<Class<?>, EventProducer>();

    /**
     * Identifier used to differentiate the event bus instance.
     * 可是实现多个Bus，就相当于多个事件总线，实例化的时候可以提供一个String类型的id
     * 作为每一个Bus的唯一标识。
     */
    private final String identifier;

    /**
     * Thread enforcer for register, unregister, and posting events.
     * ThreadEnforcer去执行 注册、注销和发送事件。
     */
    private final ThreadEnforcer enforcer;

    /**
     * Used to find handler methods in register and unregister.
     * 在注销和注册的时候，HandlerFinder去找一遍全部的EventHandler（ @Subscribe ）
     */
    private final HandlerFinder handlerFinder;

    /**
     * Queues of events for the current thread to dispatch.
     * ThreadLocal存放一个ConcurrentLinkedQueue<EventWithHandler>队列
     */
    private final ThreadLocal<ConcurrentLinkedQueue<EventWithHandler>> eventsToDispatch =
            new ThreadLocal<ConcurrentLinkedQueue<EventWithHandler>>() {
                @Override
                protected ConcurrentLinkedQueue<EventWithHandler> initialValue() {
                    return new ConcurrentLinkedQueue<EventWithHandler>();
                }
            };

    /**
     * True if the current thread is currently dispatching an event.
     * ThreadLocal存放isDispatching标识
     */
    private final ThreadLocal<Boolean> isDispatching = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    /**
     * Creates a new Bus named "default" that enforces actions on the main thread.
     * 默认构造Bus
     * 唯一标识为 DEFAULT_IDENTIFIER
     */
    public Bus() {
        this(DEFAULT_IDENTIFIER);
    }

    /**
     * Creates a new Bus with the given {@code identifier} that enforces actions on the main thread.
     * 默认构造Bus
     * 唯一标识为 自定义
     * ThreadEnforcer为 主线程
     *
     * @param identifier a brief name for this bus, for debugging purposes.  Should be a valid Java
     *                   identifier.
     */
    public Bus(String identifier) {
        this(ThreadEnforcer.MAIN, identifier);
    }

    /**
     * Creates a new Bus named "default" with the given {@code enforcer} for actions.
     * 默认构造Bus
     * 唯一标识为 DEFAULT_IDENTIFIER
     * ThreadEnforcer为 自定义
     *
     * @param enforcer Thread enforcer for register, unregister, and post actions.
     */
    public Bus(ThreadEnforcer enforcer) {
        this(enforcer, DEFAULT_IDENTIFIER);
    }

    /**
     * Creates a new Bus with the given {@code enforcer} for actions and the given {@code
     * identifier}.
     * 默认构造Bus
     * 唯一标识为 自定义
     * ThreadEnforcer为 自定义
     * HandlerFinder为 HandlerFinder.ANNOTATED
     *
     * @param enforcer   Thread enforcer for register, unregister, and post actions.
     * @param identifier A brief name for this bus, for debugging purposes.  Should be a valid Java
     *                   identifier.
     */
    public Bus(ThreadEnforcer enforcer, String identifier) {
        this(enforcer, identifier, HandlerFinder.ANNOTATED);
    }

    /**
     * Test constructor which allows replacing the default {@code HandlerFinder}.
     * 默认构造Bus
     * 唯一标识为 自定义
     * ThreadEnforcer为 自定义
     * HandlerFinder为 自定义
     *
     * @param enforcer      Thread enforcer for register, unregister, and post actions.
     * @param identifier    A brief name for this bus, for debugging purposes.  Should be a valid Java
     *                      identifier.
     * @param handlerFinder Used to discover event handlers and producers when
     *                      registering/unregistering an object.
     */
    Bus(ThreadEnforcer enforcer, String identifier, HandlerFinder handlerFinder) {
        this.enforcer = enforcer;
        this.identifier = identifier;
        this.handlerFinder = handlerFinder;
    }

    @Override
    public String toString() {
        return "[Bus \"" + identifier + "\"]";
    }

    /**
     * Registers all handler methods on {@code object} to receive events and producer methods to
     * provide events. <p/> If any subscribers are registering for types which already have a producer
     * they will be called immediately with the result of calling that producer. <p/> If any producers
     * are registering for types which already have subscribers, each subscriber will be called with
     * the value from the result of calling the producer.
     *
     * @param object object whose handler methods should be registered.
     * @throws NullPointerException if the object is null.
     */
    public void register(Object object) {
        if (object == null) {
            throw new NullPointerException("Object to register must not be null.");
        }

        /*********************
         * 处理 @Produce 逻辑 *
         *********************/

        /**
         * 一般用户不设置的话
         * 这里的ThreadEnforcer为ThreadEnforcer.MAIN
         */
        enforcer.enforce(this);

        /**
         * 通过HandlerFinder找到所有
         * object里所有 @Produce 方法
         * 找回来的封装成EventProducer
         * 并且最后返回Map<Class<?>, EventProducer>
         */
        Map<Class<?>, EventProducer> foundProducers = handlerFinder.findAllProducers(object);
        for (Class<?> type : foundProducers.keySet()) {

            /**
             * 逐个拿到EventProducer
             */
            final EventProducer producer = foundProducers.get(type);
            /**
             * ConcurrentMap putIfAbsent 安全的put
             * 防止并发
             * 查看缓存 ConcurrentMap<Class<?>, EventProducer> 有木有
             */
            EventProducer previousProducer = producersByType.putIfAbsent(type, producer);
            //checking if the previous producer existed

            /**
             * 有缓存 先 “炸” 一下
             */
            if (previousProducer != null) {
                throw new IllegalArgumentException("Producer method for type " + type
                        + " found on type " + producer.target.getClass()
                        + ", but already registered by type " + previousProducer.target.getClass() + ".");
            }

            /**
             * 逐个拿到全部缓存EventHandler （ @Subscribe方法封装类 ）
             */
            Set<EventHandler> handlers = handlersByType.get(type);
            if (handlers != null && !handlers.isEmpty()) {
                for (EventHandler handler : handlers) {
                    /**
                     * 逐个去调用@Subscribe 和 @Produce 方法
                     */
                    dispatchProducerResultToHandler(handler, producer);
                }
            }
        }

        /*********************
         * 处理 @Produce 逻辑 *
         *********************/


        /***********************
         * 处理 @SubScribe 逻辑 *
         ***********************/

        /**
         * 通过HandlerFinder找到所有
         * object里所有 @SubScribe 方法
         * 找回来的封装成EventHandler
         * Map<Class<?>, Set<EventHandler>>
         */
        Map<Class<?>, Set<EventHandler>> foundHandlersMap = handlerFinder.findAllSubscribers(object);

        /**
         * 逐个拿到EventHandler
         */
        for (Class<?> type : foundHandlersMap.keySet()) {

            /**
             * 拿到缓存 EventHandler 集合
             */
            Set<EventHandler> handlers = handlersByType.get(type);
            if (handlers == null) {
                //concurrent put if absent
                Set<EventHandler> handlersCreation = new CopyOnWriteArraySet<EventHandler>();

                /**
                 * 往缓存 EventHandler Map 里放入一份 type 对应的 EventHandle 集合
                 */
                handlers = handlersByType.putIfAbsent(type, handlersCreation);
                if (handlers == null) {
                    handlers = handlersCreation;
                }
            }
            final Set<EventHandler> foundHandlers = foundHandlersMap.get(type);
            if (!handlers.addAll(foundHandlers)) {
                throw new IllegalArgumentException("Object already registered.");
            }
        }

        /**
         * 遍历所有找到 EventHandler （ @Subscribe方法 ）
         */
        for (Map.Entry<Class<?>, Set<EventHandler>> entry : foundHandlersMap.entrySet()) {
            Class<?> type = entry.getKey();
            /**
             * 再那一次 EventProducer 缓存
             * 如果object 里 存在 @Producer 方法
             * 才循环 EventHandler 的逻辑里去 调用
             * dispatchProducerResultToHandler方法
             */
            EventProducer producer = producersByType.get(type);
            if (producer != null && producer.isValid()) {
                Set<EventHandler> foundHandlers = entry.getValue();
                for (EventHandler foundHandler : foundHandlers) {
                    if (!producer.isValid()) {
                        break;
                    }
                    if (foundHandler.isValid()) {
                        dispatchProducerResultToHandler(foundHandler, producer);
                    }
                }
            }
        }

        /***********************
         * 处理 @SubScribe 逻辑 *
         ***********************/
    }

    /**
     * 完成了所谓的 提供 @Produce 被自身 @Subscribe 消费的流程
     *
     * @param handler  handler
     * @param producer producer
     */
    private void dispatchProducerResultToHandler(EventHandler handler, EventProducer producer) {
        /**
         * 在这里进行一些数据的判断
         */
        Object event = null;
        try {
            /**
             * 然后调用EventProducer里的 produceEvent 方法
             * produceEvent里又反射
             * 调用注册object里的 @Produce 方法
             */
            event = producer.produceEvent();
        } catch (InvocationTargetException e) {
            throwRuntimeException("Producer " + producer + " threw an exception.", e);
        }
        if (event == null) {
            return;
        }
        /**
         * 通过  @Produce 的方法提供的事件不为null
         * 如果 event 不为空
         * 最后进到dispatch里调用
         * EventHandler里的handleEvent方法
         * 调用注册object里的 的 @Subscribe 方法
         * 完成了所谓的 提供 @Produce 被自身 @Subscribe 消费的流程
         */
        dispatch(event, handler);
    }

    /**
     * Unregisters all producer and handler methods on a registered {@code object}.
     *
     * @param object object whose producer and handler methods should be unregistered.
     * @throws IllegalArgumentException if the object was not previously registered.
     * @throws NullPointerException     if the object is null.
     */
    public void unregister(Object object) {
        if (object == null) {
            throw new NullPointerException("Object to unregister must not be null.");
        }

        /**
         * 一般用户不设置的话
         * 这里的ThreadEnforcer为ThreadEnforcer.MAIN
         */
        enforcer.enforce(this);

        /**
         * 通过HandlerFinder找到所有
         * object里所有 @Produce 方法
         * 找回来的封装成EventProducer
         * 并且最后返回Map<Class<?>, EventProducer>
         */
        Map<Class<?>, EventProducer> producersInListener = handlerFinder.findAllProducers(object);
        for (Map.Entry<Class<?>, EventProducer> entry : producersInListener.entrySet()) {
            final Class<?> key = entry.getKey();

            /**
             * 拿到对应的 EventProducer
             * 这里只拿一个 又表明了：
             * 一个 object 只存在 一个 EventProducer
             * producer 表示 已缓存的 该 object 的 所有 EventProducer
             * value 表示 通过Finder 找到的 所有 EventHandler
             */
            EventProducer producer = getProducerForEventType(key);
            EventProducer value = entry.getValue();

            if (value == null || !value.equals(producer)) {
                throw new IllegalArgumentException(
                        "Missing event producer for an annotated method. Is " + object.getClass()
                                + " registered?");
            }
            /**
             * 从 EventProducer 缓存Map里移除
             * 并调用 EventProducer.invalidate()方法
             * 设置该 EventProducer 不合法
             */
            producersByType.remove(key).invalidate();
        }

        Map<Class<?>, Set<EventHandler>> handlersInListener = handlerFinder.findAllSubscribers(object);
        for (Map.Entry<Class<?>, Set<EventHandler>> entry : handlersInListener.entrySet()) {

            /**
             * 拿到对应的 Set<EventHandler>
             * 又表明了：
             * 一个 object 只存在 N个 EventHandler
             * currentHandlers 表示 已缓存的 该 object 的 所有 EventHandler
             * eventMethodsInListener 表示 通过Finder 找到的 所有 EventHandler
             */
            Set<EventHandler> currentHandlers = getHandlersForEventType(entry.getKey());
            Collection<EventHandler> eventMethodsInListener = entry.getValue();

            if (currentHandlers == null || !currentHandlers.containsAll(eventMethodsInListener)) {
                throw new IllegalArgumentException(
                        "Missing event handler for an annotated method. Is " + object.getClass()
                                + " registered?");
            }

            /**
             * 循环所有 缓存的 EventHandler
             * 如果 缓存的 EventHandler 又存在 Finder先查的  EventHandler 里
             * 标记为 不合法
             */
            for (EventHandler handler : currentHandlers) {
                if (eventMethodsInListener.contains(handler)) {
                    handler.invalidate();
                }
            }

            /**
             * 该 object 所有缓存的 EventHandler 集合中 剔除
             * Finder现查的所有 EventHandler
             */
            currentHandlers.removeAll(eventMethodsInListener);
        }
    }

    /**
     * Posts an event to all registered handlers.  This method will return successfully after the
     * event has been posted to all handlers, and regardless of any exceptions thrown by handlers.
     * <p/> <p>If no handlers have been subscribed for {@code event}'s class, and {@code event} is not
     * already a {@link DeadEvent}, it will be wrapped in a DeadEvent and reposted.
     *
     * @param event event to post.
     * @throws NullPointerException if the event is null.
     */
    public void post(Object event) {
        if (event == null) {
            throw new NullPointerException("Event to post must not be null.");
        }

        /**
         * 一般用户不设置的话
         * 这里的ThreadEnforcer为ThreadEnforcer.MAIN
         */
        enforcer.enforce(this);

        /**
         * 拿到 该事件 + 该事件所有父类 的Set集合
         */
        Set<Class<?>> dispatchTypes = flattenHierarchy(event.getClass());

        boolean dispatched = false;
        for (Class<?> eventType : dispatchTypes) {

            /**
             * 拿到 该 object所有缓存 EventHandler
             */
            Set<EventHandler> wrappers = getHandlersForEventType(eventType);

            /**
             * 开始进入 遍历缓存的 EventHandler
             * 并处理事件
             * 进入 enqueueEvent 逻辑
             */
            if (wrappers != null && !wrappers.isEmpty()) {
                dispatched = true;
                for (EventHandler wrapper : wrappers) {
                    /**
                     * 事件 + EventHandler 包装成 EventWithHandler
                     * 入队
                     */
                    enqueueEvent(event, wrapper);
                }
            }
        }

        /**
         * 根据上面的循环可知道
         * 如果 object 存在 一个 EventHandler
         * 并且post的 事件不是 DeadEvent
         * 就会执行一次 post(new DeadEvent(this, event))
         */
        if (!dispatched && !(event instanceof DeadEvent)) {
            post(new DeadEvent(this, event));
        }

        dispatchQueuedEvents();
    }

    /**
     * Queue the {@code event} for dispatch during {@link #dispatchQueuedEvents()}. Events are queued
     * in-order of occurrence so they can be dispatched in the same order.
     * 封装成 EventWithHandler
     * 入队
     */
    protected void enqueueEvent(Object event, EventHandler handler) {
        eventsToDispatch.get().offer(new EventWithHandler(event, handler));
    }

    /**
     * Drain the queue of events to be dispatched. As the queue is being drained, new events may be
     * posted to the end of the queue.
     * 开始处理事件队列
     */
    protected void dispatchQueuedEvents() {
        // don't dispatch if we're already dispatching, that would allow reentrancy and out-of-order events. Instead, leave
        // the events to be dispatched after the in-progress dispatch is complete.
        if (isDispatching.get()) {
            return;
        }

        isDispatching.set(true);
        try {
            while (true) {
                /**
                 * 拿到队列，取出一个 EventWithHandler
                 */
                EventWithHandler eventWithHandler = eventsToDispatch.get().poll();
                if (eventWithHandler == null) {
                    break;
                }

                /**
                 * 处理 @Subscribe 逻辑
                 */
                if (eventWithHandler.handler.isValid()) {
                    dispatch(eventWithHandler.event, eventWithHandler.handler);
                }
            }
        } finally {
            isDispatching.set(false);
        }
    }

    /**
     * Dispatches {@code event} to the handler in {@code wrapper}.  This method is an appropriate
     * override point for subclasses that wish to make event delivery asynchronous.
     * <p/>
     * 调用 EventHandler.handleEvent方法
     * 将事件传入，进而调用 @Subscribe 方法
     *
     * @param event   event to dispatch.
     * @param wrapper wrapper that will call the handler.
     */
    protected void dispatch(Object event, EventHandler wrapper) {
        /**
         * 调用 EventHandler.handleEvent方法
         * 将事件传入，进而调用 @Subscribe 方法
         */
        try {
            wrapper.handleEvent(event);
        } catch (InvocationTargetException e) {
            throwRuntimeException(
                    "Could not dispatch event: " + event.getClass() + " to handler " + wrapper, e);
        }
    }

    /**
     * Retrieves the currently registered producer for {@code type}.  If no producer is currently
     * registered for {@code type}, this method will return {@code null}.
     * 根据object的class类型 拿到object缓存的 一个EventProducer
     *
     * @param type type of producer to retrieve.
     * @return currently registered producer, or {@code null}.
     */
    EventProducer getProducerForEventType(Class<?> type) {
        return producersByType.get(type);
    }

    /**
     * Retrieves a mutable set of the currently registered handlers for {@code type}.  If no handlers
     * are currently registered for {@code type}, this method may either return {@code null} or an
     * empty set.
     * 根据object的class类型 拿到object缓存的 一个 EventHandler 集合
     *
     * @param type type of handlers to retrieve.
     * @return currently registered handlers, or {@code null}.
     */
    Set<EventHandler> getHandlersForEventType(Class<?> type) {
        return handlersByType.get(type);
    }

    /**
     * Flattens a class's type hierarchy into a set of Class objects.  The set will include all
     * superclasses (transitively), and all interfaces implemented by these superclasses.
     *
     * @param concreteClass class whose type hierarchy will be retrieved.
     * @return {@code concreteClass}'s complete type hierarchy, flattened and uniqued.
     */
    Set<Class<?>> flattenHierarchy(Class<?> concreteClass) {
        Set<Class<?>> classes = flattenHierarchyCache.get(concreteClass);
        if (classes == null) {
            /**
             * 寻找改 事件 的所有父类 包括 自己
             * 返回一个Set 集合
             */
            Set<Class<?>> classesCreation = getClassesFor(concreteClass);
            /**
             * flattenHierarchyCache 里放入 该事件自身为key
             * 上面的Set为 value
             */
            classes = flattenHierarchyCache.putIfAbsent(concreteClass, classesCreation);
            if (classes == null) {
                classes = classesCreation;
            }
        }

        return classes;
    }

    /**
     * 寻找一个类所有父类 包括自己 存为一个 Set 集合
     *
     * @param concreteClass concreteClass
     * @return Set<Class<?>>
     */
    private Set<Class<?>> getClassesFor(Class<?> concreteClass) {
        List<Class<?>> parents = new LinkedList<Class<?>>();
        Set<Class<?>> classes = new HashSet<Class<?>>();

        parents.add(concreteClass);

        while (!parents.isEmpty()) {
            Class<?> clazz = parents.remove(0);
            classes.add(clazz);

            Class<?> parent = clazz.getSuperclass();
            if (parent != null) {
                parents.add(parent);
            }
        }
        return classes;
    }

    /**
     * Throw a {@link RuntimeException} with given message and cause lifted from an {@link
     * InvocationTargetException}. If the specified {@link InvocationTargetException} does not have a
     * cause, neither will the {@link RuntimeException}.
     */
    private static void throwRuntimeException(String msg, InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            throw new RuntimeException(msg + ": " + cause.getMessage(), cause);
        } else {
            throw new RuntimeException(msg + ": " + e.getMessage(), e);
        }
    }

    private final ConcurrentMap<Class<?>, Set<Class<?>>> flattenHierarchyCache =
            new ConcurrentHashMap<Class<?>, Set<Class<?>>>();

    /**
     * Simple struct representing an event and its handler.
     */
    static class EventWithHandler {
        final Object event;
        final EventHandler handler;

        public EventWithHandler(Object event, EventHandler handler) {
            this.event = event;
            this.handler = handler;
        }
    }
}
