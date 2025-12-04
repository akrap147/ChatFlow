package Lv3_TCP_MultiRoomChat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


// Client 1명을 controller 하기 위한 Thread라고 생각하면 된다.
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatRoomManager roomManager;
    private ChatRoom currentRoom;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;

    public ClientHandler(Socket socket, ChatRoomManager roomManager) {
        this.socket = socket;
        this.roomManager = roomManager;
    }

    public String getClientName() {
        return clientName;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 이름을 입력 받고
            clientName = in.readLine();
            System.out.println("[JOIN] " + clientName + " connected");

            // 현재 가지고 있는 방 이름들을 제공해준다.
            out.println(roomManager.getRoomListAsString());
            System.out.println("입력 받기 전");


            // 방 이름을 받고 설정해준다.
            String roomId = in.readLine();
            System.out.println("입력 받고 나서");
            currentRoom = roomManager.getOrCreateRoom(roomId);
            currentRoom.join(this);

            currentRoom.broadcast("[SYSTEM ] " + clientName + "님이 방에 입장하셨습니다.");


            String message;
            while ((message = in.readLine()) != null) {
                // 나가기
                if ("exit".equalsIgnoreCase(message)) {
                    currentRoom.broadcast("[SYSTEM] " + clientName + "님이 방을 나가셨습니다.");
                    currentRoom.leave(this);
                    break;
                }

                System.out.println("[Room + " + currentRoom.getRoomId() + "] " + clientName + ": " + message);
                currentRoom.broadcast(clientName + ": " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (currentRoom != null) {
                currentRoom.leave(this);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 개별 Client 관리 임. 해당 client에게 전달
    public void sendMessage(String msg){
        out.println(msg);
    }

}
