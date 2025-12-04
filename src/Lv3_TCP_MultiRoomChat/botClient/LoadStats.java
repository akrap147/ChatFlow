package Lv3_TCP_MultiRoomChat.botClient;

import java.util.concurrent.atomic.AtomicLong;

public class LoadStats {
    public static final AtomicLong totalMessages = new AtomicLong();
    public static final AtomicLong totalErrors = new AtomicLong();
}