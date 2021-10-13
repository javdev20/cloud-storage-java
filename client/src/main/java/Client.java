import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static Scanner command = new Scanner(System.in);
    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;

    public Client() throws IOException {
        socket = new Socket("localhost", 8199);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        System.out.println("Введите комманду:");

        while (true) {

            String[] cmd = command.nextLine().split(" ");

            if (cmd[0].equals("upload")) {
                sendFile(cmd[1]);
            } else if (cmd[0].equals("download")) {
                getFile(cmd[1]);
            } else if (cmd[0].equals("exit")) {
                break;
            }
        }
    }

    private void getFile(String fileName) throws IOException {

        try {
            File file = new File("client/src/main/resources"+ File.separator + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            out.writeUTF("download");
            out.writeUTF(fileName);

            long size = in.readLong();

            FileOutputStream fos = new FileOutputStream(file);

            byte[] buffer = new byte[8 * 1024];

            for (int i = 0; i < (size + (buffer.length - 1)) / (buffer.length); i++) {
                int read = in.read(buffer);
                fos.write(buffer, 0, read);
            }

            fos.close();

            String status = in.readUTF();
            System.out.println("Downloading status" + status);

        } catch (Exception e) {
            out.writeUTF("FATAL ERROR");
        }
    }

    private void sendFile(String fileName) {
        try {
            File file = new File("client/src/main/resources" + File.separator + fileName);
            if (!file.exists()) {
                throw  new FileNotFoundException();
            }

            long fileLength = file.length();
            FileInputStream fis = new FileInputStream(file);

            out.writeUTF("upload");
            out.writeUTF(fileName);
            out.writeLong(fileLength);

            int read = 0;
            byte[] buffer = new byte[8 * 1024];
            while ((read = fis.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            out.flush();

            String status = in.readUTF();
            System.out.println("sending status: " + status);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        new Client();
    }

}
