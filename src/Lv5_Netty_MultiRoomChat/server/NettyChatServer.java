package Lv5_Netty_MultiRoomChat.server;

import Lv5_Netty_MultiRoomChat.domain.ChatRoomManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyChatServer {

    private final ChatRoomManager roomManager = new ChatRoomManager();

    public void start(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);   // ACCEPT 전용
        EventLoopGroup workerGroup = new NioEventLoopGroup();  // READ/WRITE 전용

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new NettyChatInitializer(roomManager));

            System.out.println("[SERVER] NettyChatServer started. port : " + port);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("[SERVER] NettyChatServer stopped.");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new NettyChatServer().start(5555);
    }
}