import java.io.*;
import java.net.Socket;
import java.sql.*;

public class ClientHandler implements Runnable{

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    Connection myConn = null;
    Statement myStmt = null;
    ResultSet myRs = null;


    @Override
    public void run() {
        try (
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())
        ) {
            System.out.printf("Client %s connected\n", socket.getInetAddress());


            // Get a connection to database
            myConn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/demo", "student", "student");
            System.out.println("Database connected");

            // Create a statement
            myStmt = myConn.createStatement();
            while (true) {
                String command = in.readUTF();
                if ("upload".equals(command)) {
                    try {
                        File file = new File("server/src/main/resources"  + File.separator + in.readUTF());
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileOutputStream fos = new FileOutputStream(file);

                        long size = in.readLong();

                        byte[] buffer = new byte[8 * 1024];

                        for (int i = 0; i < (size + (buffer.length - 1)) / (buffer.length); i++) {
                            int read = in.read(buffer);
                            fos.write(buffer, 0, read);
                        }

                        fos.close();
                        out.writeUTF("OK");
                    } catch (Exception e) {
                        out.writeUTF("FATAL ERROR");
                    }
                }

                if ("download".equals(command)) {

                    try {
                        File file = new File("server/src/main/resources"  + File.separator + in.readUTF());
                        if (!file.exists()) {
                            throw new FileNotFoundException();
                        }
                        long fileLength = file.length();
                        FileInputStream fis = new FileInputStream(file);

                        out.writeLong(fileLength);

                        int read = 0;
                        byte[] buffer = new byte[8 * 1024];
                        while ((read = fis.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }

                        fis.close();
                        out.flush();
                        out.writeUTF("OK");
                    } catch (Exception e) {
                        out.writeUTF("FATAL ERROR");
                    }
                }

                if ("delete".equals(command)) {
                    try  {
                        File file = new File("server/src/main/resources"+ File.separator + in.readUTF());

                        if (!file.exists()) {
                            throw new FileNotFoundException();
                        }

                        if (file.delete()) {
                            out.writeUTF("OK");
                        } else {
                            out.writeUTF("NOT OK");
                        }

                    } catch (FileNotFoundException e) {
                        out.writeUTF("FileNotFoundException");
                        e.printStackTrace();
                    }
                }

                if ("auth".equals(command)) {
                    try {
                        String login = in.readUTF();
                        String password = in.readUTF();
                        if (userExists(login, password)) {
                            out.writeUTF("Auth is ok");
                        } else {
                            out.writeUTF("No such user");
                        }
                    } catch (IOException e) {
                        out.writeUTF("InputOutputException");
                        e.printStackTrace();
                    }
                }

                if ("reg".equals(command)) {
                    try {
                        String login = in.readUTF();
                        String password = in.readUTF();

                        if (userExists(login, password)) {
                            out.writeUTF("User already registered");
                        } else {
                            registerUser(myConn, login, password);
                            out.writeUTF("Registered successfully");
                        }

                    } catch (IOException e) {
                        out.writeUTF("IOException");
                        e.printStackTrace();
                    }
                }

                if ("exit".equals(command)) {
                    System.out.printf("Client %s disconnected correctly\n", socket.getInetAddress());
                    break;
                }

                System.out.println(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerUser(Connection myConn, String login, String password) throws SQLException {
        PreparedStatement myStmt = null;

        try {
            myStmt = myConn.prepareStatement("insert into users (login, password) " +
                                                "values (?, ?)");
            myStmt.setString(1, login);
            myStmt.setString(2, password);

            myStmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            myStmt.close();
        }

    }

    public boolean userExists(String login, String password) {
        try {

            myRs = myStmt.executeQuery("select * from users");

            // Check existence of the login
            while (myRs.next()) {
                String login1 = myRs.getString("login");
                String password1 = myRs.getString("password");

                if (login.equals(login1) && password.equals(password1)) {
                    return true;
                }
                break;
            }

        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return false;
    }

//    private static void close(Connection myConn, Statement myStmt,
//                              ResultSet myRs) throws SQLException {
//        if (myRs != null) {
//            myRs.close();
//        }
//
//        if ( myStmt!= null) {
//            myStmt.close();
//        }
//
//        if (myConn != null) {
//            myConn.close();
//        }
//    }
}
