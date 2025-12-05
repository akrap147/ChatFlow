package Lv7_Distributed_Chat_System.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    private String roomId;
    private String sender;
    private String content;
    private MessageType type;
}
