package Lv3_TCP_MultiRoomChat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private ServerSocket serverSocket;
    private final ChatRoomManager roomManager = new ChatRoomManager();

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[SERVER] 새로운 ChatServer가 시작 되었습니다. prot : " + port);

            while (true) {
                // 여기서 새로운 Client들의 Socket을 받고 배분해줄것이다.
                Socket socket = serverSocket.accept();
                System.out.println("[CONNECT] new Client가 연결되었습니다. " + socket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(socket, roomManager);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }


    //ChatServer의 Stop
    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("[SERVER] SERVER STOP.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start(5555);
    }
}
