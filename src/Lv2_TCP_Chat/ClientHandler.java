package Lv2_TCP_Chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            clientName = in.readLine();
            String message;

            // server에 알리는 용도
            System.out.println("[JOIN] " + clientName + " connected");

            while ((message = in.readLine()) != null) {
                System.out.println("[" + socket.getRemoteSocketAddress() + "] " + message);
                if ("exit".equalsIgnoreCase(message)) {
                    System.out.println(clientName);
                    server.broadcast("[SYSTEM] " + clientName + " left chat", this);
                    break;
                }

                System.out.println("[Record] " + clientName + ": " + message);
                server.broadcast(clientName + ": " + message, this);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getClientName() {
        return clientName;
    }


}
