package Lv3_TCP_MultiRoomChat.server;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {

    private final String roomId;
    private final Set<ClientHandler> participants;

    public ChatRoom(String roomId) {
        this.roomId = roomId;
        this.participants = ConcurrentHashMap.newKeySet();
    }

    public String getRoomId(){
        return roomId;
    }


    public void join(ClientHandler client) {
        participants.add(client);
        broadcast(client.getClientName() + " joined the room");
    }

    public void leave(ClientHandler client) {
        participants.remove(client);
        broadcast(client.getClientName() + " left the room");
    }


    //
    public void broadcast(String message) {
        for (ClientHandler client : participants) {
            client.sendMessage(message);
        }
    }

    public int getParticipantCount() {
        return participants.size();
    }
}