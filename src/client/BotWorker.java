package client;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BotWorker {

    private final int botId;
    private final String roomId;
    private final StatsCollector stats;

    private final WebSocketStompClient stompClient;
    private StompSession session;

    private final AtomicBoolean connected = new AtomicBoolean(false);

    public BotWorker(int botId, String url, String roomId, StatsCollector stats) {
        this.botId = botId;
        this.roomId = roomId;
        this.stats = stats;

        // WebSocket + STOMP 클라이언트 생성
        var webSocketClient = new StandardWebSocketClient();
        this.stompClient = new WebSocketStompClient(webSocketClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        this.stompClient.setTaskScheduler(scheduler);
        this.stompClient.setDefaultHeartbeat(new long[]{10000, 10000});

        connect(url);
    }

    private void connect(String url) {
        try {
            StompSessionHandler handler = new StompSessionHandlerAdapter() {

                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    BotWorker.this.session = session;
                    connected.set(true);
                    // 메시지 구독
                    session.subscribe("/topic/rooms/" + roomId, new RoomFrameHandler());
                    System.out.println("Bot-" + botId + " connected and subscribed to room " + roomId);
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    stats.recordError();
                    System.err.println("Bot-" + botId + " transport error: " + exception.getMessage());
                }

                @Override
                public void handleException(StompSession session, StompCommand command,
                                            StompHeaders headers, byte[] payload, Throwable exception) {
                    stats.recordError();
                    System.err.println("Bot-" + botId + " exception: " + exception.getMessage());
                }
            };

            // 비동기 connect → 여기서는 간단히 get() 해서 동기화
            stompClient.connect(url, handler).get(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            stats.recordError();
            System.err.println("Bot-" + botId + " failed to connect: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected.get() && session != null && session.isConnected();
    }

    /**
     * 방으로 채팅 메시지 전송.
     * content: "bot-<id>|<timestampMillis>"
     */
    public void sendChatMessage() {
        if (!isConnected()) {
            return;
        }
        long now = System.currentTimeMillis();
        String content = "bot-" + botId + "|" + now;

        ChatMessage message = new ChatMessage();
        message.setType(MessageType.CHAT);
        message.setRoomId(roomId);
        message.setSender("bot-" + botId);
        message.setContent(content);

        try {
            session.send("/app/chat.sendMessage/" + roomId, message);
        } catch (Exception e) {
            stats.recordError();
            System.err.println("Bot-" + botId + " failed to send message: " + e.getMessage());
        }
    }

    /**
     * 방에서 들어오는 메시지 처리 핸들러
     */
    private class RoomFrameHandler implements StompFrameHandler {

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return ChatMessage.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            if (!(payload instanceof ChatMessage)) {
                return;
            }
            ChatMessage msg = (ChatMessage) payload;
            String content = msg.getContent();
            if (content == null) {
                return;
            }

            // 자신의 메시지인지 확인
            String prefix = "bot-" + botId + "|";
            if (!content.startsWith(prefix)) {
                // 다른 봇이 보낸 메시지는 RTT를 안 재고 무시해도 됨 (원하면 통계 따로)
                return;
            }

            // content = "bot-<id>|<timestampMillis>"
            String[] parts = content.split("\\|");
            if (parts.length != 2) {
                return;
            }

            try {
                long sentTime = Long.parseLong(parts[1]);
                long now = System.currentTimeMillis();
                long latency = now - sentTime;
                if (latency >= 0) {
                    stats.recordLatency(latency);
                }
            } catch (NumberFormatException ignore) {
            }
        }
    }
}