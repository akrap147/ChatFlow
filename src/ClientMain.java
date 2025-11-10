import java.net.Socket;

public class ClientMain {
    public static void main(String[] args){
        String host = "127.0.0.1";
        int port = 9090;

        try(Socket socket = new Socket(host,port)){
            System.out.println("Connected host : " + host + " , port : " + port );
            Thread.sleep(50000);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
