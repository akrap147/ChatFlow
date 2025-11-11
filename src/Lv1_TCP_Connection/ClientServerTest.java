package Lv1_TCP_Connection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientServerTest {

    private SocketServer server;
    private Client client;
    private Thread serverThread;

    @BeforeEach
    void setUp() {
        server = new SocketServer();
        serverThread = new Thread(() -> server.start(6666));
        serverThread.start();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        client = new Client();
        client.startConnection("127.0.0.1", 6666);

    }

    @Test
    void testMessageExchange() throws IOException {
        String response = client.sendMessage("hello Server");
        assertEquals("hello Lv1_TCP_Connection.Client", response);
    }


    @AfterEach
    void tearDown() throws IOException {
        client.stopConnection();
        server.stop();
    }
}
