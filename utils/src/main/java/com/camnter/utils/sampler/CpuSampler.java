package com.camnter.utils.sampler;

import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

class CpuSampler extends AbstractSampler {

    private static final String TAG = "CpuSampler";
    private static final int BUFFER_SIZE = 1000;

    /**
     * TODO: Explain how we define cpu busy in README
     */
    private final int BUSY_TIME;
    private static final int MAX_ENTRY_COUNT = 10;

    private final LinkedHashMap<Long, String> cpuInfoEntries = new LinkedHashMap<>();
    private int pid = 0;
    private long userLast = 0;
    private long systemLast = 0;
    private long idleLast = 0;
    private long ioWaitLast = 0;
    private long totalLast = 0;
    private long appCpuTimeLast = 0;


    public CpuSampler(long sampleInterval) {
        super(sampleInterval);
        BUSY_TIME = (int) (sampleInterval * 1.2f);
    }


    @Override
    public void start(long delayMillis) {
        super.start(delayMillis);
        reset();
    }


    /**
     * Get cpu rate information
     *
     * @return string show cpu rate information
     */
    public String getCpuRateInfo() {
        StringBuilder sb = new StringBuilder();
        synchronized (this.cpuInfoEntries) {
            for (Map.Entry<Long, String> entry : this.cpuInfoEntries.entrySet()) {
                long time = entry.getKey();
                sb.append(TIME_FORMATTER.format(time))
                    .append(' ')
                    .append(entry.getValue())
                    .append(SEPARATOR);
            }
        }
        return sb.toString();
    }


    public boolean isCpuBusy(long start, long end) {
        if (end - start > this.sampleInterval) {
            long s = start - this.sampleInterval;
            long e = start + this.sampleInterval;
            long last = 0;
            synchronized (this.cpuInfoEntries) {
                for (Map.Entry<Long, String> entry : this.cpuInfoEntries.entrySet()) {
                    long time = entry.getKey();
                    if (s < time && time < e) {
                        if (last != 0 && time - last > BUSY_TIME) {
                            return true;
                        }
                        last = time;
                    }
                }
            }
        }
        return false;
    }


    @Override
    protected void doSample() {
        BufferedReader cpuReader = null;
        BufferedReader pidReader = null;

        try {
            cpuReader = new BufferedReader(new InputStreamReader(
                new FileInputStream("/proc/stat")), BUFFER_SIZE);
            String cpuRate = cpuReader.readLine();
            if (cpuRate == null) {
                cpuRate = "";
            }

            if (this.pid == 0) {
                this.pid = android.os.Process.myPid();
            }
            pidReader = new BufferedReader(new InputStreamReader(
                new FileInputStream("/proc/" + pid + "/stat")), BUFFER_SIZE);
            String pidCpuRate = pidReader.readLine();
            if (pidCpuRate == null) {
                pidCpuRate = "";
            }

            parse(cpuRate, pidCpuRate);
        } catch (Throwable throwable) {
            Log.e(TAG, "doSample: ", throwable);
        } finally {
            try {
                if (cpuReader != null) {
                    cpuReader.close();
                }
                if (pidReader != null) {
                    pidReader.close();
                }
            } catch (IOException exception) {
                Log.e(TAG, "doSample: ", exception);
            }
        }
    }


    private void reset() {
        this.userLast = 0;
        this.systemLast = 0;
        this.idleLast = 0;
        this.ioWaitLast = 0;
        this.totalLast = 0;
        this.appCpuTimeLast = 0;
    }


    private void parse(String cpuRate, String pidCpuRate) {
        String[] cpuInfoArray = cpuRate.split(" ");
        if (cpuInfoArray.length < 9) {
            return;
        }

        long user = Long.parseLong(cpuInfoArray[2]);
        long nice = Long.parseLong(cpuInfoArray[3]);
        long system = Long.parseLong(cpuInfoArray[4]);
        long idle = Long.parseLong(cpuInfoArray[5]);
        long ioWait = Long.parseLong(cpuInfoArray[6]);
        long total = user + nice + system + idle + ioWait
            + Long.parseLong(cpuInfoArray[7])
            + Long.parseLong(cpuInfoArray[8]);

        String[] pidCpuInfoList = pidCpuRate.split(" ");
        if (pidCpuInfoList.length < 17) {
            return;
        }

        long appCpuTime = Long.parseLong(pidCpuInfoList[13])
            + Long.parseLong(pidCpuInfoList[14])
            + Long.parseLong(pidCpuInfoList[15])
            + Long.parseLong(pidCpuInfoList[16]);

        if (this.totalLast != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            long idleTime = idle - this.idleLast;
            long totalTime = total - this.totalLast;

            stringBuilder
                .append("cpu:")
                .append((totalTime - idleTime) * 100L / totalTime)
                .append("% ")
                .append("app:")
                .append((appCpuTime - appCpuTimeLast) * 100L / totalTime)
                .append("% ")
                .append("[")
                .append("user:").append((user - userLast) * 100L / totalTime)
                .append("% ")
                .append("system:").append((system - systemLast) * 100L / totalTime)
                .append("% ")
                .append("ioWait:").append((ioWait - ioWaitLast) * 100L / totalTime)
                .append("% ]");

            synchronized (this.cpuInfoEntries) {
                this.cpuInfoEntries.put(System.currentTimeMillis(), stringBuilder.toString());
                if (this.cpuInfoEntries.size() > MAX_ENTRY_COUNT) {
                    for (Map.Entry<Long, String> entry : this.cpuInfoEntries.entrySet()) {
                        Long key = entry.getKey();
                        this.cpuInfoEntries.remove(key);
                        break;
                    }
                }
            }
        }
        this.userLast = user;
        this.systemLast = system;
        this.idleLast = idle;
        this.ioWaitLast = ioWait;
        this.totalLast = total;

        this.appCpuTimeLast = appCpuTime;
    }
}