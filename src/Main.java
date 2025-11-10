import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.*;

public class Main {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader br;

    public static void main(String[] args) {
        int port = 9090;

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("port :" + port);

            try (Socket client = server.accept()) {
                System.out.println("ACCEPT " + client.getRemoteSocketAddress());
                Thread.sleep(50000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}