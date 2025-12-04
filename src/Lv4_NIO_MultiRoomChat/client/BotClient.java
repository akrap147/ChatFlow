package Lv4_NIO_MultiRoomChat.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class BotClient implements Runnable {

    private final String host;
    private final int port;
    private final String name;
    private final String roomName;
    private final long durationMillis;
    private final CountDownLatch latch;
    private final Random random = new Random();

    public BotClient(String host, int port, String name, String roomName,
                     long durationMillis, CountDownLatch latch) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.roomName = roomName;
        this.durationMillis = durationMillis;
        this.latch = latch;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // 1) 방 입장
            out.println("JOIN " + roomName);

            // 2) 서버에서 오는 메시지는 읽어서 버퍼가 안 막히게 해준다.
            Thread reader = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        // 필요하면 로그 찍기
                        // System.out.println("[" + name + "][RECV] " + line);
                    }
                } catch (Exception ignore) {
                }
            }, "BotReader-" + name);
            reader.setDaemon(true);
            reader.start();

            long end = System.currentTimeMillis() + durationMillis;
            int seq = 0;

            while (System.currentTimeMillis() < end) {
                String msg = "hi from " + name + " #" + (seq++);
                out.println("MSG " + msg);
                LoadStats.totalMessages.incrementAndGet();

                // 약간 랜덤 딜레이로 패턴 분산
                Thread.sleep(200 + random.nextInt(300));
            }

            // 3) 방 나가기
            out.println("LEAVE");

        } catch (Exception e) {
            LoadStats.totalErrors.incrementAndGet();
            System.err.println("[BOT-ERR][" + name + "] " + e.getMessage());
        } finally {
            latch.countDown();
        }
    }
}