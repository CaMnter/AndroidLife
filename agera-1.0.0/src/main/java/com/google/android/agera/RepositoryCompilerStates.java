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

import android.support.annotation.NonNull;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * Container of the compiler state interfaces supporting the declaration of {@link Repository}s
 * using the type-safe declarative language.
 *
 * <h3>List of directives</h3>
 *
 * <b>Variables:</b> s: supplier; fs: fallible supplier; m: merger; fm: fallible merger;
 * f: function; ff: fallible function; p: predicate; r: receiver; b: binder; e: executor; v: value.
 * <ul>
 * <li>({@link RFlow#thenGetFrom then}){@link RFlow#getFrom GetFrom(s)}
 * <li>({@link RFlow#thenMergeIn then}){@link RFlow#mergeIn MergeIn(s, m)}
 * <li>({@link RFlow#thenTransform then}){@link RFlow#transform Transform(f)}
 * <li>({@link RFlow#thenAttemptGetFrom then}){@link
 * RFlow#attemptGetFrom AttemptGetFrom(fs)}.<i>term</i>
 * <li>({@link RFlow#thenAttemptMergeIn then}){@link
 * RFlow#attemptMergeIn AttemptMergeIn(s, fm)}.<i>term</i>
 * <li>({@link RFlow#thenAttemptTransform then}){@link
 * RFlow#attemptTransform AttemptTransform(ff)}.<i>term</i>
 * <li>{@link RFlow#check(Predicate) check(p)}.<i>term</i>
 * <li>{@link RFlow#check(Function, Predicate) check(f, p)}.<i>term</i>
 * <li>{@link RFlow#sendTo sendTo(r)}
 * <li>{@link RFlow#bindWith bindWith(s, b)}
 * <li>{@link RFlow#goTo goTo(e)}
 * <li>{@link RFlow#goLazy goLazy()}
 * <li>{@link RFlow#thenSkip thenSkip()}
 * </ul>
 * where <i>term</i> (the termination clause) is one of:
 * <ul>
 * <li>{@link RTermination#orSkip orSkip()}
 * <li>{@link RTermination#orEnd orEnd(f)}
 * </ul>
 *
 * Agera 中抽象出来的 仓库编译状态 接口
 * 由 RepositoryCompilerStates 编译（ create ）出 一个 Repository
 * 并且规定了仓库状态的执行顺序：
 * 1. REventSource
 * 2. RFrequency
 * 3. RFlow
 * 4. RSyncFlow（ 只有 RFlow.goTo(...) 后才进入此状态 ）
 * 5. RTermination
 * 6. RConfig
 */
public interface RepositoryCompilerStates {

    // Note on documentation grammar: most method summaries for flow directives use an infinitive verb
    // phrase ("do something") instead of the usual 3rd-person grammar ("does something"). This is
    // because the full sentence for these method summaries are "this method specifies that the next
    // step of the flow should do something", rather than "this method does something".

    /**
     * Compiler state allowing to specify the event source of the repository.
     *
     * @param <TVal> Value type of the repository.
     * @param <TStart> Value type at the start of the data processing flow. May be different from
     * {@code TVal} when chain-building a repository that starts with
     * {@link RConfig#compileIntoRepositoryWithInitialValue}.
     *
     * REventSource 事件源状态
     * TVal 类型 作为 仓库数据类型
     * TStart 类型 作为 数据流开始类型
     *
     * 定义了一个基本的接口方法 - observe(Observable... observables)
     * 可以指定一组 事件源。即，被观察者
     */
    interface REventSource<TVal, TStart> {

        /**
         * Specifies the event source of the compiled repository.
         * 指定事件源
         *
         * 结束后，进入 RFrequency 频率状态
         * TVal 类型 作为 RFrequency 仓库数据类型 TVal
         * TStart 类型 作为 RFrequency 数据流开始类型 TStart
         */
        @NonNull RFrequency<TVal, TStart> observe(@NonNull Observable... observables);
    }

    /**
     * Compiler state allowing to specify the frequency of invoking the data processing flow.
     *
     * @param <TVal> Value type of the repository.
     * @param <TStart> Value type at the start of the data processing flow.
     *
     * RFrequency 频率状态
     * TVal 类型 作为 仓库数据类型
     * TStart 类型 作为 数据流开始类型
     * 同时，也继承了 REventSource<TVal, TStart> 状态，也能在次状态指定事件源
     *
     * 该状态提供的方法可以 指定调用数据处理流的频率
     */
    interface RFrequency<TVal, TStart> extends REventSource<TVal, TStart> {

        /**
         * Specifies the minimum timeout to wait since starting the previous data processing flow,
         * before starting another flow to respond to updates from the event sources. Flows will
         * not
         * be
         * started more frequent than if {@link #onUpdatesPerLoop()} were used, even if the given
         * timeout is sufficiently small.
         *
         *
         * 设置更新频率，小于 0 的话，会自动设置为 0
         * 这里的频率是两个数据处理流的之间的频率
         *
         * 结束后，进入 RFlow 流状态
         * TVal 类型 作为 RFlow 仓库数据类型 TVal
         * TStart 类型 作为 RFlow 上一个数据流输出类型 TPre
         */
        @NonNull RFlow<TVal, TStart, ?> onUpdatesPer(int millis);

        /**
         * Specifies that multiple updates from the event sources per worker looper loop should
         * start
         * only one data processing flow.
         *
         * 等同于，onUpdatesPer(0)
         *
         * 结束后，进入 RFlow 流状态
         * TVal 类型 作为 RFlow 仓库数据类型 TVal
         * TStart 类型 作为 RFlow 上一个数据流输出类型 TPre
         */
        @NonNull RFlow<TVal, TStart, ?> onUpdatesPerLoop();
    }

    /**
     * Compiler state allowing to specify the next directive of the data processing flow.
     *
     * @param <TVal> Value type of the repository.
     * @param <TPre> The output value type of the previous directive.
     *
     * RFlow 流状态
     * TVal 类型 作为 RFlow 仓库数据类型
     * TPre 类型 作为 RFlow 上一个数据流输出类型
     *
     * 1. 可以在这选择进行一些数据流操作，得到返回的 RFlow 对象
     * 2. 也可以执行一些 attemptXxx 系列方法，跳出此状态，进入到 RTermination 终止状态
     * 3. 还可以执行 goLazy 方法，进入到 RSyncFlow 同步流状态
     */
    interface RFlow<TVal, TPre, TSelf extends RFlow<TVal, TPre, TSelf>>
            extends RSyncFlow<TVal, TPre, TSelf> {
        // Methods whose return types need subtyping (due to no "generic type of generic type" in Java):

        /**
         * RFlow 获取数据操作
         * 可类型转换（ 供应者 ）
         *
         * @param supplier 供应者
         * @param <TCur> 供应者 的 目标类型
         * @return 返回 一个 RFlow 流状态
         *
         * TVal 类型 作为 RFlow 仓库数据类型
         * TCur 类型 作为 RFlow 上一个数据流输出类型
         */
        @NonNull
        @Override <TCur> RFlow<TVal, TCur, ?> getFrom(@NonNull Supplier<TCur> supplier);

        /**
         * RFlow 尝试获取数据操作
         * 可以截获异常，进行处理（ 供应者+Result ）
         *
         * @param attemptSupplier Result 供应者
         * @param <TCur> Result 供应者 的 目标类型
         * @return 返回一个 RTermination 终止状态
         *
         * TVal 类型 作为 RTermination 仓库数据类型
         * Throwable 类型 作为 RTermination 流终止类型
         * RFlow<TVal, TCur, ?> 类型 作为 RTermination 编译返回状态类型
         */
        @NonNull
        @Override <TCur> RTermination<TVal, Throwable, RFlow<TVal, TCur, ?>> attemptGetFrom(
                @NonNull Supplier<Result<TCur>> attemptSupplier);

        @NonNull
        @Override
        RTerminationOrContinue<TVal, Throwable, RConfig<TVal>,
            RFlow<TVal, Throwable, ?>> thenAttemptGetFrom(
            @NonNull Supplier<? extends Result<? extends TVal>> attemptSupplier);

        /**
         * RFlow 合并操作
         * 可类型转换（ 供应者 ）
         * 可以合并供应者转换后的数据 和 之前的流处理结果数据（ 合并者 ）
         *
         * @param supplier 供应者
         * @param merger 合并者
         * @param <TAdd> 供应者 的 目标类型
         * @param <TCur> 合并者 的 目标类型
         * @return 返回 一个 RFlow 流状态
         *
         * TVal 类型 作为 RFlow 仓库数据类型
         * TCur 类型 作为 RFlow 上一个数据流输出类型
         */
        @NonNull
        @Override <TAdd, TCur> RFlow<TVal, TCur, ?> mergeIn(@NonNull Supplier<TAdd> supplier,
                                                            @NonNull
                                                            Merger<? super TPre, ? super TAdd, TCur> merger);

        /**
         * RFlow 尝试合并操作
         * 可类型转换（ 供应者 ）
         * 可以合并供应者转换后的数据 和 之前的流处理结果数据（ 合并者 ）
         * 可以截获异常，进行处理（ 合并者+Result ）
         *
         * @param supplier 供应者
         * @param attemptMerger 合并者
         * @param <TAdd> 供应者 的 目标类型
         * @param <TCur> 合并者 的 Result 目标类型
         * @return 返回 一个 RTermination 终止状态
         *
         * TVal 类型 作为 RTermination 仓库数据类型
         * Throwable 类型 作为 RTermination 流终止类型
         * RFlow<TVal, TCur, ?> 类型 作为 RTermination 编译返回状态类型
         */
        @NonNull
        @Override <TAdd, TCur> RTermination<TVal, Throwable, RFlow<TVal, TCur, ?>> attemptMergeIn(
                @NonNull Supplier<TAdd> supplier,
                @NonNull Merger<? super TPre, ? super TAdd, Result<TCur>> attemptMerger);

        @NonNull
        @Override
        <TAdd> RTerminationOrContinue<TVal, Throwable, RConfig<TVal>,
            RFlow<TVal, Throwable, ?>> thenAttemptMergeIn(
            @NonNull Supplier<TAdd> supplier,
            @NonNull Merger<? super TPre, ? super TAdd,
                ? extends Result<? extends TVal>> attemptMerger);

        /**
         * RFlow 转换操作
         * 用于类型转换
         * 与 getFrom(...) 的区别：
         * getFrom(...) 是通过 Supplier 可以在提供数据时，进行间接的类型转换
         * transform(...) 是通过 Function 进行直接的类型之间转换
         *
         * @param function 转换方法
         * @param <TCur> 转换方法 的 目标类型
         * @return 返回 一个 RFlow 流状态
         *
         * TVal 类型 作为 RFlow 仓库数据类型
         * TCur 类型 作为 RFlow 上一个数据流输出类型
         */
        @NonNull
        @Override <TCur> RFlow<TVal, TCur, ?> transform(
                @NonNull Function<? super TPre, TCur> function);

        /**
         * RFlow 尝试转换操作
         * 用于类型转换
         * 可以截获异常，进行处理（ 转换方法+Result ）
         *
         * @param attemptFunction 转换方法
         * @param <TCur> 转换方法 的 Result 目标类型
         * @return 返回 一个 RTermination 终止状态
         *
         * TVal 类型 作为 RTermination 仓库数据类型
         * Throwable 类型 作为 RTermination 流终止类型
         * RFlow<TVal, TCur, ?> 类型 作为 RTermination 编译返回状态类型
         */
        @NonNull
        @Override <TCur> RTermination<TVal, Throwable, RFlow<TVal, TCur, ?>> attemptTransform(
                @NonNull Function<? super TPre, Result<TCur>> attemptFunction);

        @NonNull
        @Override
        RTerminationOrContinue<TVal, Throwable, RConfig<TVal>,
            RFlow<TVal, Throwable, ?>> thenAttemptTransform(
            @NonNull Function<? super TPre, ? extends Result<? extends TVal>> attemptFunction);

        // Asynchronous directives:

        /**
         * Go to the given {@code executor} to continue the data processing flow. The executor is
         * assumed to never throw {@link RejectedExecutionException}. Synchronous executors are
         * supported but the risk of stack overflow will be higher. Note that when the executor
         * resumes
         * the flow, the directives that follow are run sequentially within the same
         * {@link Runnable#run()} call, until the flow completes or the next {@code goTo()} or, if
         * applicable, {@code goLazy()} directive is reached. Depending on the directives and
         * operators
         * used, this may starve the executor. If necessary, use additional {@code goTo()}
         * directives
         * with the same executor to achieve fairness.
         */
        /**
         * RFlow 线程池操作
         *
         * @param executor 线程池
         * @return 一个 RFlow 流状态
         *
         * 所有类型跟执行 goTo 前的类型一样
         */
        @NonNull TSelf goTo(@NonNull Executor executor);

        /**
         * Suspend the data processing flow and notify the registered {@link Updatable}s of
         * updates.
         * The remaining of the flow will be run synchronously <i>and uninterruptibly</i> the first
         * time
         * {@link Repository#get()} is called, to produce the new repository value lazily. After
         * this
         * directive, {@link #goTo(Executor)} is no longer available, and all further operators
         * should
         * be fairly lightweight in order not to block the callers of {@code get()} for too long.
         */
        /**
         * RFlow 懒加载操作
         *
         * @return 返回 一个 所有类型相同 RSyncFlow 同步流状态
         */
        @NonNull RSyncFlow<TVal, TPre, ?> goLazy();
    }

    /**
     * Compiler state allowing to specify the final synchronous steps of the data processing flow.
     *
     * @param <TVal> Value type of the repository.
     * @param <TPre> The output value type of the previous directive.
     * @param <TSelf> Self-type; for Java compiler type inference only.
     *
     * RSyncFlow 同步流状态
     *
     * TVal 类型 作为 RSyncFlow 仓库数据类型
     * TPre 类型 作为 RSyncFlow 上一个数据流输出类型
     */
    interface RSyncFlow<TVal, TPre, TSelf extends RSyncFlow<TVal, TPre, TSelf>> {

        /**
         * Ignore the input value, and use the value newly obtained from the given supplier as the
         * output value.
         */
        /**
         * RSyncFlow 获取数据操作
         * 可类型转换（ 供应者 ）
         *
         * @param supplier 供应者
         * @param <TCur> 供应者 的 目标类型
         * @return 返回 一个 RSyncFlow 同步流状态
         *
         * TVal 类型 作为 RSyncFlow 仓库数据类型
         * TCur 类型 作为 RSyncFlow 上一个数据流输出类型
         */
        @NonNull <TCur> RSyncFlow<TVal, TCur, ?> getFrom(@NonNull Supplier<TCur> supplier);

        /**
         * Like {@link #getFrom}, ignore the input value and attempt to get the new value from the
         * given
         * supplier. If the attempt fails, terminate the data processing flow by sending the
         * failure
         * to
         * the termination clause that follows; otherwise take the successful value as the output
         * of
         * this directive.
         */
        /**
         * RSyncFlow 尝试获取数据操作
         * 可以截获异常，进行处理（ 供应者+Result ）
         *
         * @param attemptSupplier Result 供应者
         * @param <TCur> Result 供应者 的 目标类型
         * @return 返回一个 RTermination 终止状态
         *
         * TVal 类型 作为 RTermination 仓库数据类型
         * Throwable 类型 作为 RTermination 流终止类型
         * RSyncFlow<TVal, TCur, ?> 类型 作为 RTermination 编译返回状态类型
         */
        @NonNull <TCur>
        RTermination<TVal, Throwable, ? extends RSyncFlow<TVal, TCur, ?>> attemptGetFrom(
                @NonNull Supplier<Result<TCur>> attemptSupplier);

        /**
         * Take the input value and the value newly obtained from the given supplier, merge them
         * using
         * the given merger, and use the resulting value as the output value.
         */
        /**
         * RSyncFlow 合并操作
         * 可类型转换（ 供应者 ）
         * 可以合并供应者转换后的数据 和 之前的流处理结果数据（ 合并者 ）
         *
         * @param supplier 供应者
         * @param merger 合并者
         * @param <TAdd> 供应者 的 目标类型
         * @param <TCur> 合并者 的 目标类型
         * @return 返回 一个 RSyncFlow 同步流状态
         *
         * TVal 类型 作为 RSyncFlow 仓库数据类型
         * TCur 类型 作为 RSyncFlow 上一个数据流输出类型
         */
        @NonNull <TAdd, TCur> RSyncFlow<TVal, TCur, ?> mergeIn(@NonNull Supplier<TAdd> supplier,
                                                               @NonNull
                                                               Merger<? super TPre, ? super TAdd, TCur> merger);

        /**
         * Like {@link #mergeIn}, take the input value and the value newly obtained from the given
         * supplier, and attempt to merge them using the given merger. If the attempt fails,
         * terminate
         * the data processing flow by sending the failure to the termination clause that follows;
         * otherwise take the successful value as the output of this directive.
         *
         * <p>This method is agnostic of the return type of the {@code supplier}. If it itself is
         * fallible, the {@code merger} is held responsible for processing the failure, which may
         * choose
         * to pass the failure on as the result of the merge.
         */
        /**
         * RSyncFlow 尝试合并操作
         * 可类型转换（ 供应者 ）
         * 可以合并供应者转换后的数据 和 之前的流处理结果数据（ 合并者 ）
         * 可以截获异常，进行处理（ 合并者+Result ）
         *
         * @param supplier 供应者
         * @param attemptMerger 合并者
         * @param <TAdd> 供应者 的 目标类型
         * @param <TCur> 合并者 的 Result 目标类型
         * @return 返回 一个 RTermination 终止状态
         *
         * TVal 类型 作为 RTermination 仓库数据类型
         * Throwable 类型 作为 RTermination 流终止类型
         * RSyncFlow<TVal, TCur, ?> 类型 作为 RTermination 编译返回状态类型
         */
        @NonNull <TAdd, TCur>
        RTermination<TVal, Throwable, ? extends RSyncFlow<TVal, TCur, ?>> attemptMergeIn(
                @NonNull Supplier<TAdd> supplier,
                @NonNull Merger<? super TPre, ? super TAdd, Result<TCur>> attemptMerger);

        /**
         * Transform the input value using the given function into the output value.
         */
        /**
         * RSyncFlow 转换操作
         * 用于类型转换
         * 与 getFrom(...) 的区别：
         * getFrom(...) 是通过 Supplier 可以在提供数据时，进行间接的类型转换
         * transform(...) 是通过 Function 进行直接的类型之间转换
         *
         * @param function 转换方法
         * @param <TCur> 转换方法 的 目标类型
         * @return 返回 一个 RFlow 流状态
         *
         * TVal 类型 作为 RSyncFlow 仓库数据类型
         * TCur 类型 作为 RSyncFlow 上一个数据流输出类型
         */
        @NonNull <TCur> RSyncFlow<TVal, TCur, ?> transform(
                @NonNull Function<? super TPre, TCur> function);

        /**
         * Like {@link #transform}, attempt to transform the input value using the given function.
         * If
         * the attempt fails, terminate the data processing flow by sending the failure to the
         * termination clause that follows; otherwise take the successful value as the output of
         * this
         * directive.
         */
        /**
         * RSyncFlow 尝试转换操作
         * 用于类型转换
         * 可以截获异常，进行处理（ 转换方法+Result ）
         *
         * @param attemptFunction 转换方法
         * @param <TCur> 转换方法 的 Result 目标类型
         * @return 返回 一个 RTermination 终止状态
         *
         * TVal 类型 作为 RTermination 仓库数据类型
         * Throwable 类型 作为 RTermination 流终止类型
         * RSyncFlow<TVal, TCur, ?> 类型 作为 RTermination 编译返回状态类型
         */
        @NonNull
        <TCur> RTermination<TVal, Throwable, ? extends RSyncFlow<TVal, TCur, ?>> attemptTransform(
                @NonNull Function<? super TPre, Result<TCur>> attemptFunction);

        /**
         * Check the input value with the given predicate. If the predicate applies, continue the
         * data
         * processing flow with the same value, otherwise terminate the flow with the termination
         * clause
         * that follows. The termination clause takes the input value as its input.
         */
        /**
         * RSyncFlow 检查方法
         * 需要传入一个 断定者，进行上个流传入类型数据的检查操作
         *
         * @param predicate 断定者
         * @return 返回 一个 RTermination 终止状态
         *
         * 类型全部不变地传下去
         */
        @NonNull RTermination<TVal, TPre, TSelf> check(@NonNull Predicate<? super TPre> predicate);

        /**
         * Use the case-function to compute a case value out of the input value and check it with
         * the
         * given predicate. If the predicate applies to the case value, continue the data
         * processing
         * flow with the <i>input value</i>, otherwise terminate the flow with the termination
         * clause
         * that follows. The termination clause takes the <i>case value</i> as its input.
         */
        /**
         * RSyncFlow 检查方法
         * 需要
         * 传入一个 转换方法，进行上个流类型数据的转换，转换为 TCase 类型
         * 再传入一个 断定者，进行转换后类型数据的检查操作
         *
         * @param caseFunction 转换方法
         * @param casePredicate 断定者
         * @param <TCase> 转换后的数据类型，也是要判断的数据类型
         * @return 返回 一个 RTermination 终止状态
         *
         * 类型全部不变地传下去
         */
        @NonNull <TCase> RTermination<TVal, TCase, TSelf> check(
                @NonNull Function<? super TPre, TCase> caseFunction,
                @NonNull Predicate<? super TCase> casePredicate);

        /**
         * Send the input value to the given receiver, and then pass on the input value as the
         * output of
         * this directive, not modifying it.
         *
         * <p>Typical uses of this directive include reporting progress and/or errors in the UI,
         * starting a side process, logging, profiling and debugging, etc. The {@link
         * Receiver#accept}
         * method is called synchronously, which means its execution blocks the rest of the data
         * processing flow. If the flow is to cancel with {@linkplain RepositoryConfig#SEND_INTERRUPT
         * the interrupt signal}, the receiver may also see the signal.
         *
         * <p>The receiver does not have to use the input value, but if it does and it moves onto a
         * different thread for processing the input value, note that the data processing flow does
         * not
         * guarantee value immutability or concurrent access for this receiver. For this reason,
         * for
         * a
         * UI-calling receiver invoked from a background thread, implementation should extract any
         * necessary data from the input value, and post the immutable form of it to the main
         * thread
         * for
         * the UI calls, so the UI modifications are main-thread-safe while the data processing
         * flow
         * can
         * continue concurrently.
         *
         * <p>Note that the blocking semantics of this directive should not be taken as the
         * permission
         * to mutate the input in a way that affects the rest of the flow -- the appropriate
         * directive
         * for that purpose is {@code transform}, with a function that returns the same input
         * instance
         * after mutation.
         */
        /**
         * RSyncFlow 发送操作
         * 其实就是 添加一个 Receiver
         *
         * @param receiver 接受者
         * @return 返回一个 RSyncFlow 同步流状态
         *
         * 所有类型跟执行 sendTo 前的类型一样
         */
        @NonNull TSelf sendTo(@NonNull Receiver<? super TPre> receiver);

        /**
         * Send the input value and the value from the given supplier to the given binder, and then
         * pass
         * on the input value as the output of this directive, not modifying it.
         *
         * <p>The same usage notes for {@link #sendTo} apply to this directive.
         */
        /**
         * RSyncFlow 绑定操作
         * 需要
         * 传入一个 供应者，提供 TAdd 类型数据
         * 再传入一个 绑定者，进行上一个流的类型 TPre 数据 与 供应者提供的 TAdd 数据进行绑定
         *
         * @param secondValueSupplier 供应者
         * @param binder 绑定者
         * @param <TAdd> 供应者 的 目标类型
         * @return 返回一个 RSyncFlow 同步流状态
         *
         * 所有类型跟执行 bindWith 前的类型一样
         */
        @NonNull <TAdd> TSelf bindWith(@NonNull Supplier<TAdd> secondValueSupplier,
                                       @NonNull Binder<? super TPre, ? super TAdd> binder);

        /**
         * End the data processing flow but without using the output value and without notifying
         * the
         * registered {@link Updatable}s.
         */
        /**
         * RSyncFlow 快进操作
         * 什么也不做，直接快进到 RConfig 配置状态
         *
         * @return 返回一个 RConfig 配置状态
         *
         * TVal 类型 作为 RConfig 的仓库类型
         */
        @NonNull RConfig<TVal> thenSkip();

        /**
         * Perform the {@link #getFrom} directive and use the output value as the new value of the
         * compiled repository, with notification if necessary.
         */
        /**
         * RSyncFlow 快进获取数据操作
         * 可类型转换（ 供应者 ）
         *
         * @param supplier 供应者
         * @return 返回 一个 RConfig 配置状态
         *
         * TVal 类型 作为 RConfig 仓库数据类型
         */
        @NonNull RConfig<TVal> thenGetFrom(@NonNull Supplier<? extends TVal> supplier);

        /**
         * Perform the {@link #attemptGetFrom} directive and use the successful output value as the
         * new
         * value of the compiled repository, with notification if necessary.
         */

        /**
         * RSyncFlow 快进尝试获取数据操作
         * 可以截获异常，进行处理（ 供应者+Result ）
         *
         * @param attemptSupplier Result 供应者
         * @return 返回一个 RTermination 终止状态
         *
         * TVal 类型 作为 RTermination 仓库数据类型
         * Throwable 类型 作为 RTermination 流终止类型
         * RConfig<TVal> 类型 作为 RTermination 编译返回状态类型
         */
        // @NonNull RTermination<TVal, Throwable, RConfig<TVal>> thenAttemptGetFrom(
        //         @NonNull Supplier<? extends Result<? extends TVal>> attemptSupplier);

        @NonNull
        RTerminationOrContinue<TVal, Throwable, RConfig<TVal>,
            ? extends RSyncFlow<TVal, Throwable, ?>> thenAttemptGetFrom(
            @NonNull Supplier<? extends Result<? extends TVal>> attemptSupplier);

        /**
         * Perform the {@link #mergeIn} directive and use the output value as the new value of the
         * compiled repository, with notification if necessary.
         */

        /**
         * RSyncFlow 快进合并操作
         * 可类型转换（ 供应者 ）
         * 可以合并供应者转换后的数据 和 之前的流处理结果数据（ 合并者 ）
         *
         * @param supplier 供应者
         * @param merger 合并者
         * @param <TAdd> 供应者 的 目标类型
         * @return 返回 一个 RConfig 配置状态
         *
         * TVal 类型 作为 RConfig 仓库数据类型
         */
        @NonNull <TAdd> RConfig<TVal> thenMergeIn(@NonNull Supplier<TAdd> supplier,
                                                  @NonNull
                                                  Merger<? super TPre, ? super TAdd, ? extends TVal> merger);

        /**
         * Perform the {@link #attemptMergeIn} directive and use the successful output value as the
         * new
         * value of the compiled repository, with notification if necessary.
         */
        /**
         * RSyncFlow 快进尝试合并操作
         * 可类型转换（ 供应者 ）
         * 可以合并供应者转换后的数据 和 之前的流处理结果数据（ 合并者 ）
         * 可以截获异常，进行处理（ 合并者+Result ）
         *
         * @param supplier 供应者
         * @param attemptMerger 合并者
         * @param <TAdd> 供应者 的 目标类型
         * @return 返回 一个 RTermination 终止状态
         *
         * TVal 类型 作为 RTermination 仓库数据类型
         * Throwable 类型 作为 RTermination 流终止类型
         * RConfig<TVal> 类型 作为 RTermination 编译返回状态类型
         */
        // @NonNull <TAdd> RTermination<TVal, Throwable, RConfig<TVal>> thenAttemptMergeIn(
        //         @NonNull Supplier<TAdd> supplier,
        //         @NonNull Merger<? super TPre, ? super TAdd,
        //                 ? extends Result<? extends TVal>> attemptMerger);

        @NonNull
        <TAdd> RTerminationOrContinue<TVal, Throwable, RConfig<TVal>,
            ? extends RSyncFlow<TVal, Throwable, ?>> thenAttemptMergeIn(
            @NonNull Supplier<TAdd> supplier,
            @NonNull Merger<? super TPre, ? super TAdd,
                ? extends Result<? extends TVal>> attemptMerger);

        /**
         * Perform the {@link #transform} directive and use the output value as the new value of
         * the
         * compiled repository, with notification if necessary.
         */
        /**
         * RSyncFlow 快进转换操作
         * 用于类型转换
         *
         * @param function 转换方法
         * @return 返回 一个 RConfig 配置状态
         *
         * TVal 类型 作为 RConfig 仓库数据类型
         */
        @NonNull RConfig<TVal> thenTransform(
                @NonNull Function<? super TPre, ? extends TVal> function);

        /**
         * Perform the {@link #attemptTransform} directive and use the successful output value as
         * the
         * new value of the compiled repository, with notification if necessary.
         */
        /**
         * RSyncFlow 快进尝试转换操作
         * 用于类型转换
         * 可以截获异常，进行处理（ 转换方法+Result ）
         *
         * @param attemptFunction 转换方法
         * @return 返回 一个 RConfig 配置状态
         *
         * TVal 类型 作为 RTermination 仓库数据类型
         * Throwable 类型 作为 RTermination 流终止类型
         * RConfig<TVal> 类型 作为 RTermination 编译返回状态类型
         */
        // @NonNull RTermination<TVal, Throwable, RConfig<TVal>> thenAttemptTransform(
        //         @NonNull Function<? super TPre, ? extends Result<? extends TVal>> attemptFunction);
        @NonNull
        RTerminationOrContinue<TVal, Throwable, RConfig<TVal>,
            ? extends RSyncFlow<TVal, Throwable, ?>> thenAttemptTransform(
            @NonNull Function<? super TPre, ? extends Result<? extends TVal>> attemptFunction);
    }

    /**
     * Compiler state allowing to terminate the data processing flow following a failed check.
     *
     * @param <TVal> Value type of the repository.
     * @param <TTerm> Value type from which to terminate the flow.
     * @param <TRet> Compiler state to return to.
     *
     * RTermination 终止状态
     *
     * TVal 类型 作为 RTermination 仓库数据类型
     * TTerm 类型 作为 RTermination 流终止时的状态，一般为 Throwable
     * TRet 类型 作为 RTermination 编译返回的状态，只有两种状态（ RFlow（ RSyncFlow ）、RConfig ）
     */
    interface RTermination<TVal, TTerm, TRet> {

        /**
         * If the previous check failed, skip the rest of the data processing flow, and do not
         * notify
         * any registered {@link Updatable}s.
         */
        /**
         * RTermination 快进操作
         *
         * @return 只有可能返回 RFlow（ RSyncFlow ）、RConfig 状态
         */
        @NonNull TRet orSkip();

        /**
         * If the previous check failed, terminate the data processing flow and update the compiled
         * repository's value to the resulting value of applying the given function to the input of
         * this
         * termination clause, with notification if necessary.
         */
        /**
         * RTermination 结束状态
         * 其实又一次进行转换
         *
         * @param valueFunction 转换方法
         * @return 只有可能返回 RFlow（ RSyncFlow ）、RConfig 状态
         */
        @NonNull TRet orEnd(@NonNull Function<? super TTerm, ? extends TVal> valueFunction);
    }

    /**
     * Compiler state allowing to terminate or continue the data processing flow following a failed
     * attempt to produce the new value of the repository.
     *
     * @param <TVal> Value type of the repository.
     * @param <TTerm> Value type from which to terminate the flow.
     * @param <TRet> Compiler state to return to if the flow is terminated.
     * @param <TCon> Compiler state to return to if the flow is to continue.
     */

    interface RTerminationOrContinue<TVal, TTerm, TRet, TCon>
        extends RTermination<TVal, TTerm, TRet> {

        /**
         * If the previous attempt failed, continue with the rest of the data processing flow, using the
         * {@linkplain Result#getFailure() failure} as the input value to the next directive. Otherwise,
         * end the data processing flow and use the successful output value from the attempt as the new
         * value of the compiled repository, with notification if necessary.
         */
        @NonNull
        TCon orContinue();
    }

    /**
     * Compiler state allowing to configure and end the declaration of the repository.
     *
     * @param <TVal> Repository value type.
     *
     * RConfig 配置状态
     *
     * TVal 类型 作为 RConfig 仓库数据类型
     */
    interface RConfig<TVal> {

        /**
         * Specifies that this repository should notify the registered {@link Updatable}s if and
         * only if
         * the given {@code checker} returns {@link Boolean#TRUE}. Every time the data processing
         * flow
         * ends with a new repository value, the checker is called with the old repository value as
         * the
         * first argument and the new value the second. The return value determines whether this
         * update
         * should generate a notification. The default behavior is to notify of the update when the
         * new
         * value is different as per {@link Object#equals}.
         *
         * <p>Note that the {@code goLazy()} directive will always generate a notification, as a
         * preventative measure to handle a potentially different value which is unknown at the
         * time
         * of
         * {@code goLazy()}. Also, technically the {@link RepositoryConfig#RESET_TO_INITIAL_VALUE}
         * deactivation configuration would also update the repository value, and therefore the
         * {@code checker} will be consulted, but because the reset happens only when the
         * repository
         * is
         * deactivated, even if the checker returns true, there is no {@link Updatable} to receive
         * the
         * notification.
         */
        /**
         * RConfig 通知操作
         * 会传入一个 合并者，合并出一个 Boolean 类型
         *
         * @param checker 合并者
         * @return 返回一个 RConfig 状态
         *
         * TVal 类型 作为 RConfig 仓库数据类型
         */
        @NonNull RConfig<TVal> notifyIf(
                @NonNull Merger<? super TVal, ? super TVal, Boolean> checker);

        /**
         * Specifies the behaviors when this repository is deactivated, i.e. from being observed to
         * not
         * being observed. The default behavior is {@link RepositoryConfig#CONTINUE_FLOW}.
         *
         * @param deactivationConfig A bitwise combination of the constants in {@link
         * RepositoryConfig}.
         */
        /**
         * RConfig 失效时的操作（ 一些特殊的行为：导致仓库失效，从观察状态变为不观察状态 ）
         *
         * @param deactivationConfig RepositoryConfig 类型
         * @return 返回一个 RConfig 配置状态
         *
         * TVal 类型 作为 RConfig 仓库数据类型
         */
        @NonNull RConfig<TVal> onDeactivation(@RepositoryConfig int deactivationConfig);

        /**
         * Specifies the behaviors when an update is observed from an event source while a data
         * processing flow is ongoing. The default behavior is {@link RepositoryConfig#CONTINUE_FLOW}.
         *
         * @param concurrentUpdateConfig A bitwise combination of the constants in
         * {@link RepositoryConfig}.
         */
        /**
         * RConfig 并发更新时（ 一些特殊的行为：一个观察者从事件源中被观察了，然后一个数据处理流还在运行 ）
         *
         * @param concurrentUpdateConfig RepositoryConfig 类型
         * @return 返回一个 RConfig 配置状态
         *
         * TVal 类型 作为 RConfig 仓库数据类型
         */
        @NonNull RConfig<TVal> onConcurrentUpdate(@RepositoryConfig int concurrentUpdateConfig);

        /**
         * Compiles a {@link Repository} that exhibits the previously defined behaviors.
         */
        /**
         * RConfig 完成操作
         *
         * @return 返回一个 仓库 Repository
         *
         * TVal 类型 作为 Repository 仓库数据类型
         */
        @NonNull Repository<TVal> compile();

        /**
         * Compiles a repository that exhibits the previously defined behaviors, and starts
         * compiling
         * a new repository with the given initial value (which can be of a different type) that
         * uses
         * the former repository as the first event source and the first data source.
         *
         * <p>This method provides a shortcut for the following code:
         *
         * <pre>
         * {@literal Repository<TVal>} subRepository = ….compile();
         * {@literal Repository<TVal2>} mainRepository = repositoryWithInitialValue(value)
         *     .observe(subRepository)
         *     .… // additional event sources and frequency which can be defined after this method
         *     .getFrom(subRepository) // first directive
         *     .… // rest of data processing flow, configuration, compile()
         * }</pre>
         *
         * The repository compiled by this method (the {@code subRepository}) therefore acts as a
         * buffer for the next repository to compile (the {@code mainRepository}), with its own
         * event
         * sources and data processing flow. This simplifies or shortens the flow of the new
         * repository,
         * and is typically useful if different parts of the overall data processing flow depend on
         * different event sources and data sources, and it is beneficial to cache the intermediate
         * values between parts.
         *
         * <p>However, due to the {@code getFrom} directive at the start of this new data
         * processing
         * flow, the next repository to compile has no access to its previous value. Additionally,
         * the
         * former repository is not exposed anywhere else. If this is undesirable, consider using
         * the
         * full form, where the former repository is explicitly compiled.
         */
        /**
         * RConfig 重构操作
         * 重新将一个仓库编译过程后得到 的  RConfig 状态，还有最终目标数据 TVal
         * 作为重构仓库的材料，会直接跳到 仓库类型为 TVal2 的编译状态中的
         * RFrequency 频率状态
         *
         * @param value 新仓库的目标数据类型初始值
         * @param <TVal2> 新仓库的目标数据类型
         * @return 新仓库编译状态中的 RFrequency 频率状态
         */
        @NonNull <TVal2> RFrequency<TVal2, TVal> compileIntoRepositoryWithInitialValue(
                @NonNull TVal2 value);
    }
}
