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
import android.support.annotation.Nullable;

import static com.google.android.agera.Preconditions.checkArgument;
import static com.google.android.agera.Preconditions.checkNotNull;
import static com.google.android.agera.Preconditions.checkState;

/**
 * An immutable object encapsulating the result of an <i>attempt</i>. An attempt is a call to
 * {@link Function#apply}, {@link Merger#merge} or {@link Supplier#get} that may fail. This class
 * helps avoid throwing exceptions from those methods, by encapsulating either the output value of
 * those calls, or the failure encountered. In this way, an attempt always produces a {@link
 * Result}
 * whether it has {@link #succeeded} or {@link #failed}.
 *
 * <p>This class can also be used to wrap a nullable value for situations where the value is indeed
 * null, but null is not accepted. In this case a {@link Result} instance representing a failed
 * attempt to obtain a non-null value can be used in place of the nullable value.
 *
 * @param <T> The output value type.
 *
 * Agera 内 抽象出来的 结果
 */
public final class Result<T> {
    // 缺少参数的 错误 Result
    @NonNull
    private static final Result<Object> ABSENT ;

    // 通用的 错误 Result
    @NonNull
    private static final Result<Object> FAILURE ;

    // 缺少参数错误
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    @NonNull
    private static final Throwable ABSENT_THROWABLE ;

    static {
        final Throwable failureThrowable = new Throwable("Attempt failed");
        failureThrowable.setStackTrace(new StackTraceElement[0]);
        FAILURE = new Result<>(null, failureThrowable);
        ABSENT_THROWABLE = new NullPointerException("Value is absent");
        ABSENT_THROWABLE.setStackTrace(new StackTraceElement[0]);
        ABSENT = new Result<>(null, ABSENT_THROWABLE);
    }

    // 存储结果
    @Nullable
    private final T value;

    // 记录异常
    @Nullable
    private final Throwable failure;


    Result(@Nullable final T value, @Nullable final Throwable failure) {
        checkArgument(value != null ^ failure != null, "Illegal Result arguments");
        this.value = value;
        this.failure = failure;
    }


    /**
     * Creates a {@link Result} of a successful attempt that produced the given {@code value}.
     *
     * 构造 成功 Result
     */
    @NonNull
    public static <T> Result<T> success(@NonNull final T value) {
        return new Result<>(checkNotNull(value), null);
    }


    /**
     * Creates a {@link Result} of a failed attempt that encountered the given {@code failure}.
     *
     * 构造 失败 Result
     */
    @NonNull
    public static <T> Result<T> failure(@NonNull final Throwable failure) {
        return new Result<>(null, checkNotNull(failure));
    }


    /**
     * Returns the singleton {@link Result} denoting a failed attempt that has a generic failure.
     *
     * 返回 通用的 错误 Result
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> Result<T> failure() {
        return (Result<T>) FAILURE;
    }


    /**
     * Creates a {@link Result} denoting a non-null value. This is an alias of {@link #success}.
     *
     * 返回 成功 Result
     */
    @NonNull
    public static <T> Result<T> present(@NonNull final T value) {
        return success(value);
    }


    /**
     * Returns the singleton {@link Result} denoting an absent value, with a failure of
     * {@link NullPointerException}.
     *
     * 返回 缺少参数的 错误 Result
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> Result<T> absent() {
        return (Result<T>) ABSENT;
    }


    /**
     * Creates a {@code Result} denoting the {@code value} if it is non-null, or returns the
     * singleton
     * {@link #absent} result.
     *
     * 判断 传入的结果 是否为 null
     * null 的话，返回 缺少参数的 错误 Result
     * 不为 null 的话，返回 成功 Result
     */
    @NonNull
    public static <T> Result<T> absentIfNull(@Nullable final T value) {
        return value == null ? Result.<T>absent() : present(value);
    }


    /**
     * Returns whether this is the result of a successful attempt.
     * 根据是否有结果，判断该 Result 是否 成功了
     */
    public boolean succeeded() {
        return value != null;
    }


    /**
     * Returns whether this is the result of a failed attempt.
     * 根据是否有结果，判断该 Result 是否 失败了
     */
    public boolean failed() {
        return value == null;
    }


    /**
     * Returns whether the output value is present. This is an alias of {@link #succeeded()}.
     *
     * 根据是否有结果，判断该 Result 是否 成功了
     */
    public boolean isPresent() {
        return succeeded();
    }


