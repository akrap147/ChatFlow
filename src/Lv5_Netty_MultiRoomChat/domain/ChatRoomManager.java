package Lv5_Netty_MultiRoomChat.domain;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoomManager {

    private final Map<String, ChatRoom> rooms = new ConcurrentHashMap<>();

    private ChatRoom getOrCreateRoom(String roomId) {
        return rooms.computeIfAbsent(roomId, ChatRoom::new);
    }

    public void joinRoom(String roomId, Channel ch) {
        getOrCreateRoom(roomId).join(ch);
    }

    public void leaveRoom(String roomId, Channel ch) {
        ChatRoom room = rooms.get(roomId);
        if (room != null) {
            room.leave(ch);
        }
    }

    public void broadcastToRoom(String roomId, String msg, Channel from) {
        ChatRoom room = rooms.get(roomId);
        if (room != null) {
            room.broadcast(msg, from);
        }
    }

    public void removeChannelFromAllRooms(Channel ch) {
        for (ChatRoom room : rooms.values()) {
            room.removeChannel(ch);
        }
    }
}