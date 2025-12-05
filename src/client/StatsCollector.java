package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

public class StatsCollector {

    private final LongAdder totalLatency = new LongAdder();
    private final LongAdder totalCount = new LongAdder();
    private final LongAdder errorCount = new LongAdder();

    // 단순 구현용. 장시간/대량 테스트면 히스토그램 구조로 바꾸는 게 좋다.
    private final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

    private final long startTimeMillis = System.currentTimeMillis();

    public void recordLatency(long ms) {
        latencies.add(ms);
        totalLatency.add(ms);
        totalCount.increment();
    }

    public void recordError() {
        errorCount.increment();
    }

    public void printSummary() {
        long now = System.currentTimeMillis();
        long durationSec = Math.max(1, (now - startTimeMillis) / 1000);

        long cnt = totalCount.sum();
        double avg = (cnt == 0) ? 0.0 : (double) totalLatency.sum() / cnt;

        long p50 = percentile(50);
        long p95 = percentile(95);
        long p99 = percentile(99);

        double tps = (double) cnt / durationSec;

        System.out.println("===== Load Test Stats =====");
        System.out.println("Elapsed      : " + durationSec + " sec");
        System.out.println("Total Msg    : " + cnt);
        System.out.println("Errors       : " + errorCount.sum());
        System.out.println("TPS (approx) : " + String.format("%.2f", tps));
        System.out.println("Avg Latency  : " + String.format("%.2f", avg) + " ms");
        System.out.println("P50          : " + p50 + " ms");
        System.out.println("P95          : " + p95 + " ms");
        System.out.println("P99          : " + p99 + " ms");
        System.out.println("===========================");
    }

    private long percentile(int p) {
        if (latencies.isEmpty()) return 0L;
        List<Long> copy;
        synchronized (latencies) {
            copy = new ArrayList<>(latencies);
        }
        Collections.sort(copy);
        int idx = (int) Math.round((p / 100.0) * (copy.size() - 1));
        idx = Math.max(0, Math.min(idx, copy.size() - 1));
        return copy.get(idx);
    }
}