    /**
     * Returns whether this is a result denoting an absent value. This is <i>not</i> an alias of
     * {@link #failed()}; this checks whether this instance is obtained from {@link #absent()}.
     *
     * 判断该 Result 是否是 缺少参数的 错误 Result
     */
    public boolean isAbsent() {
        return this == ABSENT;
    }


    /**
     * Returns the output value of the successful attempt that produced this result.
     *
     * @throws FailedResultException If this is the result of a {@link #failed} attempt. The
     * failure
     * is available from {@link FailedResultException#getCause()}. This is an unchecked exception
     * because it is easily avoidable by first checking whether the attempt has {@link #succeeded}
     * or {@link #failed}, or by using other fluent style methods to achieve the same purpose.
     *
     * 获取存储的 结果
     */
    @NonNull
    public T get() throws FailedResultException {
        if (value != null) {
            return value;
        }
        throw new FailedResultException(failure);
    }


    /**
     * Returns the failure encountered in the attempt that produced this result.
     *
     * @throws IllegalStateException If this is the result of a {@link #succeeded} attempt.
     *
     * 获取发生的错误
     */
    @NonNull
    public Throwable getFailure() {
        checkState(failure != null, "Not a failure");
        return failure;
    }


    /**
     * Returns the output value of the successful attempt, or null if the attempt has {@link
     * #failed}.
     *
     * 获取存储的 结果，不管是否为 null
     */
    @Nullable
    public T orNull() {
        return value;
    }


    /**
     * Returns the failure encountered in the attempt that produced this result, or null if the
     * attempt has {@link #succeeded}.
     *
     * 获取发生的错误，不管是否为 null
     */
    @Nullable
    public Throwable failureOrNull() {
        return failure;
    }


    /**
     * Passes the output value to the {@code receiver} if the attempt has succeeded; otherwise does
     * nothing.
     *
     * @return This instance, for chaining.
     *
     * 如果成功了 就把 结果值 交给 Receiver，并调用 Receiver.accept(...)
     */
    @NonNull
    public Result<T> ifSucceededSendTo(@NonNull final Receiver<? super T> receiver) {
        if (value != null) {
            receiver.accept(value);
        }
        return this;
    }


    /**
     * Passes the encountered failure to the {@code receiver} if the attempt has failed; otherwise
     * does nothing.
     *
     * @return This instance, for chaining.
     *
     * 如果失败了 就把 错误 交给 Receiver，并调用 Receiver.accept(...)
     */
    @NonNull
    public Result<T> ifFailedSendTo(@NonNull final Receiver<? super Throwable> receiver) {
        if (failure != null) {
            receiver.accept(failure);
        }
        return this;
    }


    /**
     * Passes the encountered failure to the {@code receiver} if the failure is absent; otherwise
     * does nothing.
     *
     * @return This instance, for chaining.
     *
     * 如果 是 缺少参数错误
     * 就把 该错误 交给 Receiver，并调用 Receiver.accept(...)
     */
    @NonNull
    public Result<T> ifAbsentFailureSendTo(@NonNull final Receiver<? super Throwable> receiver) {
        if (failure == ABSENT_THROWABLE) {
            receiver.accept(failure);
        }
        return this;
    }


    /**
     * Passes the encountered failure to the {@code receiver} if the attempt has failed, except for
     * the failure absent; otherwise does nothing.
     *
     * @return This instance, for chaining.
     *
     * 如果 有错误 并且不是 缺少参数错误
     * 就把 该错误 交给 Receiver，并调用 Receiver.accept(...)
     */
    @NonNull
    public Result<T> ifNonAbsentFailureSendTo(@NonNull final Receiver<? super Throwable> receiver) {
        if (failure != null && failure != ABSENT_THROWABLE) {
            receiver.accept(failure);
        }
        return this;
    }


    /**
     * Binds the output value with {@code bindValue} using {@code binder} if the attempt has
     * succeeded; otherwise does nothing.
     *
     * @return This instance, for chaining.
     *
     * 如果 成功 了
     * 就将 结果值 交给 Binder，并调用 Binder.bind(...) 自定义结果值的绑定逻辑
     */
    @NonNull
    public <U> Result<T> ifSucceededBind(@NonNull final U bindValue,
                                         @NonNull final Binder<? super T, ? super U> binder) {
        if (value != null) {
            binder.bind(value, bindValue);
        }
        return this;
    }


