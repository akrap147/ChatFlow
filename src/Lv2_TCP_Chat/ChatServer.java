package Lv2_TCP_Chat;

import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public void start(int port) {
        try {// serverSocket을 port기준으로 생성
            serverSocket = new ServerSocket(port);
            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("new Client Connect: " + clientSocket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(clientSocket, this);

                //concurrent에 추가
                clients.add(handler);

                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Removed Client : " + client.getClientName());
    }


    public static void main(String[] args) {
        new ChatServer().start(5555);
    }
}
