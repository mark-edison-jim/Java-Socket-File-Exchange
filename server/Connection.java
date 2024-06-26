import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Connection extends Thread {

    private Socket s;

    public Connection(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            String msg;
            DataInputStream reader = new DataInputStream(s.getInputStream());
            DataOutputStream writer = new DataOutputStream(s.getOutputStream());
            ArrayList<String> handles = new ArrayList<String>();
            // This checks whether the string that was sent from
            // the client side is the terminal "END" else we
            // send the string back to the client
            while (!(msg = reader.readUTF()).equals("END")) {
                String clientCommands[] = msg.split(" ", 2);
                switch (clientCommands[0]) {
                    case "/register":
                        if (handles.contains(clientCommands[1])) {
                            writer.writeUTF("False");
                        } else {
                            handles.add(clientCommands[1]);
                            writer.writeUTF("True");
                        }
                        break;
                    case "/store":
                        String fileName = reader.readUTF();

                        if (fileName.length() > 0) {
                            int fileContentLength = reader.readInt();
                            if (fileContentLength > 0) {
                                byte[] fileContentBytes = new byte[fileContentLength];
                                reader.readFully(fileContentBytes, 0, fileContentLength);

                                File file = new File("./files/" + fileName);
                                FileOutputStream fos = new FileOutputStream(file);
                                fos.write(fileContentBytes);
                                fos.close();
                            }
                        }
                        break;
                    case "/dir":
                        // Creating a File object for directory
                        File directoryPath = new File("./files");
                        // List of all files and directories
                        String contents[] = directoryPath.list();
                        System.out.println("List of files and directories in the specified directory:");
                        writer.writeInt(contents.length);
                        for (String name : contents) {
                            writer.writeUTF(name);
                        }
                        break;

                    case "/get":
                        String path = "./files/" + clientCommands[1];
                        File file = new File(path);
                        if (file.exists() && !file.isDirectory()) {
                            writer.writeBoolean(true);
                            sendFile(s, clientCommands[1], file);
                        } else {
                            writer.writeBoolean(false);
                        }

                        break;
                    default:
                        System.out.println("Error: Command not found.");
                        break;
                }
            }

            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server: Client " + s.getRemoteSocketAddress() + " has disconnected");
        }
    }

    static void sendFile(Socket endpoint, String fileName, File file) {
        try {

            FileInputStream fis = new FileInputStream(file.getAbsolutePath());
            DataOutputStream dos = new DataOutputStream(endpoint.getOutputStream());

            byte[] fileContentBytes = new byte[(int) file.length()];

            fis.read(fileContentBytes);

            dos.writeUTF(fileName);

            dos.writeInt(fileContentBytes.length);
            dos.write(fileContentBytes);

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}