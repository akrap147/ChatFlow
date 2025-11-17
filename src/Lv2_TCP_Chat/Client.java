package Lv2_TCP_Chat;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void start(String host, int port) {
        try {
            socket = new Socket(host, port);
            System.out.println("Connected to chat server");

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 닉네임 입력
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your nickname: ");
            String nickname = scanner.nextLine();
            out.println(nickname);

            // 서버 메시지 수신 스레드 (비동기)
            Thread listenerThread = new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = in.readLine()) != null) {
                        System.out.println(serverMsg);
                    }
                } catch (IOException e) {
                    System.out.println("[System] Disconnected from server.");
                }
            });
            listenerThread.start();

            // 메시지 전송 루프 (메인 스레드)
            while (true) {
                String message = scanner.nextLine();
                if ("exit".equalsIgnoreCase(message)) {
                    out.println("exit");
                    break;
                }
                out.println(message);
            }

            stop();

        } catch (IOException e) {
            System.err.println(" Connection error: " + e.getMessage());
        }
    }

    private void stop() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println(" Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start("127.0.0.1", 5555);
    }
}