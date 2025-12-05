package client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BotManager {

    // 설정값
    private static final String WS_URL = "ws://localhost:8080/ws"; // 네 서버 endpoint
    private static final String ROOM_ID = "room-1";                // 테스트용 방 ID
    private static final int BOT_COUNT = 100;                      // 봇 수
    private static final long SEND_INTERVAL_MS = 1000L;            // 각 봇당 전송 주기 (ms)
    private static final long TEST_DURATION_SEC = 300L;            // 전체 테스트 시간 (sec)

    private final StatsCollector statsCollector = new StatsCollector();
    private final List<BotWorker> bots = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public static void main(String[] args) throws Exception {
        BotManager manager = new BotManager();
        manager.start();
    }

    public void start() throws Exception {
        System.out.println("=== Starting BotManager ===");
        System.out.println("URL      : " + WS_URL);
        System.out.println("Room     : " + ROOM_ID);
        System.out.println("Bots     : " + BOT_COUNT);
        System.out.println("Interval : " + SEND_INTERVAL_MS + " ms");
        System.out.println("Duration : " + TEST_DURATION_SEC + " sec");

        // 1) 봇 생성 및 연결
        for (int i = 0; i < BOT_COUNT; i++) {
            BotWorker bot = new BotWorker(i, WS_URL, ROOM_ID, statsCollector);
            bots.add(bot);
        }

        // 2) 주기적으로 메시지 전송 스케줄링
        scheduler.scheduleAtFixedRate(this::broadcastFromAllBots,
                2,  // initial delay
                SEND_INTERVAL_MS,
                TimeUnit.MILLISECONDS);

        // 3) 주기적으로 통계 출력 (5초마다)
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("[STATS]");
            statsCollector.printSummary();
        }, 5, 5, TimeUnit.SECONDS);

        // 4) 지정된 시간 동안 테스트 후 종료
        TimeUnit.SECONDS.sleep(TEST_DURATION_SEC);

        System.out.println("=== Stopping load test ===");
        scheduler.shutdownNow();
        statsCollector.printSummary();
        System.out.println("=== Done ===");
    }

    private void broadcastFromAllBots() {
        for (BotWorker bot : bots) {
            bot.sendChatMessage();
        }
    }
}