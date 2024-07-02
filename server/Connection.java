import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Connection extends Thread {

    private static Socket s;
    private static HashMap<Socket, String> handles = new HashMap<>();
    private ArrayList<Socket> chatRoom = new ArrayList<>();
    private static Socket directChat;

    public Connection(Socket s, HashMap<Socket, String> handles, ArrayList<Socket> chatRoom) {
        this.s = s;
        this.handles = handles;
        this.chatRoom = chatRoom;
    }

    @Override
    public void run() {
        try {
            String msg;
            DataInputStream reader = new DataInputStream(s.getInputStream());
            DataOutputStream writer = new DataOutputStream(s.getOutputStream());

            // This checks whether the string that was sent from
            // the client side is the terminal "END" else we
            // send the string back to the client
            while (!(msg = reader.readUTF()).equals("END")) {
                String clientCommands[] = msg.split(" ", 2);
                switch (clientCommands[0]) {
                    case "/register":
                        if (handles.containsValue(clientCommands[1])) {
                            writer.writeUTF("False");
                        } else {
                            handles.put(s, clientCommands[1]);
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
                    case "/joinCR":
                        chatRoom.add(s);
                        for (Socket socket : chatRoom) {
                            if (!socket.equals(s)) {
                                DataOutputStream sender = new DataOutputStream(socket.getOutputStream());
                                sender.writeUTF("--" + handles.get(s) + " has joined the chat room--");
                            }
                        }
                        break;
                    case "/cr":
                        for (Socket socket : chatRoom) {
                            if (!socket.equals(s)) {
                                DataOutputStream sender = new DataOutputStream(socket.getOutputStream());
                                sender.writeUTF("" + handles.get(s) + ": " + clientCommands[1]);
                            }
                        }
                        System.out.println();
                        break;
                    case "/dcCR":
                    case "/getUsers":
                        String users = "";
                        for (String handle : handles.values()) {
                            users += handle + ",";
                        }
                        writer.writeUTF(users.substring(0, users.length() - 1));
                        break;
                    case "/reqDirect":
                        String target = clientCommands[1];
                        Socket targetSocket = null;
                        for (Socket socket : handles.keySet()) {
                            if (handles.get(socket).equals(target)) {
                                targetSocket = socket;
                                break;
                            }
                        }
                        requestDM(targetSocket, target);
                        break;
                    case "/joinDirect":
                        chatRoom.add(s);
                        for (Socket socket : chatRoom) {
                            if (!socket.equals(s)) {
                                DataOutputStream sender = new DataOutputStream(socket.getOutputStream());
                                sender.writeUTF("--" + handles.get(s) + " has joined the chat room--");
                            }
                        }
                        break;
                    case "/direct":
                        for (Socket socket : chatRoom) {
                            if (!socket.equals(s)) {
                                DataOutputStream sender = new DataOutputStream(socket.getOutputStream());
                                sender.writeUTF("" + handles.get(s) + ": " + clientCommands[1]);
                            }
                        }
                        System.out.println();
                        break;
                    case "/dcDirect":
                        for (Socket socket : chatRoom) {
                            if (!socket.equals(s)) {
                                DataOutputStream sender = new DataOutputStream(socket.getOutputStream());
                                sender.writeUTF("--" + handles.get(s) + " has left the chat room--");
                            }
                        }
                        chatRoom.remove(s);
                        writer.writeUTF("/dc");
                        break;
                    case "/acc":
                        
                        break;
                    default:
                        System.out.println("Error: Command not found.");
                        break;
                }
            }
            handles.remove(s);
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server: Client " + s.getRemoteSocketAddress() + " has disconnected");
        }
    }

    static void requestDM(Socket targetSocket, String target) {
        try {
            DataOutputStream targetWriter = new DataOutputStream(targetSocket.getOutputStream());
            directChat = targetSocket;
            targetWriter.writeUTF("/requestDM " + handles.get(s));
        } catch (Exception e) {
            e.printStackTrace();
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