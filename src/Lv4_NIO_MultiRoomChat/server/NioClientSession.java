package Lv4_NIO_MultiRoomChat.server;

import Lv4_NIO_MultiRoomChat.domain.ChatRoomManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * 각 클라이언트 커넥션의 상태를 관리.
 * 간단한 텍스트 프로토콜:
 *   JOIN roomId
 *   MSG message...
 *   LEAVE
 */
public class NioClientSession {

    private final SocketChannel channel;
    private final ChatRoomManager roomManager;
    private final StringBuilder inboundBuffer = new StringBuilder();

    // 현재 입장해 있는 방 (단일 방 가정)
    private String currentRoomId;

    public NioClientSession(SocketChannel channel, ChatRoomManager roomManager) {
        this.channel = channel;
        this.roomManager = roomManager;
    }

    public void appendData(ByteBuffer buf) {
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        String incoming = new String(bytes, StandardCharsets.UTF_8);
        inboundBuffer.append(incoming);
    }

    public void processMessages() throws IOException {
        int newlineIndex;
        while ((newlineIndex = inboundBuffer.indexOf("\n")) >= 0) {
            String line = inboundBuffer.substring(0, newlineIndex).trim();
            inboundBuffer.delete(0, newlineIndex + 1);

            if (!line.isEmpty()) {
                handleLine(line);
            }
        }
    }

    private void handleLine(String line) throws IOException {
        System.out.println("[RECV] " + remoteAddress() + " : " + line);

        if (line.startsWith("JOIN ")) {
            String roomId = line.substring("JOIN ".length()).trim();
            joinRoom(roomId);
        } else if (line.equals("LEAVE")) {
            leaveRoom();
        } else if (line.startsWith("MSG ")) {
            String msg = line.substring("MSG ".length()).trim();
            sendMessageToRoom(msg);
        } else {
            send("[SYSTEM] Unknown command. use: JOIN <room>, MSG <text>, LEAVE");
        }
    }

    private void joinRoom(String roomId) throws IOException {
        if (currentRoomId != null && !currentRoomId.equals(roomId)) {
            leaveRoom();
        }
        currentRoomId = roomId;
        roomManager.joinRoom(roomId, this);
        send("[SYSTEM] joined room: " + roomId);
    }

    private void leaveRoom() throws IOException {
        if (currentRoomId != null) {
            roomManager.leaveRoom(currentRoomId, this);
            send("[SYSTEM] left room: " + currentRoomId);
            currentRoomId = null;
        } else {
            send("[SYSTEM] not in any room");
        }
    }

    private void sendMessageToRoom(String msg) throws IOException {
        if (currentRoomId == null) {
            send("[SYSTEM] join room first. ex) JOIN room1");
            return;
        }
        roomManager.broadcastToRoom(currentRoomId, msg, this);
    }

    public void send(String msg) throws IOException {
        String withNewLine = msg + "\n";
        ByteBuffer out = ByteBuffer.wrap(withNewLine.getBytes(StandardCharsets.UTF_8));
        while (out.hasRemaining()) {
            channel.write(out);
        }
    }

    public void flushPending() {
        // 현재는 write 큐를 쓰지 않으므로 비워둠
    }

    public void close() {
        try {
            if (currentRoomId != null) {
                roomManager.leaveRoom(currentRoomId, this);
            } else {
                roomManager.removeSessionFromAllRooms(this);
            }
        } catch (IOException ignored) {
        }

        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String remoteAddress() {
        try {
            return channel.getRemoteAddress().toString();
        } catch (IOException e) {
            return "unknown";
        }
    }

    public SocketChannel getChannel() {
        return channel;
    }
}