package Lv6_SpringBootChat.controller;


import Lv6_SpringBootChat.model.ChatMessage;
import Lv6_SpringBootChat.model.MessageType;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 클라이언트가 SEND 할 때 사용하는 destination:
     *   /app/chat.sendMessage/{roomId}
     *
     * 서버는 해당 방 구독자들이 SUBSCRIBE한:
     *   /topic/rooms/{roomId}
     * 로 브로드캐스트한다.
     */
    @MessageMapping("/chat.sendMessage/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, ChatMessage message) {
        // roomId를 payload에도 맞춰준다 (클라가 안 채워줄 수도 있으니까)
        System.out.println("[SERVER] Received msg in room="
                + roomId + ", content=" + message.getContent());

        message.setRoomId(roomId);
        if (message.getType() == null) {
            message.setType(MessageType.CHAT);
        }
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, message);
    }

    /**
     * 입장/퇴장 이벤트 등도 원하면 이렇게 별도 엔드포인트로 받을 수 있다.
     *
     * 클라: /app/chat.enter/{roomId}로 JOIN 메시지 보내고,
     * 서버: /topic/rooms/{roomId}에 브로드캐스트.
     */
    @MessageMapping("/chat.enter/{roomId}")
    public void enterRoom(@DestinationVariable String roomId, ChatMessage message) {
        message.setRoomId(roomId);
        message.setType(MessageType.JOIN);
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, message);
    }
}