    /**
     * Binds the output value with {@code bindValue} using {@code binder} if the attempt has
     * failed;
     * otherwise does nothing.
     *
     * @return This instance, for chaining.
     *
     * 如果 失败 了
     * 就将 错误 交给 Binder，并调用 Binder.bind(...) 自定义 错误 的绑定逻辑
     */
    @NonNull
    public <U> Result<T> ifFailedBind(@NonNull final U bindValue,
                                      @NonNull final Binder<Throwable, ? super U> binder) {
        if (failure != null) {
            binder.bind(failure, bindValue);
        }
        return this;
    }


    /**
     * Binds the output value with {@code bindValue} using {@code binder} if the failure is absent;
     * otherwise does nothing.
     *
     * @return This instance, for chaining.
     *
     * 如果 是 缺少参数错误
     * 就将 该错误 交给 Binder，并调用 Binder.bind(...) 自定义 缺少参数错误 的绑定逻辑
     */
    @NonNull
    public <U> Result<T> ifAbsentFailureBind(@NonNull final U bindValue,
                                             @NonNull final Binder<Throwable, ? super U> binder) {
        if (failure == ABSENT_THROWABLE) {
            binder.bind(failure, bindValue);
        }
        return this;
    }


    /**
     * Binds the output value with {@code bindValue} using {@code binder} if the attempt has
     * failed,
     * except for the failure absent; otherwise does nothing.
     *
     * @return This instance, for chaining.
     *
     * 如果 有错误 并且不是 缺少参数错误
     * 就将 该错误 交给 Binder，并调用 Binder.bind(...) 自定义 缺少参数错误 的绑定逻辑
     */
    @NonNull
    public <U> Result<T> ifNonAbsentFailureBind(@NonNull final U bindValue,
                                                @NonNull
                                                final Binder<Throwable, ? super U> binder) {
        if (failure != null && failure != ABSENT_THROWABLE) {
            binder.bind(failure, bindValue);
        }
        return this;
    }


    /**
     * Binds the output value with the value from the {@code supplier} using {@code binder} if the
     * attempt has succeeded; otherwise does nothing, not calling either the binder or the
     * supplier.
     *
     * @return This instance, for chaining.
     *
     * 如果 成功 了
     * 就将 结果值 交给 Binder，并调用 Binder.bind(...) 自定义 结果值 与 Supplier 所提供数据 的绑定逻辑
     */
    @NonNull
    public <U> Result<T> ifSucceededBindFrom(@NonNull final Supplier<U> supplier,
                                             @NonNull final Binder<? super T, ? super U> binder) {
        if (value != null) {
            binder.bind(value, supplier.get());
        }
        return this;
    }


    /**
     * Binds the output value with the value from the {@code supplier} using {@code binder} if the
     * attempt has failed; otherwise does nothing.
     *
     * @return This instance, for chaining.
     *
     * 如果 发生 错误
     * 就将 该错误 交给 Binder，并调用 Binder.bind(...) 自定义 缺少参数错误 与 Supplier 所提供数据 的绑定逻辑
     */
    @NonNull
    public <U> Result<T> ifFailedBindFrom(@NonNull final Supplier<U> supplier,
                                          @NonNull final Binder<Throwable, ? super U> binder) {
        if (failure != null) {
            binder.bind(failure, supplier.get());
        }
        return this;
    }


    /**
     * Binds the output value with the value from the {@code supplier} using {@code binder} if the
     * failure is absent; otherwise does nothing.
     *
     * @return This instance, for chaining.
     *
     * 如果 是 缺少参数错误
     * 就将 该错误 交给 Binder，并调用 Binder.bind(...) 自定义 缺少参数错误 与 Supplier 所提供数据 的绑定逻辑
     */
    @NonNull
    public <U> Result<T> ifAbsentFailureBindFrom(@NonNull final Supplier<U> supplier,
                                                 @NonNull
                                                 final Binder<Throwable, ? super U> binder) {
        if (failure == ABSENT_THROWABLE) {
            binder.bind(failure, supplier.get());
        }
        return this;
    }


    /**
     * Binds the output value with the value from the {@code supplier} using {@code binder} if the
     * attempt has failed, except for the failure absent; otherwise does nothing.
     *
     * @return This instance, for chaining.
     *
     * 如果 有错误 并且不是 缺少参数错误
     * 就将 该错误 交给 Binder，并调用 Binder.bind(...) 自定义 缺少参数错误 与 Supplier 所提供数据 的绑定逻辑
     */
    @NonNull
    public <U> Result<T> ifNonAbsentFailureBindFrom(@NonNull final Supplier<U> supplier,
                                                    @NonNull
                                                    final Binder<Throwable, ? super U> binder) {
        if (failure != null && failure != ABSENT_THROWABLE) {
            binder.bind(failure, supplier.get());
        }
        return this;
    }


