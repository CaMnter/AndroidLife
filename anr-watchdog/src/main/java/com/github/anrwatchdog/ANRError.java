package com.github.anrwatchdog;

import android.os.Looper;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Error thrown by {@link com.github.anrwatchdog.ANRWatchDog} when an ANR is detected.
 * Contains the stack trace of the frozen UI thread.
 * <p>
 * It is important to notice that, in an ANRError, all the "Caused by" are not really the cause
 * of the exception. Each "Caused by" is the stack trace of a running thread. Note that the main
 * thread always comes first.
 *
 * 自定义一个 Error
 *
 * {@link ANRError#New(String prefix, boolean logThreadsWithoutStackTrace)}
 * 1. 获取主线程
 * 2. 创建一个排序的 map，按照线程排序，otherThread，mainThread
 * 3. 筛选出 主线程 和 prefix 关键字线程 的 栈信息
 * 4. 按照上面的排序，有 mainThread 就会放在最后
 * 5. 然后 for 会走到 map 的最后
 * 6. 也就是说，有 mainThread 的话，会返回一个包含 mainThread 的 ANRError
 * -  没的话，就是 map 最后的一个 thread 的 ANRError
 *
 * {@link ANRError#NewMainOnly()}
 * 直接创建一个包含 mainThread 的 ANRError
 */
@SuppressWarnings({ "Convert2Diamond", "UnusedDeclaration", "DanglingJavadoc" })
public class ANRError extends Error {

    private static class $ implements Serializable {
        private final String _name;
        private final StackTraceElement[] _stackTrace;


        private class _Thread extends Throwable {
            private _Thread(_Thread other) {
                super(_name, other);
            }


            @Override
            public Throwable fillInStackTrace() {
                setStackTrace(_stackTrace);
                return this;
            }
        }


        private $(String name, StackTraceElement[] stackTrace) {
            _name = name;
            _stackTrace = stackTrace;
        }
    }


    private static final long serialVersionUID = 1L;


    private ANRError($._Thread st) {
        super("Application Not Responding", st);
    }


    @Override
    public Throwable fillInStackTrace() {
        setStackTrace(new StackTraceElement[] {});
        return this;
    }


    /**
     * 1. 获取主线程
     * 2. 创建一个排序的 map，按照线程排序，otherThread，mainThread
     * 3. 筛选出 主线程 和 prefix 关键字线程 的 栈信息
     * 4. 按照上面的排序，有 mainThread 就会放在最后
     * 5. 然后 for 会走到 map 的最后
     * 6. 也就是说，有 mainThread 的话，会返回一个包含 mainThread 的 ANRError
     * -  没的话，就是 map 最后的一个 thread 的 ANRError
     *
     * @param prefix prefix
     * @param logThreadsWithoutStackTrace logThreadsWithoutStackTrace
     * @return ANRError
     */
    static ANRError New(String prefix, boolean logThreadsWithoutStackTrace) {
        final Thread mainThread = Looper.getMainLooper().getThread();

        /**
         * 创建一个排序的 map，按照线程排序，otherThread，mainThread
         */
        final Map<Thread, StackTraceElement[]> stackTraces
            = new TreeMap<Thread, StackTraceElement[]>(new Comparator<Thread>() {
            @Override
            public int compare(Thread lhs, Thread rhs) {
                if (lhs == rhs) {
                    return 0;
                }
                if (lhs == mainThread) {
                    return 1;
                }
                if (rhs == mainThread) {
                    return -1;
                }
                return rhs.getName().compareTo(lhs.getName());
            }
        });

        /**
         * 筛选出 主线程 和 prefix 关键字线程 的 栈信息
         */
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            if (
                entry.getKey() == mainThread
                    || (
                    entry.getKey().getName().startsWith(prefix)
                        && (
                        logThreadsWithoutStackTrace
                            ||
                            entry.getValue().length > 0
                    )
                )
                ) {
                stackTraces.put(entry.getKey(), entry.getValue());
            }
        }

        /**
         * 上面没筛选出 mainThread 信息的话
         * 这里将 mainThread 塞进去
         */
        // Sometimes main is not returned in getAllStackTraces() - ensure that we list it
        if (!stackTraces.containsKey(mainThread)) {
            stackTraces.put(mainThread, mainThread.getStackTrace());
        }

        /**
         * 有 mainThread 的话，会返回一个包含 mainThread 的 ANRError
         * 没的话，就是 map 最后的一个 thread 的 ANRError
         *
         */
        $._Thread tst = null;
        for (Map.Entry<Thread, StackTraceElement[]> entry : stackTraces.entrySet()) {
            tst = new $(getThreadTitle(entry.getKey()), entry.getValue()).new _Thread(tst);
        }

        return new ANRError(tst);
    }


    /**
     * 直接创建一个包含 mainThread 的 ANRError
     *
     * @return ANRError
     */
    static ANRError NewMainOnly() {
        final Thread mainThread = Looper.getMainLooper().getThread();
        final StackTraceElement[] mainStackTrace = mainThread.getStackTrace();

        return new ANRError(new $(getThreadTitle(mainThread), mainStackTrace).new _Thread(null));
    }


    private static String getThreadTitle(Thread thread) {
        return thread.getName() + " (state = " + thread.getState() + ")";
    }
}