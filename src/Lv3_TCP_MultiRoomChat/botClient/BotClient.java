package Lv3_TCP_MultiRoomChat.botClient;

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

            // 1) 이름
            out.println(name);

            // 2) 방 리스트 한 줄 읽고 버리기
            in.readLine();

            // 3) 방 이름
            out.println(roomName);

            long end = System.currentTimeMillis() + durationMillis;

            while (System.currentTimeMillis() < end) {
                String msg = "hi from " + name;
                out.println(msg);
                LoadStats.totalMessages.incrementAndGet();

                // 필요하면 in.readLine()으로 응답도 읽어볼 수 있음
                Thread.sleep(500);
            }

            out.println("exit");

        } catch (Exception e) {
            LoadStats.totalErrors.incrementAndGet();
            System.err.println("[BOT-ERR][" + name + "] " + e.getMessage());
        } finally {
            latch.countDown();
        }
    }
}