    /**
     * Returns a result denoting a failed attempt to obtain a value of a different type, with the
     * same
     * failure.
     *
     * @throws IllegalStateException If this is the result of a {@link #succeeded} attempt.
     *
     * 返回 自身错误 Result
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public <U> Result<U> sameFailure() {
        checkState(failed(), "Not a failure");
        return (Result<U>) this;
    }


    /**
     * Returns a {@link Result} wrapping the result of applying the given {@code function} to the
     * output value encapsulated in this result, or if this is the result of a {@link #failed}
     * attempt, returns the {@link #sameFailure}.
     *
     * 如果 成功 了
     * 可以用 Function 进行数据转型 后
     * 构造一个 转型目标数据 的 Result
     */
    @NonNull
    public <U> Result<U> ifSucceededMap(@NonNull final Function<? super T, U> function) {
        if (value != null) {
            return success(function.apply(value));
        }
        return sameFailure();
    }


    /**
     * Returns the result of a follow-up attempt to apply the given {@code attemptFunction} to the
     * output value encapsulated in this result, or if this is the result of a {@link #failed}
     * attempt, returns the {@link #sameFailure}.
     *
     * 如果 成功 了
     * 可以用 Function 进行数据转型 后（ 这里规定 转型必须为 Result<U> ）
     * 构造一个 转型目标数据（ （ 这里规定 转型必须为 Result<U> ） ） 的 Result<Result<U>>
     */
    @NonNull
    public <U> Result<U> ifSucceededAttemptMap(
        @NonNull final Function<? super T, Result<U>> attemptFunction) {
        if (value != null) {
            return attemptFunction.apply(value);
        }
        return sameFailure();
    }


    /**
     * Returns a {@link Result} wrapping the result of merging the output value encapsulated in
     * this
     * result with the given {@code mergeValue} using the {@code merger}, or if this is the result
     * of
     * a {@link #failed} attempt, returns the {@link #sameFailure}.
     *
     * 如果 成功 了
     * 可以用 Merger 进行数据合并 后
     * 构造一个 合并目标数据 的 Result
     */
    @NonNull
    public <U, V> Result<V> ifSucceededMerge(@NonNull final U mergeValue,
                                             @NonNull
                                             final Merger<? super T, ? super U, V> merger) {
        if (value != null) {
            return success(merger.merge(value, mergeValue));
        }
        return sameFailure();
    }


    /**
     * Returns the result of a follow-up attempt to merge the output value encapsulated in this
     * result with the given {@code mergeValue} using the {@code attemptMerger}, or if this is the
     * result of a {@link #failed} attempt, returns the {@link #sameFailure}.
     *
     * 如果 成功 了
     * 可以用 Merger 进行数据合并 后（ 这里规定 合并目标类型必须为 Result<U> ）
     * 构造一个 合并目标数据（ （ 这里规定 合并目标类型必须为 Result<U> ） ） 的 Result<Result<U>>
     */
    @NonNull
    public <U, V> Result<V> ifSucceededAttemptMerge(@NonNull final U mergeValue,
                                                    @NonNull
                                                    final Merger<? super T, ? super U, Result<V>> attemptMerger) {
        if (value != null) {
            return attemptMerger.merge(value, mergeValue);
        }
        return sameFailure();
    }


    /**
     * Returns a {@link Result} wrapping the result of merging the output value encapsulated in
     * this
     * result with the value from the given {@code mergeValueSupplier} using the {@code merger}, or
     * if
     * this is the result of a {@link #failed} attempt, returns the {@link #sameFailure}.
     *
     * 如果 成功 了
     * 可以用 Merger 进行 结果值 与 Supplier 提供数据 的合并
     * 构造一个 合并目标数据 的 Result
     */
    @NonNull
    public <U, V> Result<V> ifSucceededMergeFrom(@NonNull final Supplier<U> mergeValueSupplier,
                                                 @NonNull
                                                 final Merger<? super T, ? super U, V> merger) {
        if (value != null) {
            return success(merger.merge(value, mergeValueSupplier.get()));
        }
        return sameFailure();
    }


