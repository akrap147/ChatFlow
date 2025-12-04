package Lv4_NIO_MultiRoomChat.client;


import java.util.concurrent.CountDownLatch;

public class LoadTestMain {

    public static void main(String[] args) throws InterruptedException {
        final String host = "localhost";
        final int port = 5555;
        final int clientCount = 300;        // 봇 개수
        final long durationMillis = 10_000; // 각 봇이 보내는 시간 (ms)
        final String roomName = "room1";

        System.out.println("==== NIO ChatServer Load Test ====");
        System.out.println("host=" + host + ", port=" + port);
        System.out.println("clients=" + clientCount + ", durationMs=" + durationMillis);

        LoadStats.reset();
        CountDownLatch latch = new CountDownLatch(clientCount);

        LoadStats.startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < clientCount; i++) {
            String name = "bot-" + i;
            BotClient bot = new BotClient(host, port, name, roomName, durationMillis, latch);
            Thread t = new Thread(bot, "Bot-" + i);
            t.start();
        }

        // 모든 봇이 끝날 때까지 대기
        latch.await();
        LoadStats.endTimeMillis = System.currentTimeMillis();

        long totalMessages = LoadStats.totalMessages.get();
        long totalErrors = LoadStats.totalErrors.get();
        double tps = LoadStats.calcTps();
        long durationMs = LoadStats.endTimeMillis - LoadStats.startTimeMillis;

        System.out.println("==== LOAD TEST DONE ====");
        System.out.println("elapsed ms = " + durationMs);
        System.out.println("total messages = " + totalMessages);
        System.out.println("total errors   = " + totalErrors);
        System.out.printf("TPS ≈ %.2f, 실패율 ≈ %.3f%%%n",
                tps,
                totalMessages == 0 ? 0.0 : (totalErrors * 100.0 / (clientCount)));

        // 실패율 계산식은 네 기준에 맞게 바꿔도 된다.
        // (예: totalMessages 대비 error 비율 등)
    }
}