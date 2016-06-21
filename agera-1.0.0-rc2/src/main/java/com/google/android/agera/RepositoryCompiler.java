/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.agera;

import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import static com.google.android.agera.CompiledRepository.addBindWith;
import static com.google.android.agera.CompiledRepository.addCheck;
import static com.google.android.agera.CompiledRepository.addEnd;
import static com.google.android.agera.CompiledRepository.addFilterSuccess;
import static com.google.android.agera.CompiledRepository.addGetFrom;
import static com.google.android.agera.CompiledRepository.addGoLazy;
import static com.google.android.agera.CompiledRepository.addGoTo;
import static com.google.android.agera.CompiledRepository.addMergeIn;
import static com.google.android.agera.CompiledRepository.addSendTo;
import static com.google.android.agera.CompiledRepository.addTransform;
import static com.google.android.agera.CompiledRepository.compiledRepository;
import static com.google.android.agera.Functions.identityFunction;
import static com.google.android.agera.Mergers.objectsUnequal;
import static com.google.android.agera.Preconditions.checkNotNull;
import static com.google.android.agera.Preconditions.checkState;

/**
 * Repository 编译器
 */
@SuppressWarnings({ "unchecked, rawtypes" })
final class RepositoryCompiler implements
    RepositoryCompilerStates.RFrequency,
    RepositoryCompilerStates.RFlow,
    RepositoryCompilerStates.RTermination,
    RepositoryCompilerStates.RConfig {

    /**
     * 使用 ThreadLocal 缓存 RepositoryCompiler
     * 规定了 每个线程 都独有自己的一个 RepositoryCompiler
     */
    private static final ThreadLocal<RepositoryCompiler> compilers = new ThreadLocal<>();


    /**
     * 通过一个初始值，开始编译一个 仓库
     * 并进入到 REventSource 事件源状态
     *
     * @param initialValue 初始值
     * @param <TVal> 初始值类型
     * @return REventSource 事件源状态
     */
    @NonNull
    static <TVal> RepositoryCompilerStates.REventSource<TVal, TVal> repositoryWithInitialValue(
        @NonNull final TVal initialValue) {
        // 检查是否在 主线程
        checkNotNull(Looper.myLooper());
        // 查看是否已经缓存了 RepositoryCompiler
        RepositoryCompiler compiler = compilers.get();
        if (compiler == null) {
            // 没有获取到缓存，直接 new 一个
            compiler = new RepositoryCompiler();
        } else {
            // Remove compiler from the ThreadLocal to prevent reuse in the middle of a compilation.
            // recycle(), called by compile(), will return the compiler here. ThreadLocal.set(null) keeps
            // the entry (with a null value) whereas remove() removes the entry; because we expect the
            // return of the compiler, don't use the heavier remove().
            // 获取到缓存，清空缓存
            compilers.set(null);
        }
        /*
         *  start(...) 返回的是 RepositoryCompiler
         *  RepositoryCompiler 实现了 RFrequency 状态
         *  并且 RFrequency 状态 实现了 REventSource 状态
         */
        return compiler.start(initialValue);
    }


    /**
     * 缓存回收 RepositoryCompiler
     * 将其放入到 ThreadLocal 中
     *
     * @param compiler RepositoryCompiler
     */
    private static void recycle(@NonNull final RepositoryCompiler compiler) {
        compilers.set(compiler);
    }


    /**
     * 编译器的详细 编译状态
     * 1. NOTHING：无状态
     * 2. FIRST_EVENT_SOURCE：第一事件源状态
     * 3. FREQUENCY_OR_MORE_EVENT_SOURCE：频率状态 或者 更多事件源状态
     * 4. FLOW：流状态
     * 5. TERMINATE_THEN_FLOW：RFlow -> RTermination 的终止状态
     * 6. TERMINATE_THEN_END：RSyncFlow -> RTermination 的终止状态
     * 7. CONFIG：配置状态
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ NOTHING, FIRST_EVENT_SOURCE, FREQUENCY_OR_MORE_EVENT_SOURCE, FLOW,
                TERMINATE_THEN_FLOW, TERMINATE_THEN_END, CONFIG })
    private @interface Expect {}


    private static final int NOTHING = 0;
    private static final int FIRST_EVENT_SOURCE = 1;
    private static final int FREQUENCY_OR_MORE_EVENT_SOURCE = 2;
    private static final int FLOW = 3;
    private static final int TERMINATE_THEN_FLOW = 4;
    private static final int TERMINATE_THEN_END = 5;
    private static final int CONFIG = 6;

    // 保存 初始值
    private Object initialValue;
    // 保存 所有 观察者
    private final ArrayList<Observable> eventSources = new ArrayList<>();
    // 保存 执行频率
    private int frequency;
    // 保存 所有指令
    private final ArrayList<Object> directives = new ArrayList<>();
    // 2x fields below: store caseExtractor and casePredicate for check(caseExtractor, casePredicate)
    // for use in terminate(); if null then terminate() is terminating an attempt directive.

    /*
     * 保存 check(...) 操作符 用的
     * Function
     */
    private Function caseExtractor;
    /*
     * 保存 check(...) 操作符 用的
     * Predicate
     */
    private Predicate casePredicate;
    // 记录 是否 懒加载
    private boolean goLazyUsed;
    // 用于判断 相等 的 合并者
    private Merger notifyChecker = objectsUnequal();

    // 失效配置
    @RepositoryConfig
    private int deactivationConfig;

    // 并发更新配置
    @RepositoryConfig
    private int concurrentUpdateConfig;

    // 当前的编译状态
    @Expect
    private int expect;


    /**
     * 屏蔽 默认 构造方法
     */
    private RepositoryCompiler() {}


    /**
     * 编译器 启动
     *
     * @param initialValue 初始值
     * @return RepositoryCompiler，因为 RepositoryCompiler 实现了 所有的 仓库编译状态
     */
    @NonNull
    private RepositoryCompiler start(@NonNull final Object initialValue) {
        // 检查 当前编译状态 是否是 NOTHING：无状态
        checkExpect(NOTHING);
        // 设置 当前编译状态 为 FIRST_EVENT_SOURCE：第一事件源状态
        expect = FIRST_EVENT_SOURCE;
        // 保存 初始值
        this.initialValue = initialValue;
        return this;
    }


    /**
     * 检查编译状态
     * 判断只有满足 accept 的时候
     *
     * @param accept accept
     */
    private void checkExpect(@Expect final int accept) {
        checkState(expect == accept, "Unexpected compiler state");
    }


    /**
     * 检查编译状态
     * 判断只有满足 accept1 或 accept2 的时候
     *
     * @param accept1 accept1
     * @param accept2 accept2
     */
    private void checkExpect(@Expect final int accept1, @Expect final int accept2) {
        checkState(expect == accept1 || expect == accept2, "Unexpected compiler state");
    }


    /**
     * 检查 是否 设置了 懒加载
     */
    private void checkGoLazyUnused() {
        checkState(!goLazyUsed, "Unexpected occurrence of async directive after goLazy()");
    }

    //region REventSource


    /**
     * 指定事件源（ 观察者 ）
     *
     * @param observables 观察者数组
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler observe(@NonNull final Observable... observables) {
        /*
         * 检查 当前编译状态 是否是 FIRST_EVENT_SOURCE：第一事件源状态
         * 或者
         * 是否是 FREQUENCY_OR_MORE_EVENT_SOURCE：频率状态 或者 更多事件源状态
         */
        checkExpect(FIRST_EVENT_SOURCE, FREQUENCY_OR_MORE_EVENT_SOURCE);
        // 添加 事件源（ 观察者 ）
        for (Observable observable : observables) {
            eventSources.add(checkNotNull(observable));
        }
        // 设置 当前编译状态 为 FREQUENCY_OR_MORE_EVENT_SOURCE：频率状态 或者 更多事件源状态
        expect = FREQUENCY_OR_MORE_EVENT_SOURCE;
        return this;
    }

    //endregion REventSource

    //region RFrequency


    /**
     * 设置更新频率，小于 0 的话，会自动设置为 0
     *
     * @param millis 频率，单位为 毫秒
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler onUpdatesPer(int millis) {
        // 检查 当前编译状态 是否是 FREQUENCY_OR_MORE_EVENT_SOURCE：频率状态 或者 更多事件源状态
        checkExpect(FREQUENCY_OR_MORE_EVENT_SOURCE);
        // 频率 小于 0 的话，会自动设置为 0
        frequency = Math.max(0, millis);
        // 设置 当前编译状态 为 FLOW：流状态
        expect = FLOW;
        return this;
    }


    /**
     * 等同于，onUpdatesPer(0)
     *
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler onUpdatesPerLoop() {
        return onUpdatesPer(0);
    }

    //endregion RFrequency

    //region RSyncFlow


    /**
     * RFlow 获取数据操作
     * 可类型转换（ 供应者 ）
     *
     * @param supplier 供应者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler getFrom(@NonNull final Supplier supplier) {
        // 检查 当前编译状态 是否是 FLOW：流状态
        checkExpect(FLOW);
        /*
         * 调用 CompiledRepository.addGetFrom(...)
         * 添加 对应的
         * 1. getFrom 指令
         * 2. Supplier
         */
        addGetFrom(supplier, directives);
        return this;
    }


    /**
     * RFlow 合并操作
     * 可类型转换（ 供应者 ）
     * 可以合并供应者转换后的数据 和 之前的流处理结果数据（ 合并者 ）
     *
     * @param supplier 供应者
     * @param merger 合并者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler mergeIn(@NonNull final Supplier supplier,
                                      @NonNull final Merger merger) {
        // 检查 当前编译状态 是否是 FLOW：流状态
        checkExpect(FLOW);
        /*
         * 调用 CompiledRepository.addMergeIn(...)
         * 添加 对应的
         * 1. mergeIn 指令
         * 2. Supplier
         * 3. Merger
         */
        addMergeIn(supplier, merger, directives);
        return this;
    }


    /**
     * RFlow 转换操作
     * 用于类型转换
     * 与 getFrom(...) 的区别：
     * getFrom(...) 是通过 Supplier 可以在提供数据时，进行间接的类型转换
     * transform(...) 是通过 Function 进行直接的类型之间转换
     *
     * @param function 转换方法
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler transform(@NonNull final Function function) {
        // 检查 当前编译状态 是否是 FLOW：流状态
        checkExpect(FLOW);
        /*
         * 调用 CompiledRepository.addTransform(...)
         * 添加 对应的
         * 1. transform 指令
         * 2. Function
         */
        addTransform(function, directives);
        return this;
    }


    /**
     * RSyncFlow 检查方法
     * 需要传入一个 断定者，进行上个流传入类型数据的检查操作
     *
     * 然后调用 check(@NonNull final Function function, @NonNull final Predicate predicate)
     * 还需要传入一个 通用的 IdentityFunction
     *
     * @param predicate 断定者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler check(@NonNull final Predicate predicate) {
        return check(identityFunction(), predicate);
    }


    /**
     * RSyncFlow 检查方法
     * 需要
     * 传入一个 转换方法，进行上个流类型数据的转换，转换为 TCase 类型
     * 再传入一个 断定者，进行转换后类型数据的检查操作
     *
     * @param function 转换方法
     * @param predicate 断定者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler check(
        @NonNull final Function function, @NonNull final Predicate predicate) {
        // 检查 当前编译状态 是否是 FLOW：流状态
        checkExpect(FLOW);
        // 检查 Function 是否为 null
        caseExtractor = checkNotNull(function);
        // 检查 Function 是否为 null
        casePredicate = checkNotNull(predicate);
        // 设置 当前编译状态 为 RFlow -> RTermination 的终止状态
        expect = TERMINATE_THEN_FLOW;
        return this;
    }


    /**
     * RSyncFlow 发送操作
     * 其实就是 添加一个 Receiver
     *
     * @param receiver 接受者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler sendTo(@NonNull final Receiver receiver) {
        // 检查 当前编译状态 是否是 FLOW：流状态
        checkExpect(FLOW);
        /*
         * 调用 CompiledRepository.addSendTo(...)
         * 添加 对应的
         * 1. sendTo 指令
         * 2. Receiver
         */
        addSendTo(checkNotNull(receiver), directives);
        return this;
    }


    /**
     * RSyncFlow 绑定操作
     * 需要
     * 传入一个 供应者，提供 数据
     * 再传入一个 绑定者，进行上一个流的 数据 与 供应者提供的 数据进行绑定
     *
     * @param secondValueSupplier 供应者
     * @param binder 绑定者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler bindWith(@NonNull final Supplier secondValueSupplier,
                                       @NonNull final Binder binder) {
        // 检查 当前编译状态 是否是 FLOW：流状态
        checkExpect(FLOW);
        /*
         * 调用 CompiledRepository.addBindWith(...)
         * 添加 对应的
         * 1. bind 指令
         * 2. Supplier
         * 3. Binder
         */
        addBindWith(secondValueSupplier, binder, directives);
        return this;
    }


    /**
     * RSyncFlow 快进操作
     * 什么也不做，直接快进到 RConfig 配置状态
     *
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler thenSkip() {
        /*
         * 调用 endFlow(...)
         * 快进标识 ship 设置为 true
         */
        endFlow(true);
        return this;
    }


    /**
     * RSyncFlow 快进获取数据操作
     * 可类型转换（ 供应者 ）
     *
     * @param supplier 供应者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler thenGetFrom(@NonNull final Supplier supplier) {
        // 调用 getFrom(...)
        getFrom(supplier);
        /*
         * 调用 endFlow(...)
         * 快进标识 ship 设置为 false
         */
        endFlow(false);
        return this;
    }


    /**
     * RSyncFlow 快进合并操作
     * 可类型转换（ 供应者 ）
     * 可以合并供应者转换后的数据 和 之前的流处理结果数据（ 合并者 ）
     *
     * @param supplier 供应者
     * @param merger 合并者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler thenMergeIn(
        @NonNull final Supplier supplier, @NonNull final Merger merger) {
        // 调用 mergeIn(...)
        mergeIn(supplier, merger);
        /*
         * 调用 endFlow(...)
         * 快进标识 ship 设置为 false
         */
        endFlow(false);
        return this;
    }


    /**
     * RSyncFlow 快进转换操作
     * 用于类型转换
     *
     * @param function 转换方法
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler thenTransform(@NonNull final Function function) {
        // 调用 transform(...)
        transform(function);
        /*
         * 调用 endFlow(...)
         * 快进标识 ship 设置为 false
         */
        endFlow(false);
        return this;
    }


    /**
     * 结束流
     * 主要用于 thenXxx(...) 方法
     *
     * @param skip 是否快进
     */
    private void endFlow(final boolean skip) {
        /*
         * 调用 CompiledRepository.addEnd(...)
         * 添加 对应的
         * 1. end 指令
         * 2. skip 快进标识
         */
        addEnd(skip, directives);
        // 设置 当前编译状态 为 CONFIG：配置状态
        expect = CONFIG;
    }


    /**
     * RFlow 尝试获取数据操作
     * 可以截获异常，进行处理（ 供应者+Result ）
     *
     * @param attemptSupplier Result 供应者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler attemptGetFrom(@NonNull final Supplier attemptSupplier) {
        // 调用 getFrom(...)
        getFrom(attemptSupplier);
        // 设置 当前编译状态 为 RFlow -> RTermination 的终止状态
        expect = TERMINATE_THEN_FLOW;
        return this;
    }


    /**
     * RFlow 尝试合并操作
     * 可类型转换（ 供应者 ）
     * 可以合并供应者转换后的数据 和 之前的流处理结果数据（ 合并者 ）
     * 可以截获异常，进行处理（ 合并者+Result ）
     *
     * @param supplier 供应者
     * @param attemptMerger 合并者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler attemptMergeIn(
        @NonNull final Supplier supplier, @NonNull final Merger attemptMerger) {
        // 调用 mergeIn(...)
        mergeIn(supplier, attemptMerger);
        // 设置 当前编译状态 为 RFlow -> RTermination 的终止状态
        expect = TERMINATE_THEN_FLOW;
        return this;
    }


    /**
     * RFlow 尝试转换操作
     * 用于类型转换
     * 可以截获异常，进行处理（ 转换方法+Result
     *
     * @param attemptFunction 转换方法
     * @return attemptFunction
     */
    @NonNull
    @Override
    public RepositoryCompiler attemptTransform(@NonNull final Function attemptFunction) {
        // 调用 transform(...)
        transform(attemptFunction);
        // 设置 当前编译状态 为 RFlow -> RTermination 的终止状态
        expect = TERMINATE_THEN_FLOW;
        return this;
    }


    /**
     * RSyncFlow 快进尝试获取数据操作
     * 可以截获异常，进行处理（ 供应者+Result ）
     *
     * @param attemptSupplier Result 供应者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler thenAttemptGetFrom(@NonNull final Supplier attemptSupplier) {
        // 调用 transform(...)
        getFrom(attemptSupplier);
        // 设置 当前编译状态 为 RSyncFlow -> RTermination 的终止状态
        expect = TERMINATE_THEN_END;
        return this;
    }


    /**
     * RSyncFlow 快进尝试合并操作
     * 可类型转换（ 供应者 ）
     * 可以合并供应者转换后的数据 和 之前的流处理结果数据（ 合并者 ）
     * 可以截获异常，进行处理（ 合并者+Result ）
     *
     * @param supplier 供应者
     * @param attemptMerger 合并者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler thenAttemptMergeIn(
        @NonNull final Supplier supplier, @NonNull final Merger attemptMerger) {
        // 调用 mergeIn(...)
        mergeIn(supplier, attemptMerger);
        // 设置 当前编译状态 为 RSyncFlow -> RTermination 的终止状态
        expect = TERMINATE_THEN_END;
        return this;
    }


    /**
     * RSyncFlow 快进尝试转换操作
     * 用于类型转换
     * 可以截获异常，进行处理（ 转换方法+Result ）
     *
     * @param attemptFunction 转换方法
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler thenAttemptTransform(@NonNull final Function attemptFunction) {
        // 调用 transform(...)
        transform(attemptFunction);
        // 设置 当前编译状态 为 RSyncFlow -> RTermination 的终止状态
        expect = TERMINATE_THEN_END;
        return this;
    }

    //endregion RSyncFlow

    //region RFlow


    /**
     * RFlow 线程池操作
     *
     * @param executor 线程池
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler goTo(@NonNull final Executor executor) {
        // 检查 当前编译状态 是否是 FLOW：流状态
        checkExpect(FLOW);
        // 检查 是否 设置了 懒加载
        checkGoLazyUnused();
        // 调用 addGoTo(...)
        addGoTo(executor, directives);
        return this;
    }


    /**
     * RFlow 懒加载操作
     *
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler goLazy() {
        // 检查 当前编译状态 是否是 FLOW：流状态
        checkExpect(FLOW);
        // 检查 是否 设置了 懒加载
        checkGoLazyUnused();
        // 调用 addGoLazy(...)
        addGoLazy(directives);
        // 记录 懒加载
        goLazyUsed = true;
        return this;
    }

    //endregion RFlow

    //region RTermination


    /**
     * RTermination 快进操作
     *
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler orSkip() {
        /*
         * 调用 terminate(...)
         * 不传入 Function
         */
        terminate(null);
        return this;
    }


    /**
     * RTermination 结束状态
     * 其实又一次进行转换
     *
     * @param valueFunction 转换方法
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler orEnd(@NonNull final Function valueFunction) {
        /*
         * 调用 terminate(...)
         * 传入 Function
         */
        terminate(valueFunction);
        return this;
    }


    /**
     * @param valueFunction 转换方法
     */
    private void terminate(@Nullable final Function valueFunction) {
        /*
         * 检查 当前编译状态 是否是 TERMINATE_THEN_FLOW：RFlow -> RTermination 的终止状态
         * 或者
         * 是否是 TERMINATE_THEN_END：RSyncFlow -> RTermination 的终止状态
         */
        checkExpect(TERMINATE_THEN_FLOW, TERMINATE_THEN_END);
        /**
         * 检查 check(...) 操作符 用的
         * Function
         * 是否 为 null
         */
        if (caseExtractor != null) {
            /*
             * 调用 CompiledRepository.addEnd(...)
             * 添加 对应的
             * 1. check 指令
             * 2. check(...) 操作符 用的 Function
             * 3. check(...) 操作符 用的 Predicate
             * 4. valueFunction 转换方法
             */
            addCheck(caseExtractor, checkNotNull(casePredicate), valueFunction, directives);
        } else {
            /*
             * 调用 CompiledRepository.addFilterSuccess(...)
             * 添加 对应的
             * 1. filter_success 指令
             * 2. valueFunction 转换方法
             */
            addFilterSuccess(valueFunction, directives);
        }
        // 回收资源
        caseExtractor = null;
        casePredicate = null;

        // 检查 当前编译状态 是否是 RSyncFlow -> RTermination 的终止状态
        if (expect == TERMINATE_THEN_END) {
            /*
             * 调用 endFlow(...)
             * 快进标识 ship 设置为 false
             */
            endFlow(false);
        } else {
            // 设置 当前编译状态 为 FLOW：流状态
            expect = FLOW;
        }
    }

    //endregion RTermination

    //region RConfig


    /**
     * RConfig 通知操作
     * 会传入一个 合并者，合并出一个 Boolean 类型
     *
     * @param notifyChecker 合并者
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler notifyIf(@NonNull final Merger notifyChecker) {
        // 检查 当前编译状态 是否是 CONFIG：配置状态
        checkExpect(CONFIG);
        this.notifyChecker = checkNotNull(notifyChecker);
        return this;
    }


    /**
     * RConfig 失效时的操作（ 一些特殊的行为：导致仓库失效，从观察状态变为不观察状态 ）
     *
     * @param deactivationConfig RepositoryConfig 类型
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler onDeactivation(@RepositoryConfig final int deactivationConfig) {
        // 检查 当前编译状态 是否是 CONFIG：配置状态
        checkExpect(CONFIG);
        this.deactivationConfig = deactivationConfig;
        return this;
    }


    /**
     * RConfig 并发更新时（ 一些特殊的行为：一个观察者从事件源中被观察了，然后一个数据处理流还在运行 ）
     *
     * @param concurrentUpdateConfig RepositoryConfig 类型
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler onConcurrentUpdate(
        @RepositoryConfig final int concurrentUpdateConfig) {
        // 检查 当前编译状态 是否是 CONFIG：配置状态
        checkExpect(CONFIG);
        this.concurrentUpdateConfig = concurrentUpdateConfig;
        return this;
    }


    /**
     * 完成 仓库 编译
     *
     * @return Repository
     */
    @NonNull
    @Override
    public Repository compile() {
        // 调用 compileRepositoryAndReset() 去 完成 仓库 编译
        Repository repository = compileRepositoryAndReset();
        // 回收 仓库编译器
        recycle(this);
        return repository;
    }


    /**
     * RConfig -> REventSource 变换
     * 就是一个 仓库 处理完后的流
     * 拿着这个流的数据 接着 进入到 新仓库的 编译过程中 的 REventSource 状态
     *
     * @param value 新仓库的目标数据类型初始值
     * @return RepositoryCompiler
     */
    @NonNull
    @Override
    public RepositoryCompiler compileIntoRepositoryWithInitialValue(@NonNull final Object value) {
        // 调用 compileRepositoryAndReset() 去 完成 仓库 编译
        Repository repository = compileRepositoryAndReset();
        // Don't recycle, instead sneak in the first directive and start the second repository
        // 调用 addGetFrom(...)
        addGetFrom(repository, directives);
        // 重新进入到 REventSource 状态
        return start(value).observe(repository);
    }


    /**
     * 完成 仓库 编译
     * 并且 重置 仓库编译器 的 状态
     *
     * @return Repository
     */
    @NonNull
    private Repository compileRepositoryAndReset() {
        // 检查 当前编译状态 是否是 CONFIG：配置状态
        checkExpect(CONFIG);
        /*
         * 调用 CompiledRepository.compiledRepository(...)
         * 去创建一个仓库，该仓库的类型为 CompiledRepository
         */
        Repository repository = compiledRepository(initialValue, eventSources, frequency,
            directives,
            notifyChecker, concurrentUpdateConfig, deactivationConfig);

        /*
         * 恢复 如下 编译参数 到默认设置
         */
        expect = NOTHING;
        initialValue = null;
        eventSources.clear();
        frequency = 0;
        directives.clear();
        goLazyUsed = false;
        notifyChecker = objectsUnequal();
        deactivationConfig = RepositoryConfig.CONTINUE_FLOW;
        concurrentUpdateConfig = RepositoryConfig.CONTINUE_FLOW;
        return repository;
    }

    //endregion RConfig
}
