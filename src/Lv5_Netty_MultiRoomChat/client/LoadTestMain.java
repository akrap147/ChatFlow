package Lv5_Netty_MultiRoomChat.client;

import java.util.concurrent.CountDownLatch;

public class LoadTestMain {

    public static void main(String[] args) throws InterruptedException {
        final String host = "localhost";
        final int port = 5555;
        final int clientCount = 300;
        final long durationMillis = 10_000;
        final String roomName = "room1";

        System.out.println("==== Netty ChatServer Load Test ====");
        System.out.println("host=" + host + ", port=" + port);
        System.out.println("clients=" + clientCount + ", durationMs=" + durationMillis);

        // 통계 초기화
        LoadStats.totalMessages.set(0);
        LoadStats.totalErrors.set(0);

        CountDownLatch latch = new CountDownLatch(clientCount);

        long start = System.currentTimeMillis();

        for (int i = 0; i < clientCount; i++) {
            String name = "bot-" + i;
            BotClient bot = new BotClient(host, port, name, roomName, durationMillis, latch);
            new Thread(bot, "Bot-" + i).start();
        }

        latch.await();
        long end = System.currentTimeMillis();

        long elapsed = end - start;
        long totalMessages = LoadStats.totalMessages.get();
        long totalErrors   = LoadStats.totalErrors.get();

        double tps = elapsed > 0
                ? totalMessages * 1000.0 / elapsed
                : 0.0;
        double errorRate = clientCount > 0
                ? totalErrors * 100.0 / clientCount
                : 0.0;

        System.out.println("==== LOAD TEST DONE ====");
        System.out.println("elapsed ms = " + elapsed);
        System.out.println("total messages = " + totalMessages);
        System.out.println("total errors   = " + totalErrors);
        System.out.printf("TPS ≈ %.2f, 실패율 ≈ %.3f%%%n", tps, errorRate);
    }
}