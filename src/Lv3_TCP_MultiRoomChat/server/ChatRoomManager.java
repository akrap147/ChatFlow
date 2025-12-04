package Lv3_TCP_MultiRoomChat.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoomManager {
    private final Map<String, ChatRoom> rooms = new ConcurrentHashMap<>();

    public ChatRoomManager() {
    }

    public ChatRoom getOrCreateRoom(String roomId) {
        return rooms.computeIfAbsent(roomId, id -> new ChatRoom(id));
    }

    public ChatRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public void removeRoom(String roomId) {
        rooms.remove(roomId);
    }

    public Map<String, ChatRoom> getAllRooms() {
        return rooms;
    }

    public String getRoomListAsString() {


        if (rooms.isEmpty()) {
            return """
                    ┌───────────────────────────────┐
                    │       현재 개설된 채팅방이 없습니다. │
                    │  새로운 방 이름을 입력해 만들어보세요! │
                    └───────────────────────────────┘
                    """;
        }

        StringBuilder sb = new StringBuilder("Room List:\n");
        for (String roomId : rooms.keySet()) {
            ChatRoom room = rooms.get(roomId);
            sb.append("- ").append(roomId)
                    .append(" (").append(room.getParticipantCount()).append(" 명)\n");
        }

        return sb.toString();
    }


    public int getRoomCount() {
        return rooms.size();
    }
}