    /**
     * Returns the result of a follow-up attempt to merge the output value encapsulated in this
     * result with the value from the given {@code mergeValueSupplier} using the
     * {@code attemptMerger}, or if this is the result of a {@link #failed} attempt, returns the
     * {@link #sameFailure}.
     *
     * <p>This method is agnostic of the value type of the {@code mergeValueSupplier}. If it is
     * also
     * fallible, the {@code attemptMerger} has the responsibility to interpret the result should
     * the
     * supplier fail. The merger may choose to, for example, return the {@code sameFailure()} if
     * this
     * happens.
     *
     * 如果 成功 了
     * 可以用 Merger 进行 结果值 与 Supplier 提供数据 的合并 后（ 这里规定 合并目标类型必须为 Result<V> ）
     * 构造一个 合并目标数据（ （ 这里规定 合并目标类型必须为 Result<V> ） ） 的 Result<Result<V>>
     */
    @NonNull
    public <U, V> Result<V> ifSucceededAttemptMergeFrom(
        @NonNull final Supplier<U> mergeValueSupplier,
        @NonNull final Merger<? super T, ? super U, Result<V>> attemptMerger) {
        if (value != null) {
            return attemptMerger.merge(value, mergeValueSupplier.get());
        }
        return sameFailure();
    }


    /**
     * Returns the output value if the attempt has succeeded, or the given {@code other} value
     * otherwise.
     *
     * 获取结果值，如果没有就用 提供的 值
     */
    @NonNull
    public T orElse(@NonNull final T other) {
        return value != null ? value : checkNotNull(other);
    }


    /**
     * Returns the output value if the attempt has succeeded, or the value from the given
     * {@code supplier} otherwise.
     *
     * 获取结果值，如果没有就从 提供的 Supplier 内取
     */
    @NonNull
    public T orGetFrom(@NonNull final Supplier<? extends T> supplier) {
        return value != null ? value : checkNotNull(supplier.get());
    }


    /**
     * Returns the same result if the attempt has succeeded, or the result of the attempt to get
     * from
     * the given {@code attemptSupplier} otherwise.
     *
     * 获取 Result<结果值类型>，如果没有就从 提供的 Supplier 内取
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public Result<T> orAttemptGetFrom(
        @NonNull final Supplier<? extends Result<? extends T>> supplier) {
        return value != null ? this : (Result<T>) checkNotNull(supplier.get());
    }


    /**
     * Returns the output value if the attempt has succeeded, or the resulting value of applying
     * the
     * given {@code recoverFunction} to the failure of the attempt.
     *
     * 恢复方法
     * 如果存在结果值，就会返回
     * 不存在的话，就用 Function 结合 发生的错误 去转换为一个结果值，达到恢复效果
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public T recover(@NonNull final Function<? super Throwable, ? extends T> recoverFunction) {
        if (value != null) {
            return value;
        }
        return recoverFunction.apply(failure);
    }


    /**
     * Returns the same result if the attempt has succeeded, or the result of the attempt to apply
     * the
     * given {@code attemptRecoverFunction} to the failure of the attempt.
     *
     * 恢复方法
     * 如果存在结果值，就会返回 自身 Result
     * 不存在的话，就用 Function 结合 发生的错误 去转换为一个 Result<结果类型> ，达到恢复效果
     */
    @SuppressWarnings({ "ConstantConditions", "unchecked" })
    @NonNull
    public Result<T> attemptRecover(
        @NonNull final Function<? super Throwable, ? extends Result<? extends T>>
            attemptRecoverFunction) {
        if (value != null) {
            return this;
        }
        return (Result<T>) attemptRecoverFunction.apply(failure);
    }


    /**
     * Result 自定义的判断相等方法
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final Result<?> result = (Result<?>) o;

        if (value != null ? !value.equals(result.value) : result.value != null) { return false; }
        if (failure != null ? !failure.equals(result.failure) : result.failure != null) {
            return false;
        }

        return true;
    }


    /**
     * Result 自定义 hashCode 方法
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (failure != null ? failure.hashCode() : 0);
        return result;
    }


    /**
     * Result 自定义 toString 方法
     *
     * @return toString
     */
    @Override
    public String toString() {
        if (this == ABSENT) {
            return "Result{Absent}";
        }
        if (this == FAILURE) {
            return "Result{Failure}";
        }
        if (value != null) {
            return "Result{Success; value=" + value + "}";
        }
        return "Result{Failure; failure=" + failure + "}";
    }
}
