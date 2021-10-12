import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public Client() throws IOException {
        socket = new Socket("localhost", 8199);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    public static void main(String[] args) throws IOException {
        new Client();
    }

}
