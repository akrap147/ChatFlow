package Lv4_NIO_MultiRoomChat.client;

import java.util.concurrent.atomic.AtomicLong;

public class LoadStats {

    public static final AtomicLong totalMessages = new AtomicLong();
    public static final AtomicLong totalErrors = new AtomicLong();

    public static long startTimeMillis;
    public static long endTimeMillis;

    public static void reset() {
        totalMessages.set(0);
        totalErrors.set(0);
        startTimeMillis = 0;
        endTimeMillis = 0;
    }

    public static double calcTps() {
        long durationMs = endTimeMillis - startTimeMillis;
        if (durationMs <= 0) return 0.0;
        return totalMessages.get() * 1000.0 / durationMs;
    }
}