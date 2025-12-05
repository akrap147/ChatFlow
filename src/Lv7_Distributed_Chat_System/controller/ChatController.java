package Lv7_Distributed_Chat_System.controller;

import Lv7_Distributed_Chat_System.model.ChatMessage;
import Lv7_Distributed_Chat_System.model.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send/{roomId}")
    public void send(@DestinationVariable String roomId, ChatMessage message) {
        System.out.println("[SERVER] room=" + roomId + " msg=" + message.getContent());
        message.setRoomId(roomId);

        if (message.getType() == null) {
            message.setType(MessageType.CHAT);
        }

        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, message);
    }

    @MessageMapping("/chat.enter/{roomId}")
    public void enter(@DestinationVariable String roomId, ChatMessage message) {
        message.setRoomId(roomId);
        message.setType(MessageType.JOIN);

        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, message);
    }
}
