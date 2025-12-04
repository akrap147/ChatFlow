package Lv3_TCP_MultiRoomChat.botClient;

import java.util.concurrent.CountDownLatch;

public class LoadTestMain {

    public static void main(String[] args) throws InterruptedException {
        String host = "127.0.0.1";
        int port = 5555;

        int clientCount = 500;           // 유저 수
        int roomCount = 5;               // room-0 ~ room-4
        long durationMillis = 30_000L;   // 각 봇이 활동할 시간 (30초)

        CountDownLatch latch = new CountDownLatch(clientCount);

        long startAt = System.currentTimeMillis();

        for (int i = 0; i < clientCount; i++) {
            String name = "user-" + i;
            String roomName = "room-" + (i % roomCount);

            BotClient bot = new BotClient(host, port, name, roomName, durationMillis, latch);
            new Thread(bot, name).start();

            // 너무 동시에 꽝 찍지 말고 살짝 텀
            Thread.sleep(10);
        }

        // 모든 봇이 끝날 때까지 대기
        latch.await();

        long endAt = System.currentTimeMillis();
        long elapsed = endAt - startAt;

        System.out.println("==== LOAD TEST DONE ====");
        System.out.println("elapsed ms = " + elapsed);
        System.out.println("total messages = " + LoadStats.totalMessages.get());
        System.out.println("total errors   = " + LoadStats.totalErrors.get());

        double tps = LoadStats.totalMessages.get() / (elapsed / 1000.0);
        double failRate = LoadStats.totalErrors.get() * 100.0 / Math.max(1, LoadStats.totalMessages.get());

        System.out.printf("TPS ≈ %.2f, 실패율 ≈ %.2f%%%n", tps, failRate);
    }
}