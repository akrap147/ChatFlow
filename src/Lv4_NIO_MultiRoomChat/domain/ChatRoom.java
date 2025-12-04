package Lv4_NIO_MultiRoomChat.domain;

import Lv4_NIO_MultiRoomChat.server.NioClientSession;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 하나의 채팅방.
 */
public class ChatRoom {

    private final String roomId;
    private final Set<NioClientSession> participants =
            Collections.synchronizedSet(new HashSet<>());

    public ChatRoom(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void join(NioClientSession session) throws IOException {
        participants.add(session);
        broadcast("[SYSTEM] " + session.remoteAddress() + " joined.", session);
    }

    public void leave(NioClientSession session) throws IOException {
        if (participants.remove(session)) {
            broadcast("[SYSTEM] " + session.remoteAddress() + " left.", session);
        }
    }

    public void broadcast(String message, NioClientSession from) throws IOException {
        synchronized (participants) {
            for (NioClientSession s : participants) {
                if (s == from) continue;
                s.send("[" + roomId + "] " + from.remoteAddress() + " : " + message);
            }
        }
    }

    public void removeSession(NioClientSession session) {
        participants.remove(session);
    }
}