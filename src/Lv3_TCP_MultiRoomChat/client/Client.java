package Lv3_TCP_MultiRoomChat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Scanner sc = new Scanner(System.in);

    public void start(String host, int port) {
        try {
            socket = new Socket(host, port);
            System.out.println("[Client] Server와의 연결을 성공하였습니다. host : " + host + ", port : " + port);

            // socket과의 통로에서 Input을 받는다.
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Client -> Server로 전송
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Enter your name :  ");
            String name = sc.nextLine();

            // Server입장에서 첫 입력은 name으로 받는다.
            out.println(name);

            System.out.println("--- 채팅 방 --- ");
            String roomList = in.readLine();
            System.out.println(roomList);
            System.out.println("입장할 방 이름을 입력하세요: ");

            String roomName = sc.nextLine();
            out.println(roomName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        Client client = new Client();
        client.start("127.0.0.1", 5555);
    }
}
