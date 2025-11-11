package Lv1_TCP_Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            //Socket 서버 생성

            System.out.println("Server listening on port " + port);

            clientSocket = serverSocket.accept();

            System.out.println("Lv1_TCP_Connection.Client connected: " + clientSocket.getRemoteSocketAddress());

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String greeting = in.readLine();
            if ("hello Server".equals(greeting)) {
                out.println("hello Lv1_TCP_Connection.Client");
            } else {
                out.println("unrecognized greeting");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
}