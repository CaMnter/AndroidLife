package com.camnter.gradle.plugin.toytime;

/**
 * Copy from gradle 4.1
 */
public class Clock implements Timer {
    private long startTime;
    private long startInstant;
    private TimeProvider timeProvider;
    private static final long MS_PER_MINUTE = 60000L;
    private static final long MS_PER_HOUR = 3600000L;


    public Clock() {
        this(new TrueTimeProvider());
    }


    public Clock(long startTime) {
        this.timeProvider = new TrueTimeProvider();
        this.startTime = startTime;
        long msSinceStart = Math.max(this.timeProvider.getCurrentTime() - startTime, 0L);
        this.startInstant = this.timeProvider.getCurrentTimeForDuration() - msSinceStart;
    }


    protected Clock(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        this.reset();
    }


    public String getElapsed() {
        long timeInMs = this.getElapsedMillis();
        return prettyTime(timeInMs);
    }


    public long getElapsedMillis() {
        return Math.max(this.timeProvider.getCurrentTimeForDuration() - this.startInstant, 0L);
    }


    public void reset() {
        this.startTime = this.timeProvider.getCurrentTime();
        this.startInstant = this.timeProvider.getCurrentTimeForDuration();
    }


    public long getStartTime() {
        return this.startTime;
    }


    public static String prettyTime(long timeInMs) {
        StringBuilder result = new StringBuilder();
        if (timeInMs > 3600000L) {
            result.append(timeInMs / 3600000L).append(" hrs ");
        }

        if (timeInMs > 60000L) {
            result.append(timeInMs % 3600000L / 60000L).append(" mins ");
        }

        result.append((double) (timeInMs % 60000L) / 1000.0D).append(" secs");
        return result.toString();
    }
}
