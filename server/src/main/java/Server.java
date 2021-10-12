import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8199);
        System.out.println("Server started...");
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client accepted...");
            new Thread(new ClientHandler(socket)).start();
        }
    }
}
