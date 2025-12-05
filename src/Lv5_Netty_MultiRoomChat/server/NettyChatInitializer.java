package Lv5_Netty_MultiRoomChat.server;

import Lv5_Netty_MultiRoomChat.domain.ChatRoomManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

public class NettyChatInitializer extends ChannelInitializer<SocketChannel> {

    private final ChatRoomManager roomManager;

    public NettyChatInitializer(ChatRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        // NIO에서 '\n' 기준으로 자르던 걸 Netty 디코더로 처리
        p.addLast(new LineBasedFrameDecoder(1024));
        p.addLast(new StringDecoder(StandardCharsets.UTF_8));
        p.addLast(new StringEncoder(StandardCharsets.UTF_8));

        p.addLast(new NettyChatHandler(roomManager));
    }
}