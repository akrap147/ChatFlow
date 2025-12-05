package Lv5_Netty_MultiRoomChat.server;

import Lv5_Netty_MultiRoomChat.domain.ChatRoomManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyChatHandler extends SimpleChannelInboundHandler<String> {

    private final ChatRoomManager roomManager;
    private String currentRoomId;

    public NettyChatHandler(ChatRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("[CONNECT] " + remoteAddress(ctx.channel()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String line) throws Exception {
        line = line.trim();
        if (line.isEmpty()) return;

        Channel ch = ctx.channel();
        System.out.println("[RECV] " + remoteAddress(ch) + " : " + line);

        if (line.startsWith("JOIN ")) {
            String roomId = line.substring("JOIN ".length()).trim();
            joinRoom(ch, roomId);
        } else if (line.equals("LEAVE")) {
            leaveRoom(ch);
        } else if (line.startsWith("MSG ")) {
            String msg = line.substring("MSG ".length()).trim();
            sendMessageToRoom(ch, msg);
        } else {
            send(ch, "[SYSTEM] Unknown command. use: JOIN <room>, MSG <text>, LEAVE");
        }
    }

    private void joinRoom(Channel ch, String roomId) {
        if (currentRoomId != null && !currentRoomId.equals(roomId)) {
            leaveRoom(ch);
        }
        currentRoomId = roomId;
        roomManager.joinRoom(roomId, ch);
        send(ch, "[SYSTEM] joined room: " + roomId);
    }

    private void leaveRoom(Channel ch) {
        if (currentRoomId != null) {
            roomManager.leaveRoom(currentRoomId, ch);
            send(ch, "[SYSTEM] left room: " + currentRoomId);
            currentRoomId = null;
        } else {
            send(ch, "[SYSTEM] not in any room");
        }
    }

    private void sendMessageToRoom(Channel ch, String msg) {
        if (currentRoomId == null) {
            send(ch, "[SYSTEM] join room first. ex) JOIN room1");
            return;
        }
        roomManager.broadcastToRoom(currentRoomId, msg, ch);
    }

    private void send(Channel ch, String msg) {
        ch.writeAndFlush(msg + "\n");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel ch = ctx.channel();
        if (currentRoomId != null) {
            roomManager.leaveRoom(currentRoomId, ch);
        } else {
            roomManager.removeChannelFromAllRooms(ch);
        }
        System.out.println("[DISCONNECT] " + remoteAddress(ch));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private String remoteAddress(Channel ch) {
        return String.valueOf(ch.remoteAddress());
    }
}