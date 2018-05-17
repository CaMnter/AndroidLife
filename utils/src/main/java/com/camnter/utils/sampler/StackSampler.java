package com.camnter.utils.sampler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * AndroidPerformanceMonitor 摘抄并修改的源码
 * 栈信息采集器
 *
 * 在 子线程打印 UI 线程的栈信息
 *
 * https://github.com/markzhai/AndroidPerformanceMonitor/blob/master/blockcanary-analyzer/src/main/java/com/github/moduth/blockcanary/StackSampler.java
 *
 * @author CaMnter
 */
class StackSampler extends AbstractSampler {

    private static final int DEFAULT_MAX_ENTRY_COUNT = 100;
    private static final LinkedHashMap<Long, String> STACK_MAP = new LinkedHashMap<>();

    public static final String SEPARATOR = "\r\n";
    public static final SimpleDateFormat TIME_FORMATTER =
        new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);

    private int maxEntryCount = DEFAULT_MAX_ENTRY_COUNT;
    private Thread currentThread;


    public StackSampler(Thread thread, long sampleIntervalMillis) {
        this(thread, DEFAULT_MAX_ENTRY_COUNT, sampleIntervalMillis);
    }


    public StackSampler(Thread thread, int maxEntryCount, long sampleIntervalMillis) {
        super(sampleIntervalMillis);
        this.currentThread = thread;
        this.maxEntryCount = maxEntryCount;
    }


    public ArrayList<String> getThreadStackEntries(long startTime, long endTime) {
        ArrayList<String> result = new ArrayList<>();
        synchronized (STACK_MAP) {
            for (Long entryTime : STACK_MAP.keySet()) {
                if (startTime < entryTime && entryTime < endTime) {
                    result.add(TIME_FORMATTER.format(entryTime)
                        + SEPARATOR
                        + SEPARATOR
                        + STACK_MAP.get(entryTime));
                }
            }
        }
        return result;
    }


    @Override
    protected void doSample() {
        StringBuilder stringBuilder = new StringBuilder();

        for (StackTraceElement stackTraceElement : this.currentThread.getStackTrace()) {
            stringBuilder
                .append(stackTraceElement.toString())
                .append(SEPARATOR);
        }

        synchronized (STACK_MAP) {
            if (STACK_MAP.size() == this.maxEntryCount && this.maxEntryCount > 0) {
                STACK_MAP.remove(STACK_MAP.keySet().iterator().next());
            }
            STACK_MAP.put(System.currentTimeMillis(), stringBuilder.toString());
        }
    }

}