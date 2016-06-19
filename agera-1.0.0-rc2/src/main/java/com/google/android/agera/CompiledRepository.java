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

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.Executor;

import static com.google.android.agera.Observables.compositeObservable;
import static com.google.android.agera.Preconditions.checkNotNull;
import static com.google.android.agera.Preconditions.checkState;
import static com.google.android.agera.RepositoryConfig.CANCEL_FLOW;
import static com.google.android.agera.RepositoryConfig.RESET_TO_INITIAL_VALUE;
import static com.google.android.agera.RepositoryConfig.SEND_INTERRUPT;
import static com.google.android.agera.WorkerHandler.MSG_CALL_ACKNOWLEDGE_CANCEL;
import static com.google.android.agera.WorkerHandler.MSG_CALL_MAYBE_START_FLOW;
import static com.google.android.agera.WorkerHandler.workerHandler;
import static java.lang.Thread.currentThread;

@SuppressWarnings({ "rawtypes", "unchecked" })
final class CompiledRepository extends BaseObservable
    implements Repository, Updatable, Runnable {

    @NonNull
    static Repository compiledRepository(
        @NonNull final Object initialValue,
        @NonNull final List<Observable> eventSources,
        final int frequency,
        @NonNull final List<Object> directives,
        @NonNull final Merger<Object, Object, Boolean> notifyChecker,
        @RepositoryConfig final int concurrentUpdateConfig,
        @RepositoryConfig final int deactivationConfig) {
        final Object[] directiveArray = directives.toArray();
        return new CompiledRepository(initialValue, compositeObservable(frequency,
            eventSources.toArray(new Observable[eventSources.size()])),
            directiveArray, notifyChecker, deactivationConfig, concurrentUpdateConfig);
    }

    //region Invariants

    // 初始值
    @NonNull
    private final Object initialValue;

    // 事件源
    @NonNull
    private final Observable eventSource;

    // 所有指令
    @NonNull
    private final Object[] directives;

    /*
     * 实质上为一个 ObjectsUnequalMerger
     * 用于判断 传入的两个流数据 是否 相等
     * 判断后 合并出对应的 true or false
     */
    @NonNull
    private final Merger<Object, Object, Boolean> notifyChecker;

    // 失效配置
    @RepositoryConfig
    private final int deactivationConfig;

    // 并发更新配置
    @RepositoryConfig
    private final int concurrentUpdateConfig;

    // WorkerHandler
    @NonNull
    private final WorkerHandler workerHandler;


    CompiledRepository(
        @NonNull final Object initialValue,
        @NonNull final Observable eventSource,
        @NonNull final Object[] directives,
        @NonNull final Merger<Object, Object, Boolean> notifyChecker,
        @RepositoryConfig final int deactivationConfig,
        @RepositoryConfig final int concurrentUpdateConfig) {
        this.initialValue = initialValue;
        this.currentValue = initialValue;
        this.intermediateValue = initialValue; // non-final field but with @NonNull requirement
        this.eventSource = eventSource;
        this.directives = directives;
        this.notifyChecker = notifyChecker;
        this.deactivationConfig = deactivationConfig;
        this.concurrentUpdateConfig = concurrentUpdateConfig;
        this.workerHandler = workerHandler();
    }

    //endregion Invariants

    //region Data processing flow states


    /**
     * 运行状态
     *
     * IDLE：闲置
     * RUNNING：运行
     * CANCEL_REQUESTED：取消请求
     * PAUSED_AT_GO_TO：子线程暂停
     * PAUSED_AT_GO_LAZY：懒加载暂停
     * RUNNING_LAZILY：懒加载运行
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ IDLE, RUNNING, CANCEL_REQUESTED, PAUSED_AT_GO_TO, PAUSED_AT_GO_LAZY, RUNNING_LAZILY })
    private @interface RunState {}


    private static final int IDLE = 0;
    private static final int RUNNING = 1;
    private static final int CANCEL_REQUESTED = 2;
    private static final int PAUSED_AT_GO_TO = 3;
    private static final int PAUSED_AT_GO_LAZY = 4;
    private static final int RUNNING_LAZILY = 5;

    // 记录 运行状态，默认为：闲置
    @RunState
    private int runState = IDLE;

    // 记录 是否需要重启
    private boolean restartNeeded;

    /*
     * 最后一个 goTo()/goLazy() 指令的 index
     * 如果重启，则为 -1 或者 其他指令
     */
    /** Index of the last goTo()/goLazy() directive, for resuming, or -1 for other directives. */
    private int lastDirectiveIndex = -1;

    // 当前值
    /** The current value to be exposed through the repository's get method. */
    @NonNull
    private Object currentValue;

    // 中间值
    /** The intermediate value computed by the executed part of the flow. */
    @NonNull
    private Object intermediateValue;

    // 当前线程
    /** The thread currently running a directive that can be interrupted. */
    @Nullable
    private Thread currentThread;

    //endregion Data processing flow states

    //region Starting and requesting cancellation
    // - All methods in this region are called from the Worker Looper thread, but reading and writing
    //   states that might be accessed from a different thread are still synchronized.


    /**
     * 如果身为 BaseObservable ，被观察者 被激活时（ 成功添加一个观察者后 ）
     * 为 事件源 添加 观察者，为 此 CompiledRepository
     * 然后执行 maybeStartFlow()
     */
    @Override
    protected void observableActivated() {
        eventSource.addUpdatable(this);
        maybeStartFlow();
    }


    /**
     * 如果身为 BaseObservable ，被观察者 被失效时（ 成功删除一个观察者后 ）
     * 为 事件源 删除 观察者，为 此 CompiledRepository
     * 然后执行 maybeCancelFlow(...)
     */
    @Override
    protected void observableDeactivated() {
        eventSource.removeUpdatable(this);
        maybeCancelFlow(deactivationConfig, false);
    }


    /**
     * 同时调用
     * 1. 可能取消流
     * 2. 可能开始流
     */
    @Override
    public void update() {
        maybeCancelFlow(concurrentUpdateConfig, true);
        maybeStartFlow();
    }


    /**
     * Called on the worker looper thread. Starts the data processing flow if it's not running.
     * This
     * also cancels the lazily-executed part of the flow if the run state is "paused at lazy".
     *
     * 如果 运行状态为
     * 1. 闲置
     * 2. 懒加载暂停
     *
     * 那么重置状态为：运行
     * 中间值存放当前值
     * 开始运行 流 runFlowFrom(...)
     */
    void maybeStartFlow() {
        synchronized (this) {
            if (runState == IDLE || runState == PAUSED_AT_GO_LAZY) {
                runState = RUNNING;
                lastDirectiveIndex = -1; // this could be pointing at the goLazy directive
                restartNeeded = false;
            } else {
                return; // flow already running, do not continue.
            }
        }
        intermediateValue = currentValue;
        runFlowFrom(0, false);
    }


    /**
     * Called on the worker looper thread. Depending on the {@code config}, cancels the data
     * processing flow, resets the value, and/or sends the interrupt signal to the thread currently
     * processing a getFrom/mergeIn/transform instruction of the flow.
     *
     * @param scheduleRestart Whether to schedule a restart if a current flow is canceled.
     *
     * 如果 运行状态为
     * 1. 运行
     * 2. 子线程暂停
     * 3. 并且，仓库配置不为 CONTINUE_FLOW
     *
     * 那么重置状态为：取消请求
     *
     * 4. 如果，仓库配置为 SEND_INTERRUPT，并且当前线程 存在，那么线程停止
     * 5. 如果，仓库配置为 RESET_TO_INITIAL_VALUE，并且 不重启，那么重置到初始值
     */
    private void maybeCancelFlow(
        @RepositoryConfig final int config, final boolean scheduleRestart) {
        synchronized (this) {
            if (runState == RUNNING || runState == PAUSED_AT_GO_TO) {
                restartNeeded = scheduleRestart;

                // If config forbids cancellation, exit now after scheduling the restart, to skip the
                // cancellation request.
                if ((config & CANCEL_FLOW) == 0) {
                    return;
                }

                runState = CANCEL_REQUESTED;

                if ((config & SEND_INTERRUPT) == SEND_INTERRUPT && currentThread != null) {
                    currentThread.interrupt();
                }
            }

            // Resetting to the initial value should be done even if the flow is not running.
            if (!scheduleRestart && (config & RESET_TO_INITIAL_VALUE) == RESET_TO_INITIAL_VALUE) {
                setNewValueLocked(initialValue);
            }
        }
    }

    //endregion Starting and requesting cancellation

    //region Acknowledging cancellation and restarting
    // - Apart from handleMessage(), other methods in this region can be called from a thread that is
    //   not the Worker Looper thread.


    /**
     * Checks if the current data processing flow has been requested cancellation. Acknowledges the
     * request if so. This must be called while locked in a synchronized context.
     *
     * @return Whether the data processing flow is cancelled.
     *
     * 如果是 取消请求 状态
     * 则调用 CompiledRepository.acknowledgeCancel()
     */
    private boolean checkCancellationLocked() {
        if (runState == CANCEL_REQUESTED) {
            workerHandler.obtainMessage(MSG_CALL_ACKNOWLEDGE_CANCEL, this).sendToTarget();
            return true;
        }
        return false;
    }


    /**
     * Called by the worker handler.
     *
     * 如果是 取消请求 状态
     * 然后：
     * 1. 将运行状态 设置为 闲置状态
     * 2. 中间值赋上初始值
     * 3. 标记为需要重启
     *
     * 然后开始重启流 maybeStartFlow()
     */
    void acknowledgeCancel() {
        boolean shouldStartFlow = false;
        synchronized (this) {
            if (runState == CANCEL_REQUESTED) {
                runState = IDLE;
                intermediateValue
                    = initialValue; // GC the intermediate value but keep field non-null.
                shouldStartFlow = restartNeeded;
            }
        }
        if (shouldStartFlow) {
            maybeStartFlow();
        }
    }


    /**
     * Checks if the data processing flow needs restarting, and restarts it if so. This must be
     * called
     * while locked in a synchronized context and after the previous data processing flow has
     * completed.
     *
     * 如果 已经标记了 需要重启
     * 那么调用 CompiledRepository.maybeStartFlow()
     */
    private void checkRestartLocked() {
        if (restartNeeded) {
            workerHandler.obtainMessage(MSG_CALL_MAYBE_START_FLOW, this).sendToTarget();
        }
    }

    //endregion Acknowledging cancellation and restarting

    //region Running directives
    // The directive creation methods are interleaved here so the index-to-operator relation is clear.

    private static final int END = 0;
    private static final int GET_FROM = 1;
    private static final int MERGE_IN = 2;
    private static final int TRANSFORM = 3;
    private static final int CHECK = 4;
    private static final int GO_TO = 5;
    private static final int GO_LAZY = 6;
    private static final int SEND_TO = 7;
    private static final int BIND = 8;
    private static final int FILTER_SUCCESS = 9;


    /**
     * @param asynchronously Whether this flow is run asynchronously. True after the first goTo and
     * before goLazy. This is to omit unnecessarily locking the synchronized context to check for
     * cancellation, because if the flow is run synchronously, cancellation requests theoretically
     * cannot be delivered here.
     *
     * 获得所有 操作符指令 的 数组
     * 开始遍历数组
     * 1. 检查是否取消请求，是的话，直接退出循环
     * 2. 子线程加载，调用 setPausedAtGoToLocked(...) ，从 子线程暂停 到 恢复 流执行
     * 3. 懒加载
     *
     * 接着：
     * 1. 检查运行状态是否是 取消请求，是的话 break
     * 2. 如果是子线程执行的话，setPausedAtGoToLocked(...)
     * 3. 如果是懒加载的话，setLazyAndEndFlowLocked(...)
     *
     * 最后：
     * 分发指令，运行 不同的 操作符操作
     */
    private void runFlowFrom(final int index, final boolean asynchronously) {
        final Object[] directives = this.directives;
        final int length = directives.length;
        int i = index;
        while (0 <= i && i < length) {
            final int directiveType = (Integer) directives[i];
            if (asynchronously || directiveType == GO_TO || directiveType == GO_LAZY) {
                // Check cancellation before running the next directive. This needs to be done while locked.
                // For goTo and goLazy, because they need to change the states and suspend the flow, they
                // need the lock and are therefore treated specially here.
                synchronized (this) {
                    if (checkCancellationLocked()) {
                        break;
                    }
                    if (directiveType == GO_TO) {
                        setPausedAtGoToLocked(i);
                        // the actual executor delivery is done below, outside the lock, to eliminate any
                        // deadlock possibility.
                    } else if (directiveType == GO_LAZY) {
                        setLazyAndEndFlowLocked(i);
                        return;
                    }
                }
            }

            // A table-switch on a handful of options is a good compromise in code size and runtime
            // performance comparing to a full-fledged double-dispatch pattern with subclasses.
            switch (directiveType) {
                case GET_FROM:
                    i = runGetFrom(directives, i);
                    break;
                case MERGE_IN:
                    i = runMergeIn(directives, i);
                    break;
                case TRANSFORM:
                    i = runTransform(directives, i);
                    break;
                case CHECK:
                    i = runCheck(directives, i);
                    break;
                case GO_TO:
                    i = runGoTo(directives, i);
                    break;
                case SEND_TO:
                    i = runSendTo(directives, i);
                    break;
                case BIND:
                    i = runBindWith(directives, i);
                    break;
                case FILTER_SUCCESS:
                    i = runFilterSuccess(directives, i);
                    break;
                case END:
                    i = runEnd(directives, i);
                    break;
                // Missing GO_LAZY but it has already been dealt with in the synchronized block above.
            }
        }
    }


    /**
     * 添加 getFrom 指令
     * 到 指令集合 里
     *
     * getFrom 所在集合的位置，后面紧跟着
     * getFrom 需要的 Supplier
     *
     * @param supplier getFrom 需要的 Supplier
     * @param directives 指令集合
     */
    static void addGetFrom(@NonNull final Supplier supplier,
                           @NonNull final List<Object> directives) {
        directives.add(GET_FROM);
        directives.add(supplier);
    }


    /**
     * 开始 运行 getFrom(...) 操作符
     *
     * @param directives 指令集合
     * @param index 索引
     * @return 下个指令，由于一个指令，后紧跟着，指令需要的角色（ Supplier，Function，Merger... ）
     * 所以要 +2
     */
    private int runGetFrom(@NonNull final Object[] directives, final int index) {
        final Supplier supplier = (Supplier) directives[index + 1];
        intermediateValue = checkNotNull(supplier.get());
        return index + 2;
    }


    /**
     * 添加 mergeIn 指令
     * 到 指令集合 里
     *
     * mergeIn 所在集合的位置，后面紧跟着
     * 1. mergeIn 需要的 Supplier
     * 2. mergeIn 需要的 Merger
     *
     * @param supplier mergeIn 需要的 Supplier
     * @param merger mergeIn 需要的 Merger
     * @param directives 指令集合
     */
    static void addMergeIn(@NonNull final Supplier supplier, @NonNull final Merger merger,
                           @NonNull final List<Object> directives) {
        directives.add(MERGE_IN);
        directives.add(supplier);
        directives.add(merger);
    }


    /**
     * 开始 运行 mergeIn(...) 操作符
     *
     * @param directives 指令集合
     * @param index 索引
     * @return 下个指令，由于一个指令，后紧跟着，指令需要的角色（ Supplier，Function，Merger... ）
     * 需要两个角色
     * 所以要 +3
     */
    private int runMergeIn(@NonNull final Object[] directives, final int index) {
        final Supplier supplier = (Supplier) directives[index + 1];
        final Merger merger = (Merger) directives[index + 2];
        intermediateValue = checkNotNull(merger.merge(intermediateValue, supplier.get()));
        return index + 3;
    }


    /**
     * 添加 transform 指令
     * 到 指令集合 里
     *
     * transform 所在集合的位置，后面紧跟着
     * transform 需要的 Function
     *
     * @param function transform 需要的 Function
     * @param directives 指令集合
     */
    static void addTransform(@NonNull final Function function,
                             @NonNull final List<Object> directives) {
        directives.add(TRANSFORM);
        directives.add(function);
    }


    /**
     * 开始 运行 transform(...) 操作符
     *
     * @param directives 指令集合
     * @param index 索引
     * @return 下个指令，由于一个指令，后紧跟着，指令需要的角色（ Supplier，Function，Merger... ）
     * 所以要 +2
     */
    private int runTransform(@NonNull final Object[] directives, final int index) {
        final Function function = (Function) directives[index + 1];
        intermediateValue = checkNotNull(function.apply(intermediateValue));
        return index + 2;
    }


    /**
     * 添加 check 指令
     * 到 指令集合 里
     *
     * check 所在集合的位置，后面紧跟着
     * 1. check 需要的 caseFunction
     * 2. check 需要的 casePredicate
     * 3. check 需要的 terminatingValueFunction
     *
     * @param caseFunction check 需要的 caseFunction
     * @param casePredicate check 需要的 casePredicate
     * @param terminatingValueFunction check 需要的 terminatingValueFunction
     * @param directives 指令集合
     */
    static void addCheck(@NonNull final Function caseFunction,
                         @NonNull final Predicate casePredicate,
                         @Nullable final Function terminatingValueFunction,
                         @NonNull final List<Object> directives) {
        directives.add(CHECK);
        directives.add(caseFunction);
        directives.add(casePredicate);
        directives.add(terminatingValueFunction);
    }


    /**
     * 开始 运行 check(...) 操作符
     *
     * @param directives 指令集合
     * @param index 索引
     * @return 下个指令，由于一个指令，后紧跟着，指令需要的角色（ Supplier，Function，Merger... ）
     * 需要三个角色
     * 如果断定
     * 成功了 +4，因为有三个角色
     * 成功了返回 -1，表示终止，并且运行 runTerminate(...)
     */
    private int runCheck(@NonNull final Object[] directives, final int index) {
        final Function caseFunction = (Function) directives[index + 1];
        final Predicate casePredicate = (Predicate) directives[index + 2];
        final Function terminatingValueFunction = (Function) directives[index + 3];

        final Object caseValue = caseFunction.apply(intermediateValue);
        if (casePredicate.apply(caseValue)) {
            return index + 4;
        } else {
            runTerminate(caseValue, terminatingValueFunction);
            return -1;
        }
    }


    /**
     * 添加 goTo 指令
     * 到 指令集合 里
     *
     * goTo 所在集合的位置，后面紧跟着
     * goTo 需要的 Executor
     *
     * @param executor goTo 需要的 Executor
     * @param directives 指令集合
     */
    static void addGoTo(@NonNull final Executor executor, @NonNull final List<Object> directives) {
        directives.add(GO_TO);
        directives.add(executor);
    }


    /**
     * 开始 运行 goTo(...) 操作符
     *
     * @param directives 指令集合
     * @param index 索引
     * @return 返回 -1 终止
     */
    private int runGoTo(@NonNull final Object[] directives, final int index) {
        Executor executor = (Executor) directives[index + 1];
        executor.execute(this);
        return -1;
    }


    /**
     * 继续执行 goTo 后面的 指令
     * 目前只在本类使用
     *
     * @param directives 指令集合
     * @param index goTo 的 指令 index
     * @return goTo 的下一个指令 的 index
     */
    private static int continueFromGoTo(@NonNull final Object[] directives, final int index) {
        checkState(directives[index].equals(GO_TO), "Inconsistent directive state for goTo");
        return index + 2;
    }


    /**
     * 添加 goLazy 指令
     * 到 指令集合 里
     *
     * @param directives 指令集合
     */
    static void addGoLazy(@NonNull final List<Object> directives) {
        directives.add(GO_LAZY);
    }


    /**
     * 继续执行 goLazy 后面的 指令
     * 目前只在本类使用
     *
     * @param directives 指令集合
     * @param index goLazy 的 指令 index
     * @return goLazy 的下一个指令 的 index
     */
    private static int continueFromGoLazy(@NonNull final Object[] directives, final int index) {
        checkState(directives[index].equals(GO_LAZY), "Inconsistent directive state for goLazy");
        return index + 1;
    }


    /**
     * 添加 sendTo 指令
     * 到 指令集合 里
     *
     * sendTo 所在集合的位置，后面紧跟着
     * sendTo 需要的 Receiver
     *
     * @param receiver goTo 需要的 Receiver
     * @param directives 指令集合
     */
    static void addSendTo(
        @NonNull final Receiver receiver, @NonNull final List<Object> directives) {
        directives.add(SEND_TO);
        directives.add(receiver);
    }


    /**
     * 开始 运行 sendTo(...) 操作符
     *
     * @param directives 指令集合
     * @param index 索引
     * @return 下个指令，由于一个指令，后紧跟着，指令需要的角色（ Supplier，Function，Merger... ）
     * 所以要 +2
     */
    private int runSendTo(@NonNull final Object[] directives, final int index) {
        Receiver receiver = (Receiver) directives[index + 1];
        receiver.accept(intermediateValue);
        return index + 2;
    }


    /**
     * 添加 bindWith 指令
     * 到 指令集合 里
     *
     * bindWith 所在集合的位置，后面紧跟着
     * 1. bindWith 需要的 Supplier
     * 2. bindWith 需要的 Receiver
     *
     * @param supplier bindWith 需要的 Supplier
     * @param binder bindWith 需要的 Binder
     * @param directives 指令集合
     */
    static void addBindWith(@NonNull final Supplier supplier, @NonNull final Binder binder,
                            @NonNull final List<Object> directives) {
        directives.add(BIND);
        directives.add(supplier);
        directives.add(binder);
    }


    /**
     * 开始 运行 bindWith(...) 操作符
     *
     * @param directives 指令集合
     * @param index 索引
     * @return 下个指令，由于一个指令，后紧跟着，指令需要的角色（ Supplier，Function，Merger... ）
     * 需要两个角色
     * 所以要 +3
     */
    private int runBindWith(@NonNull final Object[] directives, final int index) {
        final Supplier supplier = (Supplier) directives[index + 1];
        final Binder binder = (Binder) directives[index + 2];
        binder.bind(intermediateValue, supplier.get());
        return index + 3;
    }


    /**
     * 添加 filterSuccess 指令
     * 到 指令集合 里
     *
     * filterSuccess 所在集合的位置，后面紧跟着
     * filterSuccess 需要的 terminatingValueFunction
     *
     * @param terminatingValueFunction filterSuccess 需要的 terminatingValueFunction
     * @param directives 指令集合
     */
    static void addFilterSuccess(
        @Nullable final Function terminatingValueFunction, @NonNull final List<Object> directives) {
        directives.add(FILTER_SUCCESS);
        directives.add(terminatingValueFunction);
    }


    /**
     * 开始 运行 filterSuccess(...) 操作符
     *
     * @param directives 指令集合
     * @param index 索引
     * @return 下个指令，由于一个指令，后紧跟着，指令需要的角色（ Supplier，Function，Merger... ）
     * 需要一个角色
     * 判断结果是否
     * 成功，+2，因为有一个角色
     * 失败，返回 -1，终止
     */
    private int runFilterSuccess(@NonNull final Object[] directives, final int index) {
        final Function terminatingValueFunction = (Function) directives[index + 1];
        final Result tryValue = (Result) intermediateValue;
        if (tryValue.succeeded()) {
            intermediateValue = tryValue.get();
            return index + 2;
        } else {
            runTerminate(tryValue.getFailure(), terminatingValueFunction);
            return -1;
        }
    }


    /**
     * 终止状态
     *
     * @param terminatingValueFunction 结束时的转换方法
     */
    private void runTerminate(@NonNull final Object caseValue,
                              @Nullable final Function terminatingValueFunction) {
        if (terminatingValueFunction == null) {
            skipAndEndFlow();
        } else {
            setNewValueAndEndFlow(checkNotNull(terminatingValueFunction.apply(caseValue)));
        }
    }


    /**
     * 添加 end 指令
     * 到 指令集合 里
     *
     * end 所在集合的位置，后面紧跟着
     * end 需要的 ship 标识
     *
     * @param skip end 需要的 ship 标识
     * @param directives 指令集合
     */
    static void addEnd(final boolean skip, @NonNull final List<Object> directives) {
        directives.add(END);
        directives.add(skip);
    }


    /**
     * 开始 运行 end(...) 操作符
     *
     * @param directives 指令集合
     * @param index 索引
     * @return 返回 -1，终止
     */
    private int runEnd(@NonNull final Object[] directives, final int index) {
        final boolean skip = (Boolean) directives[index + 1];
        if (skip) {
            skipAndEndFlow();
        } else {
            setNewValueAndEndFlow(intermediateValue);
        }
        return -1;
    }

    //endregion Running directives

    //region Completing, pausing and resuming flow


    /**
     * 快进 并 结束 流
     *
     * 设置 运行状态 为 闲置
     * 中间值 设置为 初始值
     *
     * 调用 checkRestartLocked()
     * 如果 已经标记了 需要重启
     * 那么调用 CompiledRepository.maybeStartFlow()
     */
    private synchronized void skipAndEndFlow() {
        runState = IDLE;
        intermediateValue
            = initialValue; // GC the intermediate value but field must be kept non-null.
        checkRestartLocked();
    }


    /**
     * 设置 新值 并 结束 流
     *
     * 1. 检查 运行状态 是否是 懒加载运行，记录为 wasRunningLazily
     * 2. 设置 运行状态 为 闲置
     * 3. 中间值 设置为 初始值
     * 4. 如果 wasRunningLazily 记录 为 true，当前值 设置为 新值
     * 5. 如果 wasRunningLazily 记录 为 false，调用 setNewValueLocked(newValue)
     * 6. 调用 checkRestartLocked()
     * 如果 已经标记了 需要重启
     * 那么调用 CompiledRepository.maybeStartFlow()
     *
     * @param newValue 新值
     */
    private synchronized void setNewValueAndEndFlow(@NonNull final Object newValue) {
        final boolean wasRunningLazily = runState == RUNNING_LAZILY;
        runState = IDLE;
        intermediateValue
            = initialValue; // GC the intermediate value but field must be kept non-null.
        if (wasRunningLazily) {
            currentValue = newValue; // Don't notify if this new value is produced lazily
        } else {
            setNewValueLocked(newValue); // May notify otherwise
        }
        checkRestartLocked();
    }


    /**
     * notifyChecker 实际上是一个 ObjectsUnequalMerger 对象
     * 对象不相等 合并者，用于比较
     *
     * 1. 判断 新值 与 当前值 是否 相等，记录为 shouldNotify
     * 2. 设置 当前值 等于 新值
     * 3. 如果 shouldNotify 记录 为 true，dispatchUpdate() 通知所有观察者
     *
     * @param newValue 新值
     */
    private void setNewValueLocked(@NonNull final Object newValue) {
        final boolean shouldNotify = notifyChecker.merge(currentValue, newValue);
        currentValue = newValue;
        if (shouldNotify) {
            dispatchUpdate();
        }
    }


    /**
     * goTo 指令，设置 暂停
     *
     * 1. lastDirectiveIndex 设置为 goTo 指令的 index
     * 2. 设置 运行状态 为 子线程暂停
     *
     * @param resumeIndex goTo 指令的 index
     */
    private void setPausedAtGoToLocked(final int resumeIndex) {
        lastDirectiveIndex = resumeIndex;
        runState = PAUSED_AT_GO_TO;
    }

    /** Called from the executor of a goTo instruction to continue processing. */
    /**
     * CompiledRepository 也作为 Runnable 这个角色
     *
     * 1. 保存 最后指令 lastDirectiveIndex 到 index
     * 2. 检查 当前 运行状态：必须是 子线程暂停 或者 取消请求，不然报错
     * 3. 设置 lastDirectiveIndex = -1
     * 4. checkCancellationLocked()，
     * 如果是 取消请求 状态
     * 则调用 CompiledRepository.acknowledgeCancel()
     *
     * 然后返回
     *
     * 5. 设置 运行状态 为 运行
     * 6. 记录 当前线程
     * 7. runFlowFrom(continueFromGoTo(directives, index), true); 从 暂停 -> 恢复 流
     * 8. Thread.interrupted(); 线程中断
     * 9. 如果 执行 6 了，那么最后要，清空 当前线程的引用
     */
    @Override
    public void run() {
        final Thread myThread = currentThread();
        final int index;
        synchronized (this) {
            index = lastDirectiveIndex;
            checkState(runState == PAUSED_AT_GO_TO || runState == CANCEL_REQUESTED,
                "Illegal call of Runnable.run()");
            lastDirectiveIndex = -1;

            if (checkCancellationLocked()) {
                return;
            }
            runState = RUNNING;
            // allow thread interruption (set this when still holding the lock)
            currentThread = myThread;
        }
        // leave the synchronization lock to run the rest of the flow
        runFlowFrom(continueFromGoTo(directives, index), true);
        // consume any unconsumed interrupted flag
        Thread.interrupted();
        // disallow interrupting the current thread, but chances are the next directive has started
        // asynchronously, so check currentThread is still this thread. This also works if a goTo
        // directive is given a synchronous executor, in which case the next part of the flow will
        // have been completed by now and currentThread will have been reset by that invocation of
        // runFlowFrom().
        synchronized (this) {
            if (currentThread == myThread) {
                currentThread = null;
            }
        }
    }


    /**
     * 设置 lazy 状态 并 结束 流
     *
     * 1. lastDirectiveIndex 设置为 goLazy 指令的 index
     * 2. 设置 运行状态 为 懒加载暂停
     * 3. 通知所有观察者 dispatchUpdate()
     * 4. checkRestartLocked()
     * 如果 已经标记了 需要重启
     * 那么调用 CompiledRepository.maybeStartFlow()
     *
     * @param resumeIndex goLazy 指令的 index
     */
    private void setLazyAndEndFlowLocked(final int resumeIndex) {
        lastDirectiveIndex = resumeIndex;
        runState = PAUSED_AT_GO_LAZY;
        dispatchUpdate();
        checkRestartLocked();
    }


    /**
     * 获取 CompiledRepository 作为 仓库 角色 时
     * 保存的 仓库数据
     *
     * 1. 如果 当前 运行状态 是 懒加载暂停
     * 2. index = lastDirectiveIndex，拿到最后执行指令的 index
     * 3. 运行状态 设置为 懒加载运行
     * 4. runFlowFrom(...) 恢复流，继续执行，内部会对 currentValue 进行赋值
     *
     * 5. 返回 当前值/数据 ( currentValue )
     *
     * @return 仓库数据
     */
    @NonNull
    @Override
    public synchronized Object get() {
        if (runState == PAUSED_AT_GO_LAZY) {
            final int index = lastDirectiveIndex;
            runState = RUNNING_LAZILY;
            runFlowFrom(continueFromGoLazy(directives, index), false);
        }
        return currentValue;
    }

    //endregion Completing, pausing and resuming flow
}
