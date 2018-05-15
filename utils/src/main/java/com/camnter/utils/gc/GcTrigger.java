package com.camnter.utils.gc;

/**
 * LeakCanary 摘抄的源码，实现了接近 立即 GC 的功能
 * https://github.com/square/leakcanary/blob/master/leakcanary-watcher/src/main/java/com/squareup/leakcanary/GcTrigger.java
 *
 * 实质上，LeakCanary 也借鉴了 AOSP 的代码
 * https://android.googlesource.com/platform/libcore/+/master/support/src/test/java/libcore/java/lang/ref/FinalizationTester.java
 *
 * @author CaMnter
 */
public interface GcTrigger {
    GcTrigger DEFAULT = new GcTrigger() {
        @Override
        public void runGc() {
            // Code taken from AOSP FinalizationTest:
            // https://android.googlesource.com/platform/libcore/+/master/support/src/test/java/libcore/
            // java/lang/ref/FinalizationTester.java
            // System.gc() does not garbage collect every time. Runtime.gc() is
            // more likely to perfom a gc.
            Runtime.getRuntime().gc();
            enqueueReferences();
            System.runFinalization();
        }


        private void enqueueReferences() {
            // Hack. We don't have a programmatic way to wait for the reference queue daemon to move
            // references to the appropriate queues.
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new AssertionError();
            }
        }
    };

    void runGc();
}