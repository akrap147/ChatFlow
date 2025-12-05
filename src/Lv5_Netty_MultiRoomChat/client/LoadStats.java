package Lv5_Netty_MultiRoomChat.client;

import java.util.concurrent.atomic.AtomicLong;

public class LoadStats {
    public static final AtomicLong totalMessages = new AtomicLong();
    public static final AtomicLong totalErrors   = new AtomicLong();
}