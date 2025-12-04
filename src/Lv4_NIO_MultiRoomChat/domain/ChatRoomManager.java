package Lv4_NIO_MultiRoomChat.domain;

import Lv4_NIO_MultiRoomChat.server.NioClientSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 방 목록을 관리.
 */
public class ChatRoomManager {

    private final Map<String, ChatRoom> rooms = new ConcurrentHashMap<>();

    public ChatRoom getOrCreateRoom(String roomId) {
        return rooms.computeIfAbsent(roomId, ChatRoom::new);
    }

    public void joinRoom(String roomId, NioClientSession session) throws IOException {
        ChatRoom room = getOrCreateRoom(roomId);
        room.join(session);
    }

    public void leaveRoom(String roomId, NioClientSession session) throws IOException {
        ChatRoom room = rooms.get(roomId);
        if (room != null) {
            room.leave(session);
        }
    }

    public void broadcastToRoom(String roomId, String message, NioClientSession from) throws IOException {
        ChatRoom room = rooms.get(roomId);
        if (room != null) {
            room.broadcast(message, from);
        }
    }

    public void removeSessionFromAllRooms(NioClientSession session) {
        for (ChatRoom room : rooms.values()) {
            room.removeSession(session);
        }
    }
}