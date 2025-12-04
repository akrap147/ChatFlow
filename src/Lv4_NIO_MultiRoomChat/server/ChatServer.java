package Lv4_NIO_MultiRoomChat.server;

import Lv4_NIO_MultiRoomChat.domain.ChatRoomManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Lv4: 순수 NIO 기반 멀티룸 채팅 서버.
 */
public class ChatServer {

    private Selector selector;
    private ServerSocketChannel serverChannel;
    private final ChatRoomManager roomManager = new ChatRoomManager();

    public void start(int port) throws IOException {
        selector = Selector.open();

        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));

        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("[SERVER] NIO ChatServer started. port : " + port);

        while (true) {
            selector.select();

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                try {
                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    } else if (key.isWritable()) {
                        handleWrite(key);
                    }
                } catch (IOException e) {
                    System.out.println("[ERROR] " + e.getMessage());
                    closeKey(key);
                }
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = ssc.accept();
        if (clientChannel == null) return;

        clientChannel.configureBlocking(false);
        System.out.println("[CONNECT] new client : " + clientChannel.getRemoteAddress());

        NioClientSession session = new NioClientSession(clientChannel, roomManager);

        clientChannel.register(selector, SelectionKey.OP_READ, session);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel ch = (SocketChannel) key.channel();
        NioClientSession session = (NioClientSession) key.attachment();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = ch.read(buffer);

        if (read == -1) {
            System.out.println("[DISCONNECT] client : " + ch.getRemoteAddress());
            closeKey(key);
            return;
        }
        if (read == 0) return;

        buffer.flip();
        session.appendData(buffer);
        session.processMessages();
    }

    private void handleWrite(SelectionKey key) throws IOException {
        NioClientSession session = (NioClientSession) key.attachment();
        if (session != null) {
            session.flushPending();
        }
    }

    private void closeKey(SelectionKey key) throws IOException {
        Object attachment = key.attachment();
        if (attachment instanceof NioClientSession session) {
            session.close();
        }
        Channel ch = key.channel();
        key.cancel();
        if (ch != null) {
            ch.close();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        try {
            server.start(5555);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}