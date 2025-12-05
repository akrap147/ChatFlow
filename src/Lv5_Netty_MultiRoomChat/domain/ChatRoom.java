package Lv5_Netty_MultiRoomChat.domain;

import io.netty.channel.Channel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChatRoom {

    private final String roomId;
    private final Set<Channel> channels =
            Collections.synchronizedSet(new HashSet<>());

    public ChatRoom(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void join(Channel ch) {
        channels.add(ch);
    }

    public void leave(Channel ch) {
        channels.remove(ch);
    }

    public void broadcast(String message, Channel from) {
        synchronized (channels) {
            for (Channel ch : channels) {
                if (ch == from) continue;
                ch.writeAndFlush("[" + roomId + "] " + from.remoteAddress() + " : " + message + "\n");
            }
        }
    }

    public void removeChannel(Channel ch) {
        channels.remove(ch);
    }
}